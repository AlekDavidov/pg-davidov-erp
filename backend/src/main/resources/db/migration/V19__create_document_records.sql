CREATE TABLE document_records (
                                  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                                  title VARCHAR(255) NOT NULL,

                                  document_type VARCHAR(50) NOT NULL,

                                  document_number VARCHAR(100),

                                  supplier_id UUID
                                      REFERENCES suppliers(id)
                                                         ON DELETE SET NULL,

                                  document_date DATE,

                                  valid_from DATE,

                                  valid_until DATE,

                                  notes TEXT,

                                  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                                  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

                                  CHECK (
                                      valid_from IS NULL
                                          OR valid_until IS NULL
                                          OR valid_until >= valid_from
                                      )
);

CREATE TABLE document_record_documents (
                                           document_record_id UUID NOT NULL
                                               REFERENCES document_records(id)
                                                   ON DELETE CASCADE,

                                           document_id UUID NOT NULL
                                               REFERENCES documents(id)
                                                   ON DELETE CASCADE,

                                           PRIMARY KEY (
                                                        document_record_id,
                                                        document_id
                                               )
);

CREATE INDEX idx_document_records_type
    ON document_records(document_type);

CREATE INDEX idx_document_records_supplier
    ON document_records(supplier_id);

CREATE INDEX idx_document_records_document_date
    ON document_records(document_date);

CREATE INDEX idx_document_records_valid_until
    ON document_records(valid_until);