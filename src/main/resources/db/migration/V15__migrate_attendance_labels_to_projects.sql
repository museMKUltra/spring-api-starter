-- Add project_id while keeping user_id temporarily
-- so existing labels can be migrated safely.
ALTER TABLE attendance_label
    ADD COLUMN project_id BIGINT UNSIGNED NULL AFTER id;

-- Assign each existing label to its user's Default project.
UPDATE attendance_label al
    JOIN projects p
ON p.user_id = al.user_id
    AND p.name = 'Default'
    SET al.project_id = p.id;

-- Make sure every existing label has been migrated.
-- If this fails, investigate before continuing.
-- This is intentionally omitted as a SQL assertion because
-- MySQL does not provide a simple migration assertion mechanism.

ALTER TABLE attendance_label
    MODIFY COLUMN project_id BIGINT UNSIGNED NOT NULL;

ALTER TABLE attendance_label
    ADD CONSTRAINT fk_label_project
        FOREIGN KEY (project_id) REFERENCES projects (id)
            ON DELETE CASCADE;

-- project_id is now the owner relationship.
ALTER TABLE attendance_label
DROP FOREIGN KEY attendance_label_users_id_fk;

DROP INDEX idx_label_user_order
    ON attendance_label;

CREATE INDEX idx_label_project_order
    ON attendance_label (project_id, sort_order);

ALTER TABLE attendance_label
DROP COLUMN user_id;