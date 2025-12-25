-- Update appointments table for business management
ALTER TABLE appointments 
    ADD COLUMN IF NOT EXISTS business_id BIGINT,
    ADD COLUMN IF NOT EXISTS service_id BIGINT,
    ADD COLUMN IF NOT EXISTS employee_id BIGINT,
    ADD COLUMN IF NOT EXISTS price DECIMAL(10, 2),
    ADD COLUMN IF NOT EXISTS payment_status VARCHAR(50) DEFAULT 'PENDING';

-- Add foreign keys
ALTER TABLE appointments 
    ADD CONSTRAINT fk_appointment_business FOREIGN KEY (business_id) REFERENCES businesses(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_appointment_service FOREIGN KEY (service_id) REFERENCES services(id) ON DELETE SET NULL,
    ADD CONSTRAINT fk_appointment_employee FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE SET NULL;

-- Remove old foreign keys if they exist
ALTER TABLE appointments DROP CONSTRAINT IF EXISTS fk_appointment_user;
ALTER TABLE appointments DROP CONSTRAINT IF EXISTS fk_appointment_provider;

-- Drop old columns
ALTER TABLE appointments 
    DROP COLUMN IF EXISTS service_provider_id,
    DROP COLUMN IF EXISTS duration_minutes,
    DROP COLUMN IF EXISTS service_type;

-- Rename appointment_date to start_time
ALTER TABLE appointments RENAME COLUMN appointment_date TO start_time;

-- Add end_time
ALTER TABLE appointments ADD COLUMN IF NOT EXISTS end_time TIMESTAMP;

-- Add customer_id (renamed from user_id conceptually)
ALTER TABLE appointments ADD COLUMN IF NOT EXISTS customer_id BIGINT;
ALTER TABLE appointments 
    ADD CONSTRAINT fk_appointment_customer FOREIGN KEY (customer_id) REFERENCES users(id) ON DELETE CASCADE;

-- Drop user_id if exists and copy to customer_id
UPDATE appointments SET customer_id = user_id WHERE customer_id IS NULL;
ALTER TABLE appointments DROP COLUMN IF EXISTS user_id;

-- Add indexes
CREATE INDEX IF NOT EXISTS idx_appointment_business ON appointments(business_id);
CREATE INDEX IF NOT EXISTS idx_appointment_service ON appointments(service_id);
CREATE INDEX IF NOT EXISTS idx_appointment_employee ON appointments(employee_id);
CREATE INDEX IF NOT EXISTS idx_appointment_customer ON appointments(customer_id);
CREATE INDEX IF NOT EXISTS idx_appointment_start_time ON appointments(start_time);
CREATE INDEX IF NOT EXISTS idx_appointment_payment_status ON appointments(payment_status);

