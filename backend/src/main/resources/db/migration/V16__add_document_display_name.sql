ALTER TABLE documents
    ADD COLUMN display_name VARCHAR(255);

UPDATE documents
SET display_name = filename
WHERE display_name IS NULL;

ALTER TABLE documents
    ALTER COLUMN display_name SET NOT NULL;