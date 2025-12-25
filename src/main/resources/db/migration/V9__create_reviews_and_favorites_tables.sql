-- Create reviews table
CREATE TABLE IF NOT EXISTS reviews (
    id BIGSERIAL PRIMARY KEY,
    appointment_id BIGINT NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL,
    business_id BIGINT NOT NULL,
    employee_id BIGINT,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_review_appointment FOREIGN KEY (appointment_id) REFERENCES appointments(id) ON DELETE CASCADE,
    CONSTRAINT fk_review_customer FOREIGN KEY (customer_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_review_business FOREIGN KEY (business_id) REFERENCES businesses(id) ON DELETE CASCADE,
    CONSTRAINT fk_review_employee FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_review_appointment ON reviews(appointment_id);
CREATE INDEX IF NOT EXISTS idx_review_customer ON reviews(customer_id);
CREATE INDEX IF NOT EXISTS idx_review_business ON reviews(business_id);
CREATE INDEX IF NOT EXISTS idx_review_employee ON reviews(employee_id);

-- Create trigger for updated_at
CREATE TRIGGER update_reviews_updated_at BEFORE UPDATE
    ON reviews FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Create favorites table
CREATE TABLE IF NOT EXISTS favorites (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    business_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_favorite_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_favorite_business FOREIGN KEY (business_id) REFERENCES businesses(id) ON DELETE CASCADE,
    CONSTRAINT uk_user_business_favorite UNIQUE (user_id, business_id)
);

CREATE INDEX IF NOT EXISTS idx_favorite_user ON favorites(user_id);
CREATE INDEX IF NOT EXISTS idx_favorite_business ON favorites(business_id);

