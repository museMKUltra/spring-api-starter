CREATE TABLE attendance_label
(
    id         BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(100)     NOT NULL,
    type       TINYINT UNSIGNED NOT NULL COMMENT '0=work,1=break,2=overtime,3=leave',
    color      CHAR(7)          NOT NULL COMMENT 'HEX color like #RRGGBB',
    created_at DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE attendance_session
(
    id           BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,

    user_id      BIGINT NOT NULL,
    label_id     BIGINT UNSIGNED  NULL,

    clock_in     DATETIME         NOT NULL,
    clock_out    DATETIME         NULL,

    work_minutes INT UNSIGNED     NULL,
    work_date    DATE             NOT NULL,

    status       TINYINT UNSIGNED NOT NULL DEFAULT 0
        COMMENT '0=active,1=completed,2=cancelled',

    description  VARCHAR(255)     NULL,

    created_at   DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_user_date (user_id, work_date),
    INDEX idx_user_status (user_id, status),

    CONSTRAINT fk_session_user
        FOREIGN KEY (user_id) REFERENCES users (id),

    CONSTRAINT fk_session_label
        FOREIGN KEY (label_id) REFERENCES attendance_label (id)
);

CREATE TABLE employee_rates
(
    id             BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,

    user_id        BIGINT NOT NULL,
    hourly_rate    DECIMAL(10, 2)  NOT NULL,

    effective_from DATE            NOT NULL,
    effective_to   DATE            NULL,

    created_at     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_user_effective (user_id, effective_from, effective_to),

    CONSTRAINT fk_rates_user
        FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE work_summary
(
    id            BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,

    user_id       BIGINT NOT NULL,

    year          SMALLINT UNSIGNED NOT NULL,
    month         TINYINT UNSIGNED  NOT NULL,

    total_minutes INT UNSIGNED      NOT NULL DEFAULT 0,

    hourly_rate   DECIMAL(10, 2)    NOT NULL,
    salary_amount DECIMAL(12, 2)    NOT NULL DEFAULT 0,

    status        TINYINT UNSIGNED  NOT NULL DEFAULT 0
        COMMENT '0=draft,1=confirmed,2=paid',

    created_at    DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP,

    UNIQUE KEY uk_user_year_month (user_id, year, month),

    CONSTRAINT fk_summary_user
        FOREIGN KEY (user_id) REFERENCES users (id)
);
