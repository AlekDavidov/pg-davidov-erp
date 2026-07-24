# PG Davidov ERP — Sprint 1 Foundation

Pokretljiv foundation: PostgreSQL, Spring Boot, Flyway, Vue, PrimeVue i Docker Compose.

## Pokretanje

1. Kopiraj `.env.example` u `.env` i promeni lozinke.
2. Pokreni `docker compose up --build -d`.
3. Otvori aplikaciju na http://localhost:8081.

Backend health: http://localhost:8080/actuator/health  
Swagger UI: http://localhost:8080/swagger-ui.html

PgAdmin je opcion: `docker compose --profile tools up -d pgadmin`, zatim http://localhost:5050.

Gašenje: `docker compose down`. Brisanje lokalne baze: `docker compose down -v`.

## Trenutni obuhvat

- kompletna V1 Flyway šema iz Sprinta 0;
- Spring Boot backend skeleton, health i OpenAPI;
- Vue aplikacija sa navigacijom i dark mode-om;
- PostgreSQL persistent volume;
- opcion PgAdmin profil.

Sledeći korak: Organizations, Suppliers i Categories kao prvi kompletni vertikalni slice.
