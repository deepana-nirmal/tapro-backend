DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = current_schema()
          AND table_name = 'orders'
          AND column_name = 'table_number'
          AND udt_name = 'bytea'
    ) THEN
        ALTER TABLE orders
            ALTER COLUMN table_number TYPE text
            USING CASE
                WHEN table_number IS NULL THEN NULL
                ELSE convert_from(table_number, 'UTF8')
            END;
    END IF;
END $$;
