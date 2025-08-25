# !/bin/bash

set -e # stops the script if any command fails

BUCKET_NAME="cnf-templates-bucket"
REGION="us-east-1"

# Ensure AWS CLI uses LocalStack-friendly credentials and region
export AWS_ACCESS_KEY_ID="test"
export AWS_SECRET_ACCESS_KEY="test"
export AWS_DEFAULT_REGION="$REGION"
export AWS_EC2_METADATA_DISABLED=true

echo "🔧 Synthesizing CDK template..."
mvn exec:java -Dexec.mainClass="com.sachm.LocalStack" -Dexec.classpathScope=compile

# Check if bucket exists
if aws --endpoint-url=http://localhost:4566 s3api head-bucket --bucket "$BUCKET_NAME" 2>/dev/null; then
  echo "✅ Bucket $BUCKET_NAME already exists."
else
  echo "⚡ Creating bucket $BUCKET_NAME ..."
  aws --endpoint-url=http://localhost:4566 s3api create-bucket \
    --bucket "$BUCKET_NAME" \
    --region "$REGION" 
  echo "✅ Bucket created."
fi

echo "🗑️  Deleting existing stack if it exists..."
aws --endpoint-url=http://localhost:4566 cloudformation delete-stack \
    --stack-name patient-management || true

echo "⏳ Waiting for stack deletion to complete..."
aws --endpoint-url=http://localhost:4566 cloudformation wait stack-delete-complete \
    --stack-name patient-management || true

echo "📤 Uploading template to S3..."
aws --endpoint-url=http://localhost:4566 s3 cp cdk.out/localstack.template.json s3://$BUCKET_NAME/localstack.template.json

echo "🚀 Deploying CloudFormation stack..."
aws --endpoint-url=http://localhost:4566 cloudformation create-stack \
    --stack-name patient-management \
    --template-url https://$BUCKET_NAME.s3.localhost.localstack.cloud:4566/localstack.template.json \
    --capabilities CAPABILITY_IAM CAPABILITY_NAMED_IAM || \
aws --endpoint-url=http://localhost:4566 cloudformation update-stack \
    --stack-name patient-management \
    --template-url https://$BUCKET_NAME.s3.localhost.localstack.cloud:4566/localstack.template.json \
    --capabilities CAPABILITY_IAM CAPABILITY_NAMED_IAM

echo "⏳ Waiting for stack creation to complete..."
aws --endpoint-url=http://localhost:4566 cloudformation wait stack-create-complete \
    --stack-name patient-management

echo "🌐 Getting load balancer DNS name..."
aws --endpoint-url=http://localhost:4566 elbv2 describe-load-balancers \
    --query "LoadBalancers[0].DNSName" --output text
