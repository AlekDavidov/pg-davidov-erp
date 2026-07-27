CREATE TABLE suppliers
(
    id                 UUID PRIMARY KEY,
    code               VARCHAR(30)  NOT NULL UNIQUE,
    name               VARCHAR(200) NOT NULL,
    default_category_id UUID,
    payment_method_id   UUID,
    payment_terms      INTEGER      NOT NULL DEFAULT 0,
    pib                VARCHAR(20),
    registration_number VARCHAR(20),
    phone              VARCHAR(50),
    email              VARCHAR(255),
    contact_person     VARCHAR(100),
    notes              TEXT,
    active             BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at         TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at         TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_supplier_category
        FOREIGN KEY (default_category_id)
            REFERENCES categories (id),

    CONSTRAINT fk_supplier_payment_method
        FOREIGN KEY (payment_method_id)
            REFERENCES payment_methods (id)
);