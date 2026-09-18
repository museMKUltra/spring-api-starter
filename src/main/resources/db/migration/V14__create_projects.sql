CREATE TABLE projects
(
    id          BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT       NOT NULL,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(255) NULL,
    status      VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_projects_user
        FOREIGN KEY (user_id) REFERENCES users (id)
            ON DELETE CASCADE,

    CONSTRAINT uk_projects_user_name
        UNIQUE (user_id, name)
);

CREATE INDEX idx_projects_user
    ON projects (user_id);

-- Create a default project for every existing user.
INSERT INTO projects (user_id, name)
SELECT id, 'Default'
FROM users;