-- Create work_schedules table
CREATE TABLE IF NOT EXISTS work_schedules (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    day_of_week VARCHAR(20) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_schedule_employee FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE,
    CONSTRAINT uk_employee_day UNIQUE (employee_id, day_of_week)
);

CREATE INDEX IF NOT EXISTS idx_schedule_employee ON work_schedules(employee_id);
CREATE INDEX IF NOT EXISTS idx_schedule_day ON work_schedules(day_of_week);

-- Create trigger for updated_at
CREATE TRIGGER update_work_schedules_updated_at BEFORE UPDATE
    ON work_schedules FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

