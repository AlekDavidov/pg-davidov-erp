# Migracija iz Google Sheets-a

## Princip

Google Sheets ostaje glavni sistem dok nova aplikacija ne prođe paralelno testiranje.

## Redosled uvoza

1. Categories
2. Suppliers
3. Supplier Aliases
4. Bank Accounts
5. Documents metadata
6. Invoices
7. Transactions
8. Invoice Payments

## Kontrole nakon migracije

- Broj redova po entitetu.
- Zbir prihoda i rashoda po godini.
- Broj faktura po statusu.
- Saldo po dobavljaču.
- Broj povezanih dokumenata.
- Nasumična ručna provera najmanje 20 zapisa.

## Cutover

1. Finalni backup Google Sheets-a.
2. Poslednji import u novu bazu.
3. Poređenje kontrolnih izveštaja.
4. Zaključavanje Sheet-a za unos.
5. Nova aplikacija postaje glavni sistem.
