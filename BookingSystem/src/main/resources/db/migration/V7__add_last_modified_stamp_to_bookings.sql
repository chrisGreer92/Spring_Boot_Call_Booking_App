
ALTER TABLE booking
    ADD COLUMN last_modified_at timestamptz NOT NULL DEFAULT now();


CREATE OR REPLACE FUNCTION set_last_modified_at()
RETURNS trigger AS $$
BEGIN
    NEW.last_modified_at := now();
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_set_last_modified_at ON booking;

CREATE TRIGGER trg_set_last_modified_at
    BEFORE UPDATE ON booking
    FOR EACH ROW
    EXECUTE FUNCTION set_last_modified_at();