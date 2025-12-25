-- Create businesses table
CREATE TABLE IF NOT EXISTS businesses (
    id BIGSERIAL PRIMARY KEY,
    owner_id BIGINT NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(100),
    address VARCHAR(500),
    city VARCHAR(100),
    business_type VARCHAR(50),
    phone VARCHAR(20),
    email VARCHAR(255),
    image_url VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_business_owner FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_business_owner ON businesses(owner_id);
CREATE INDEX IF NOT EXISTS idx_business_city ON businesses(city);
CREATE INDEX IF NOT EXISTS idx_business_category ON businesses(category);
CREATE INDEX IF NOT EXISTS idx_business_active ON businesses(is_active);

-- Create trigger for updated_at
CREATE TRIGGER update_businesses_updated_at BEFORE UPDATE
    ON businesses FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

