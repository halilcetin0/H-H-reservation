-- Add email verification and password reset columns to users table
ALTER TABLE users 
    ADD COLUMN IF NOT EXISTS email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS verification_token VARCHAR(255),
    ADD COLUMN IF NOT EXISTS verification_token_expires_at TIMESTAMP NULL,
    ADD COLUMN IF NOT EXISTS password_reset_token VARCHAR(255),
    ADD COLUMN IF NOT EXISTS password_reset_token_expires_at TIMESTAMP NULL;

-- Add indexes for token lookups
CREATE INDEX IF NOT EXISTS idx_verification_token ON users(verification_token);
CREATE INDEX IF NOT EXISTS idx_password_reset_token ON users(password_reset_token);
CREATE INDEX IF NOT EXISTS idx_email_verified ON users(email_verified);

