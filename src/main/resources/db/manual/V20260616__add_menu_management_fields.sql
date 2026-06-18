ALTER TABLE menu_items ADD COLUMN visible BOOLEAN;
ALTER TABLE menu_items ADD COLUMN featured BOOLEAN;
ALTER TABLE menu_items ADD COLUMN preparation_time INTEGER;

CREATE TABLE menu_item_ingredients (
    menu_item_id BIGINT NOT NULL,
    ingredient VARCHAR(255)
);

CREATE TABLE menu_item_allergens (
    menu_item_id BIGINT NOT NULL,
    allergen VARCHAR(255)
);
