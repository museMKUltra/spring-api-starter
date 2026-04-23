-- 1. add nullable sort_order column
ALTER TABLE attendance_label
    ADD COLUMN sort_order INT;

-- 2. set sort_order of columns grouped by user_id
UPDATE attendance_label l
    JOIN (SELECT id,
                 ROW_NUMBER() OVER (
                     PARTITION BY COALESCE(user_id, 0)
                     ORDER BY id
                     ) - 1 AS rn
          FROM attendance_label
          WHERE deleted_at IS NULL) t ON l.id = t.id
SET l.sort_order = t.rn;

-- 3. set sort_order of deleted columns to 0
UPDATE attendance_label
SET sort_order = 0
WHERE deleted_at IS NOT NULL;

-- 4. make sort_order column not nullable
ALTER TABLE attendance_label
    MODIFY sort_order INT NOT NULL;

-- 5. create index
CREATE INDEX idx_label_user_order
    ON attendance_label (user_id, sort_order);