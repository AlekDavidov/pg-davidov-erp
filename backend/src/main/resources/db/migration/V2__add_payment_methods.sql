CREATE TABLE payment_methods
(
    id         UUID                     DEFAULT gen_random_uuid() NOT NULL
        PRIMARY KEY,
    code       VARCHAR(30)                                        NOT NULL
        UNIQUE,
    name       VARCHAR(100)                                       NOT NULL
        UNIQUE,
    active     BOOLEAN                  DEFAULT TRUE              NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()             NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()             NOT NULL
);

INSERT INTO payment_methods (code, name)
VALUES
    ('BANK_TRANSFER', 'Prenos na račun'),
    ('CASH', 'Gotovina'),
    ('CARD', 'Kartica');

ALTER TABLE suppliers
    ADD COLUMN payment_method_id UUID;

ALTER TABLE suppliers
    ADD CONSTRAINT fk_suppliers_payment_method
        FOREIGN KEY (payment_method_id)
            REFERENCES payment_methods (id);

CREATE INDEX idx_suppliers_payment_method
    ON suppliers (payment_method_id);

ALTER TABLE suppliers
DROP COLUMN payment_method;