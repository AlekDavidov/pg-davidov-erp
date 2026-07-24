CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TYPE category_type AS ENUM ('INCOME', 'EXPENSE', 'BOTH');
CREATE TYPE transaction_status AS ENUM ('NEW', 'VERIFIED', 'MATCHED', 'CANCELLED');
CREATE TYPE transaction_source AS ENUM ('MANUAL', 'BANK_IMPORT', 'MIGRATION');
CREATE TYPE statement_validation_status AS ENUM ('PENDING', 'VALID', 'INVALID', 'MANUALLY_APPROVED');
CREATE TYPE supplier_match_status AS ENUM ('UNMATCHED', 'SUGGESTED', 'CONFIRMED');

CREATE TABLE app_users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL UNIQUE,
    category_type category_type NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE suppliers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    default_category_id UUID REFERENCES categories(id),
    payment_method VARCHAR(50),
    payment_term_days INTEGER CHECK (payment_term_days IS NULL OR payment_term_days >= 0),
    tax_id VARCHAR(20),
    registration_number VARCHAR(20),
    phone VARCHAR(50),
    email VARCHAR(200),
    contact_person VARCHAR(150),
    address TEXT,
    notes TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE supplier_aliases (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    supplier_id UUID NOT NULL REFERENCES suppliers(id),
    alias VARCHAR(255) NOT NULL,
    normalized_alias VARCHAR(255) NOT NULL UNIQUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (supplier_id, alias)
);

CREATE TABLE bank_accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(30) NOT NULL UNIQUE,
    bank_name VARCHAR(150) NOT NULL,
    account_number VARCHAR(100) NOT NULL UNIQUE,
    currency_code CHAR(3) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE bank_statements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    bank_account_id UUID NOT NULL REFERENCES bank_accounts(id),
    statement_code VARCHAR(50) NOT NULL UNIQUE,
    period_from DATE,
    period_to DATE,
    opening_balance NUMERIC(19,2) NOT NULL,
    total_income NUMERIC(19,2) NOT NULL DEFAULT 0,
    total_expenses NUMERIC(19,2) NOT NULL DEFAULT 0,
    closing_balance NUMERIC(19,2) NOT NULL,
    validation_status statement_validation_status NOT NULL DEFAULT 'PENDING',
    original_filename VARCHAR(255) NOT NULL,
    file_checksum_sha256 VARCHAR(64),
    imported_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (bank_account_id, file_checksum_sha256)
);

CREATE TABLE bank_statement_rows (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    bank_statement_id UUID NOT NULL REFERENCES bank_statements(id) ON DELETE CASCADE,
    entry_no INTEGER NOT NULL,
    booking_date DATE NOT NULL,
    execution_date DATE,
    income NUMERIC(19,2) NOT NULL DEFAULT 0,
    expenses NUMERIC(19,2) NOT NULL DEFAULT 0,
    counterparty_raw VARCHAR(500),
    description_raw TEXT,
    order_type VARCHAR(150),
    order_reference VARCHAR(255),
    complaint_reference VARCHAR(255),
    page_number INTEGER,
    suggested_supplier_id UUID REFERENCES suppliers(id),
    match_status supplier_match_status NOT NULL DEFAULT 'UNMATCHED',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (bank_statement_id, entry_no),
    CHECK (income >= 0 AND expenses >= 0),
    CHECK (NOT (income > 0 AND expenses > 0))
);

CREATE TABLE transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transaction_code VARCHAR(40) NOT NULL UNIQUE,
    transaction_date DATE NOT NULL,
    currency_code CHAR(3) NOT NULL,
    debit NUMERIC(19,2) NOT NULL DEFAULT 0,
    credit NUMERIC(19,2) NOT NULL DEFAULT 0,
    description TEXT,
    raw_counterparty VARCHAR(500),
    bank_account_id UUID REFERENCES bank_accounts(id),
    supplier_id UUID REFERENCES suppliers(id),
    category_id UUID REFERENCES categories(id),
    bank_statement_row_id UUID UNIQUE REFERENCES bank_statement_rows(id),
    reference VARCHAR(255),
    status transaction_status NOT NULL,
    source transaction_source NOT NULL,
    verified BOOLEAN NOT NULL DEFAULT FALSE,
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK (debit >= 0 AND credit >= 0),
    CHECK (NOT (debit > 0 AND credit > 0)),
    CHECK (debit > 0 OR credit > 0)
);

CREATE TABLE invoices (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    invoice_code VARCHAR(40) NOT NULL UNIQUE,
    invoice_number VARCHAR(150) NOT NULL,
    supplier_id UUID NOT NULL REFERENCES suppliers(id),
    invoice_date DATE NOT NULL,
    due_date DATE,
    amount NUMERIC(19,2) NOT NULL CHECK (amount > 0),
    currency_code CHAR(3) NOT NULL,
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (supplier_id, invoice_number)
);

CREATE TABLE invoice_payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    invoice_id UUID NOT NULL REFERENCES invoices(id) ON DELETE CASCADE,
    transaction_id UUID NOT NULL REFERENCES transactions(id),
    amount NUMERIC(19,2) NOT NULL CHECK (amount > 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (invoice_id, transaction_id)
);

CREATE TABLE documents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    document_code VARCHAR(40) NOT NULL UNIQUE,
    filename VARCHAR(255) NOT NULL,
    content_type VARCHAR(150),
    size_bytes BIGINT CHECK (size_bytes IS NULL OR size_bytes >= 0),
    storage_path TEXT NOT NULL,
    checksum_sha256 VARCHAR(64) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE invoice_documents (
    invoice_id UUID NOT NULL REFERENCES invoices(id) ON DELETE CASCADE,
    document_id UUID NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    PRIMARY KEY (invoice_id, document_id)
);

CREATE TABLE transaction_documents (
    transaction_id UUID NOT NULL REFERENCES transactions(id) ON DELETE CASCADE,
    document_id UUID NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    PRIMARY KEY (transaction_id, document_id)
);

CREATE TABLE audit_log (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID REFERENCES app_users(id),
    entity_type VARCHAR(100) NOT NULL,
    entity_id VARCHAR(100) NOT NULL,
    action VARCHAR(50) NOT NULL,
    old_value JSONB,
    new_value JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_supplier_aliases_supplier ON supplier_aliases(supplier_id);
CREATE INDEX idx_bank_statement_rows_statement ON bank_statement_rows(bank_statement_id);
CREATE INDEX idx_transactions_date ON transactions(transaction_date);
CREATE INDEX idx_transactions_supplier ON transactions(supplier_id);
CREATE INDEX idx_transactions_category ON transactions(category_id);
CREATE INDEX idx_invoices_supplier ON invoices(supplier_id);
CREATE INDEX idx_invoices_due_date ON invoices(due_date);
CREATE INDEX idx_invoice_payments_invoice ON invoice_payments(invoice_id);
CREATE INDEX idx_invoice_payments_transaction ON invoice_payments(transaction_id);
