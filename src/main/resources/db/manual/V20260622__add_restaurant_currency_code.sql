ALTER TABLE restaurant
ADD COLUMN IF NOT EXISTS currency_code VARCHAR(3) DEFAULT 'LKR';

UPDATE restaurant
SET currency_code = 'LKR'
WHERE currency_code IS NULL;
