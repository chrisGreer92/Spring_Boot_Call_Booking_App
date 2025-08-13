ALTER TABLE booking
    ALTER COLUMN start_time TYPE timestamptz,
    ALTER COLUMN end_time TYPE timestamptz,
    ALTER COLUMN created_at TYPE timestamptz;