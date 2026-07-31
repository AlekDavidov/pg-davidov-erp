CREATE SEQUENCE IF NOT EXISTS supplier_code_seq
    START WITH 1
    INCREMENT BY 1;

SELECT setval(
               'supplier_code_seq',
               GREATEST(
                       COALESCE(
                               (
                                   SELECT MAX(
                                                  CAST(SUBSTRING(code FROM 4) AS BIGINT)
                                          )
                                   FROM suppliers
                                   WHERE code ~ '^SUP[0-9]+$'
            ),
            0
        ) + 1,
                       1
               ),
               false
       );