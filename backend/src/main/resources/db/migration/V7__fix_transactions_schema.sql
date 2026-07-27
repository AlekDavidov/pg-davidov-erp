ALTER TABLE transactions
ALTER COLUMN currency_code TYPE VARCHAR(3)
    USING TRIM(currency_code);

ALTER TABLE transactions
    ADD CONSTRAINT transactions_supplier_id_fkey
        FOREIGN KEY (supplier_id)
            REFERENCES suppliers(id);