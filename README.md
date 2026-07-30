# PG Davidov ERP — Sprint 1 Foundation

Pokretljiv foundation: PostgreSQL, Spring Boot, Flyway, Vue, PrimeVue i Docker Compose.

## Pokretanje

## Local development

Za svakodnevni razvoj preporučuje se sledeći setup:

| Servis | Način pokretanja |
|--------|------------------|
| PostgreSQL | Docker |
| Backend | IntelliJ |
| Frontend | npm run dev |

### PostgreSQL

```bash
docker compose up -d postgres
```

### Backend

Pokrenuti iz IntelliJ:

```
PgDavidovErpApplication
```

Adresa:

```
http://localhost:8090
```

### Frontend

```bash
cd frontend
npm install
npm run dev
```

Adresa:

```
http://localhost:5173
```

Napomena:

Docker backend i Docker frontend nisu predviđeni za svakodnevni razvoj, jer Docker frontend automatski pokreće Docker backend koji zauzima port 8090.

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
