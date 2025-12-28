-- Add user_id to employees table for linking with user accounts
ALTER TABLE employees 
    ADD COLUMN IF NOT EXISTS user_id BIGINT,
    ADD CONSTRAINT fk_employee_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_employee_user ON employees(user_id);

-- Add approval fields to appointments table
ALTER TABLE appointments 
    ADD COLUMN IF NOT EXISTS owner_approved BOOLEAN DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS employee_approved BOOLEAN DEFAULT FALSE;

CREATE INDEX IF NOT EXISTS idx_appointment_owner_approved ON appointments(owner_approved);
CREATE INDEX IF NOT EXISTS idx_appointment_employee_approved ON appointments(employee_approved);


