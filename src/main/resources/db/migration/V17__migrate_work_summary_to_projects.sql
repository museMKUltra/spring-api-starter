-- Add project_id while keeping user_id temporarily.
ALTER TABLE work_summary
    ADD COLUMN project_id BIGINT UNSIGNED NULL AFTER user_id;

-- Assign each existing summary to its user's Default project.
UPDATE work_summary ws
    JOIN projects p
ON p.user_id = ws.user_id
    AND p.name = 'Default'
    SET ws.project_id = p.id;

-- Make project_id mandatory after migration.
ALTER TABLE work_summary
    MODIFY COLUMN project_id BIGINT UNSIGNED NOT NULL;

ALTER TABLE work_summary
    ADD CONSTRAINT fk_summary_project
        FOREIGN KEY (project_id) REFERENCES projects (id)
            ON DELETE CASCADE;

-- A summary is now unique per project and month instead of per user.
ALTER TABLE work_summary
    ADD CONSTRAINT uk_summary_project_year_month
        UNIQUE (project_id, year, month);

-- fk_summary_user relies on uk_user_year_month for its index,
-- so swap in a replacement index in the same statement.
ALTER TABLE work_summary
    ADD INDEX idx_summary_user (user_id),
    DROP INDEX uk_user_year_month;
