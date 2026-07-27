ALTER TABLE bank_accounts
ALTER COLUMN currency_code TYPE VARCHAR(3)
    USING TRIM(currency_code);