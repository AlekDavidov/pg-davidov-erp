# Database Dictionary

## app_users
Lokalni korisnici aplikacije. MVP može početi sa jednim administratorskim nalogom, ali model ostaje spreman za više korisnika.

## categories
Kontrolisana lista kategorija prihoda i rashoda. `category_type` određuje da li je kategorija `INCOME`, `EXPENSE` ili `BOTH`.

## suppliers
Jedinstveni šifarnik dobavljača. Polje `default_category_id` služi samo kao podrazumevani predlog, ne kao obavezna kategorija svake transakcije.

## supplier_aliases
Normalizovani nazivi koji se pojavljuju na bankovnim izvodima. Jedan alias pripada jednom dobavljaču.

## bank_accounts
Računi gazdinstva po banci i valuti.

## bank_statements
Zaglavlje uvezenog izvoda. Čuva zbirne vrednosti i rezultat validacije.

## bank_statement_rows
Originalni redovi sa izvoda. Ovo je staging i audit sloj; originalni tekst se ne menja nakon uvoza.

## transactions
Finalne poslovne transakcije. Debit i credit su međusobno isključivi i proveravaju se CHECK ograničenjem.

## invoices
Primljene fakture. Status se izračunava, ne čuva trajno.

## invoice_payments
Raspodela iznosa transakcije na fakture. Omogućava many-to-many vezu.

## documents
Metapodaci o fajlovima. Fajlovi se u MVP-u čuvaju na lokalnom disku, dok baza čuva putanju i checksum.

## invoice_documents / transaction_documents
Veze dokumenata sa fakturama i transakcijama.

## audit_log
Neizmenjivi trag važnih izmena u aplikaciji.
