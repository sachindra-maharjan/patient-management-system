CREATE TABLE IF NOT EXISTS patient (
    id UUID PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    dob DATE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    gender VARCHAR(10) NOT NULL,
    street VARCHAR(100) NOT NULL,
    city VARCHAR(50) NOT NULL,
    state VARCHAR(50) NOT NULL,
    zip_code VARCHAR(20) NOT NULL,
    country VARCHAR(50) NOT NULL,
    provider VARCHAR(100) NOT NULL,
    policy_number VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_patient_email ON patient(email);
CREATE INDEX IF NOT EXISTS idx_patient_name ON patient(first_name, last_name);

-- CREATE OR REPLACE FUNCTION update_updated_at_column()
-- RETURNS TRIGGER AS $$
-- BEGIN
--    NEW.updated_at = CURRENT_TIMESTAMP;
--    RETURN NEW;
-- END;
-- -- $$ language 'plpgsql';

-- CREATE TRIGGER IF NOT EXISTS set_updated_at
-- BEFORE UPDATE ON patient
-- FOR EACH ROW
-- EXECUTE FUNCTION update_updated_at_column();
