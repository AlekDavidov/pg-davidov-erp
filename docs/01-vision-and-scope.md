# PG Davidov ERP – Vision & Scope

## Vizija

PG Davidov ERP je lokalno hostovana poslovna aplikacija za evidenciju finansija i dokumentacije poljoprivrednog gazdinstva. Prva verzija zamenjuje operativni deo postojećeg Google Sheets ERP-a, bez širenja na stoku, proizvodnju, mehanizaciju i zalihe.

## Glavni ciljevi MVP-a

1. Jedinstveno i pouzdano mesto za dobavljače, fakture, transakcije i dokumente.
2. Direktan uvoz bankovnih PDF izvoda AIK i Intesa banke.
3. Automatsko prepoznavanje dobavljača i kategorije preko postojećih alias pravila.
4. Tačno povezivanje jedne ili više uplata sa jednom ili više faktura.
5. Supplier ledger i osnovni finansijski dashboard.
6. Potpuna sledljivost izvora podatka i istorije izmena.

## U MVP-u

- Suppliers
- Supplier aliases
- Categories
- Bank accounts
- Bank statement import
- Transactions
- Invoices
- Invoice payments
- Documents
- Supplier ledger
- Basic dashboard and reports
- Audit log
- Backup baze

## Van MVP-a

- Stoka
- Proizvodnja mleka i sira
- Mehanizacija i servisi
- Zalihe hrane
- Mobilna aplikacija
- Multi-tenant SaaS
- AI asistent
- Fiskalizacija i računovodstveni obračuni

## Način korišćenja

Prva verzija radi lokalno kroz Docker na korisnikovom laptopu. Aplikaciji se pristupa iz browsera preko `http://localhost`.
