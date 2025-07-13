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
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0
);

CREATE INDEX idx_patient_email ON patient(email);
-- CREATE INDEX idx_patient_phone_number ON patient(phone_number);
-- CREATE INDEX idx_patient_policy_number ON patient(policy_number);

-- Patient INSERT statements with UUID values
INSERT INTO patient (id, first_name, last_name, dob, email, phone_number, gender,
                    street, city, state, zip_code, country,
                    provider, policy_number, version) 
VALUES 
    ('550e8400-e29b-41d4-a716-446655440001', 'John', 'Doe', '1990-05-15', 'john.doe@email.com', '555-0101', 'MALE',
     '123 Main St', 'New York', 'NY', '10001', 'USA',
     'Blue Cross', 'BC123456789', 0),
    
    ('550e8400-e29b-41d4-a716-446655440002', 'Jane', 'Smith', '1985-08-22', 'jane.smith@email.com', '555-0102', 'FEMALE',
     '456 Oak Ave', 'Los Angeles', 'CA', '90210', 'USA',
     'Aetna', 'AE987654321', 0),
    
    ('550e8400-e29b-41d4-a716-446655440003', 'Michael', 'Johnson', '1978-12-03', 'michael.johnson@email.com', '555-0103', 'MALE',
     '789 Pine Rd', 'Chicago', 'IL', '60601', 'USA',
     'Cigna', 'CI456789123', 0),
    
    ('550e8400-e29b-41d4-a716-446655440004', 'Sarah', 'Williams', '1992-03-10', 'sarah.williams@email.com', '555-0104', 'FEMALE',
     '321 Elm St', 'Houston', 'TX', '77001', 'USA',
     'UnitedHealth', 'UH789123456', 0),
    
    ('550e8400-e29b-41d4-a716-446655440005', 'David', 'Brown', '1980-07-18', 'david.brown@email.com', '555-0105', 'MALE',
     '654 Maple Dr', 'Phoenix', 'AZ', '85001', 'USA',
     'Humana', 'HU321654987', 0),

    ('550e8400-e29b-41d4-a716-446655440006', 'Emily', 'Davis', '1987-11-25', 'emily.davis@email.com', '555-0106', 'FEMALE',
     '987 Cedar Ln', 'Miami', 'FL', '33101', 'USA',
     'Kaiser Permanente', 'KP111222333', 0),
    
    ('550e8400-e29b-41d4-a716-446655440007', 'Robert', 'Wilson', '1975-04-12', 'robert.wilson@email.com', '555-0107', 'MALE',
     '456 Birch Way', 'Seattle', 'WA', '98101', 'USA',
     'Anthem', 'AN444555666', 0),
    
    ('550e8400-e29b-41d4-a716-446655440008', 'Lisa', 'Anderson', '1995-09-08', 'lisa.anderson@email.com', '555-0108', 'FEMALE',
     '789 Spruce Ave', 'Denver', 'CO', '80201', 'USA',
     'Molina Healthcare', 'MH777888999', 0),
    
    ('550e8400-e29b-41d4-a716-446655440009', 'James', 'Taylor', '1982-01-30', 'james.taylor@email.com', '555-0109', 'MALE',
     '321 Willow St', 'Boston', 'MA', '02101', 'USA',
     'Centene', 'CE123789456', 0),
    
    ('550e8400-e29b-41d4-a716-446655440010', 'Amanda', 'Martinez', '1989-06-14', 'amanda.martinez@email.com', '555-0110', 'FEMALE',
     '654 Aspen Dr', 'San Francisco', 'CA', '94101', 'USA',
     'WellCare', 'WC456123789', 0),
    
    ('550e8400-e29b-41d4-a716-446655440011', 'Christopher', 'Garcia', '1973-12-07', 'christopher.garcia@email.com', '555-0111', 'MALE',
     '987 Redwood Blvd', 'Portland', 'OR', '97201', 'USA',
     'Bright Health', 'BH789456123', 0),
    
    ('550e8400-e29b-41d4-a716-446655440012', 'Jessica', 'Rodriguez', '1991-02-18', 'jessica.rodriguez@email.com', '555-0112', 'FEMALE',
     '123 Sycamore Ct', 'Atlanta', 'GA', '30301', 'USA',
     'Oscar Health', 'OH321654987', 0),
    
    ('550e8400-e29b-41d4-a716-446655440013', 'Daniel', 'Lee', '1984-07-22', 'daniel.lee@email.com', '555-0113', 'MALE',
     '456 Magnolia Pl', 'Dallas', 'TX', '75201', 'USA',
     'Ambetter', 'AM654987321', 0),
    
    ('550e8400-e29b-41d4-a716-446655440014', 'Nicole', 'White', '1988-03-05', 'nicole.white@email.com', '555-0114', 'FEMALE',
     '789 Dogwood Rd', 'Philadelphia', 'PA', '19101', 'USA',
     'Health Net', 'HN987321654', 0),
    
    ('550e8400-e29b-41d4-a716-446655440015', 'Matthew', 'Harris', '1979-10-11', 'matthew.harris@email.com', '555-0115', 'MALE',
     '321 Hickory Ln', 'Detroit', 'MI', '48201', 'USA',
     'CareSource', 'CS321987654', 0),
    
    ('550e8400-e29b-41d4-a716-446655440016', 'Ashley', 'Clark', '1993-08-29', 'ashley.clark@email.com', '555-0116', 'FEMALE',
     '654 Poplar Ave', 'Minneapolis', 'MN', '55401', 'USA',
     'Medica', 'MD654321987', 0),
    
    ('550e8400-e29b-41d4-a716-446655440017', 'Andrew', 'Lewis', '1981-05-16', 'andrew.lewis@email.com', '555-0117', 'MALE',
     '987 Chestnut St', 'Cleveland', 'OH', '44101', 'USA',
     'Medical Mutual', 'MM987654321', 0),
    
    ('550e8400-e29b-41d4-a716-446655440018', 'Samantha', 'Robinson', '1986-12-02', 'samantha.robinson@email.com', '555-0118', 'FEMALE',
     '123 Walnut Dr', 'Kansas City', 'MO', '64101', 'USA',
     'Blue KC', 'BK123456789', 0),
    
    ('550e8400-e29b-41d4-a716-446655440019', 'Kevin', 'Walker', '1977-09-19', 'kevin.walker@email.com', '555-0119', 'MALE',
     '456 Pecan Way', 'Las Vegas', 'NV', '89101', 'USA',
     'Prominence Health', 'PH456789123', 0),
    
    ('550e8400-e29b-41d4-a716-446655440020', 'Rachel', 'Perez', '1994-01-25', 'rachel.perez@email.com', '555-0120', 'FEMALE',
     '789 Almond Ct', 'Orlando', 'FL', '32801', 'USA',
     'Florida Blue', 'FB789123456', 0),
    
    ('550e8400-e29b-41d4-a716-446655440021', 'Ryan', 'Hall', '1983-11-08', 'ryan.hall@email.com', '555-0121', 'MALE',
     '321 Cashew Pl', 'San Diego', 'CA', '92101', 'USA',
     'Sharp Health Plan', 'SH321789456', 0),
    
    ('550e8400-e29b-41d4-a716-446655440022', 'Lauren', 'Young', '1990-04-13', 'lauren.young@email.com', '555-0122', 'FEMALE',
     '654 Pistachio Rd', 'Tampa', 'FL', '33601', 'USA',
     'Florida Health Care', 'FHC654456789', 0),
    
    ('550e8400-e29b-41d4-a716-446655440023', 'Brandon', 'Allen', '1976-06-27', 'brandon.allen@email.com', '555-0123', 'MALE',
     '987 Macadamia Ln', 'Austin', 'TX', '73301', 'USA',
     'Sendero Health', 'SH987789123', 0),
    
    ('550e8400-e29b-41d4-a716-446655440024', 'Stephanie', 'King', '1988-12-31', 'stephanie.king@email.com', '555-0124', 'FEMALE',
     '123 Hazelnut Ave', 'Nashville', 'TN', '37201', 'USA',
     'BlueCross BlueShield TN', 'BCBST123321654', 0),
    
    ('550e8400-e29b-41d4-a716-446655440025', 'Tyler', 'Wright', '1985-02-14', 'tyler.wright@email.com', '555-0125', 'MALE',
     '456 Brazil Nut St', 'Charlotte', 'NC', '28201', 'USA',
     'Blue Cross NC', 'BCNC456654987', 0);

