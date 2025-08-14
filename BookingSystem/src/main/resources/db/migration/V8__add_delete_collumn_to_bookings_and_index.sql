ALTER TABLE booking
    ADD COLUMN deleted boolean NOT NULL DEFAULT false;

CREATE INDEX IF NOT EXISTS idx_booking_deleted_status_start
    ON booking (deleted, status, start_time);