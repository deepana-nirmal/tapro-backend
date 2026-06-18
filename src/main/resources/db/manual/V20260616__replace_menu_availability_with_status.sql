ALTER TABLE menu_items ADD COLUMN status VARCHAR(30);

UPDATE menu_items
SET status = CASE
    WHEN visible = FALSE THEN 'HIDDEN'
    WHEN available = FALSE THEN 'OUT_OF_STOCK'
    ELSE 'AVAILABLE'
END;

ALTER TABLE menu_items DROP COLUMN available;
ALTER TABLE menu_items DROP COLUMN visible;
