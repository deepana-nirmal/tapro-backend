ALTER TABLE menu_items
ADD COLUMN featured_label VARCHAR(50);

UPDATE menu_items
SET featured_label = 'TODAY_SPECIAL'
WHERE featured = TRUE AND featured_label IS NULL;
