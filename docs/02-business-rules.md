# Poslovna pravila

## Sedam osnovnih pravila

1. Podaci se ne dupliraju bez opravdanog razloga.
2. Svaki entitet ima stabilan interni ID.
3. Sve vrednosti koje korisnik bira dolaze iz kontrolisanih lista.
4. Sve što može pouzdano da se popuni automatski, popunjava se automatski.
5. Poslovna logika se ne ponavlja na više mesta.
6. Poslovna pravila su u backend-u; frontend radi prikaz i osnovnu validaciju.
7. Svaka izmena baze ide kroz Flyway migraciju.

## Transakcije

- Ručno kreirana transakcija počinje statusom `NEW`.
- Uvezena bankovna transakcija počinje statusom `VERIFIED` tek nakon uspešne validacije izvoda.
- Transakcija prelazi u `MATCHED` kada je povezana sa dokumentom ili fakturom u skladu sa pravilima sistema.
- Dozvoljeni statusi: `NEW`, `VERIFIED`, `MATCHED`, `CANCELLED`.
- Izvor transakcije se čuva kao `MANUAL`, `BANK_IMPORT` ili drugi budući kontrolisani izvor.

## Fakture

- Status fakture se ne čuva kao nezavisna istina, već se izračunava iz iznosa, dospeća i povezanih plaćanja.
- Izračunati statusi: `OPEN`, `OVERDUE`, `PARTIALLY_PAID`, `PARTIALLY_PAID_OVERDUE`, `PAID`, `OVERPAID`.
- Jedna uplata može pokrivati više faktura.
- Jedna faktura može imati više uplata.
- Povezivanje se čuva u posebnoj tabeli `invoice_payments`.

## Bankovni uvoz

- Svaki izvod ima jedinstveni `statement_code`, npr. `AIK-2026-05-005` ili `INTESA-2024-06-001`.
- Isti izvod ne sme biti uvezen dva puta.
- Svaki red izvoda čuva originalni tekst banke radi sledljivosti.
- Validacija izvoda koristi: početno stanje + prilivi - odlivi = završno stanje.
- Neuspešna validacija ne kreira finalne transakcije bez ručne potvrde.

## Supplier matching

- Alias pripada tačno jednom dobavljaču.
- Alias se normalizuje pre poređenja.
- Tačan alias može automatski popuniti dobavljača i podrazumevanu kategoriju.
- Nesiguran pogodak ostaje samo predlog i zahteva potvrdu.
