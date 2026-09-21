ALTER TABLE attendance_label
    ADD COLUMN user_id BIGINT NULL AFTER id;

UPDATE attendance_label al
    JOIN projects p
ON p.id = al.project_id
    AND p.name = 'Default'
    SET al.user_id = p.user_id;

ALTER TABLE attendance_label
    MODIFY COLUMN user_id BIGINT NOT NULL;

ALTER TABLE attendance_label
    ADD CONSTRAINT attendance_label_users_id_fk
        FOREIGN KEY (user_id) REFERENCES users (id);