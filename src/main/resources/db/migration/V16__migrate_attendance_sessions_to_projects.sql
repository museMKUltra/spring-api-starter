-- Add project_id while keeping user_id temporarily.
ALTER TABLE attendance_session
    ADD COLUMN project_id BIGINT UNSIGNED NULL AFTER user_id;

-- Assign each existing session to its user's Default project.
UPDATE attendance_session s
    JOIN projects p
ON p.user_id = s.user_id
    AND p.name = 'Default'
    SET s.project_id = p.id;

-- Make project_id mandatory after migration.
ALTER TABLE attendance_session
    MODIFY COLUMN project_id BIGINT UNSIGNED NOT NULL;

ALTER TABLE attendance_session
    ADD CONSTRAINT fk_session_project
        FOREIGN KEY (project_id) REFERENCES projects (id)
            ON DELETE CASCADE;

CREATE INDEX idx_session_project_date
    ON attendance_session (project_id, work_date);