CREATE TABLE company_profile (
                                 id UUID PRIMARY KEY,

                                 name VARCHAR(255) NOT NULL,

                                 pib VARCHAR(50),

                                 registration_number VARCHAR(50),

                                 address VARCHAR(255),

                                 city VARCHAR(120),

                                 postal_code VARCHAR(20),

                                 phone VARCHAR(50),

                                 email VARCHAR(255),

                                 bank_name VARCHAR(255),

                                 bank_account_number VARCHAR(100),

                                 created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                 updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);