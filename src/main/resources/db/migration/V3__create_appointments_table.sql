-- Create appointments table
CREATE TABLE IF NOT EXISTS appointments (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    service_provider_id BIGINT NOT NULL,
    appointment_date TIMESTAMP NOT NULL,
    duration_minutes INT NOT NULL,
    service_type VARCHAR(255) NOT NULL,
    notes TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    cancellation_reason VARCHAR(500),
    reminder_sent BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_appointment_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_appointment_provider FOREIGN KEY (service_provider_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_user_id ON appointments(user_id);
CREATE INDEX IF NOT EXISTS idx_provider_id ON appointments(service_provider_id);
CREATE INDEX IF NOT EXISTS idx_appointment_date ON appointments(appointment_date);
CREATE INDEX IF NOT EXISTS idx_status ON appointments(status);
CREATE INDEX IF NOT EXISTS idx_reminder_sent ON appointments(reminder_sent);

-- Create trigger for updated_at
CREATE TRIGGER update_appointments_updated_at BEFORE UPDATE
    ON appointments FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

