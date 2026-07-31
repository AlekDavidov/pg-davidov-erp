# PG Davidov ERP

ERP sistem za upravljanje poljoprivrednim gazdinstvom.

## Tehnologije

- Java 21
- Spring Boot
- PostgreSQL
- Flyway
- Vue 3
- PrimeVue
- Docker
- Docker Compose

---

# Pokretanje projekta

## Preduslovi

Potrebno je samo:

- Docker Desktop

Nije potrebno lokalno instalirati:

- Java
- Maven
- Node.js
- PostgreSQL

---

## Prvo pokretanje

Kopirati konfiguraciju:

```bash
copy .env.example .env
```

Pokrenuti aplikaciju:

```bash
start.bat
```

Prilikom prvog pokretanja Docker će preuzeti potrebne image-e i napraviti bazu.

---

## Adrese

Frontend

```
http://localhost
```

Backend API

```
http://localhost:8090
```

Swagger

```
http://localhost:8090/swagger-ui/index.html
```

Health

```
http://localhost:8090/actuator/health
```

PgAdmin (opciono)

```
http://localhost:5050
```

Pokretanje:

```bash
docker compose --profile tools up -d pgadmin
```

---

# Zaustavljanje

```bash
stop.bat
```

---

# Backup baze

```bash
backup.bat
```

Backup se čuva u:

```
backups/
```

---

# Lokalni razvoj

Za razvoj se preporučuje sledeći način rada:

| Servis | Pokretanje |
|--------|------------|
| PostgreSQL | Docker |
| Backend | IntelliJ |
| Frontend | Vite (`npm run dev`) |

### PostgreSQL

```bash
docker compose up -d postgres
```

### Backend

Pokrenuti iz IntelliJ:

```
PgDavidovErpApplication
```

Backend:

```
http://localhost:8090
```

### Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend:

```
http://localhost:5173
```

---

# Docker razvoj

Kompletna aplikacija može da radi i potpuno kroz Docker:

```bash
start.bat
```

ili

```bash
docker compose up --build -d
```

---

# Trenutno implementirano

- Spring Boot backend
- PostgreSQL
- Flyway migracije
- Docker Compose
- Suppliers CRUD
- Transactions CRUD
- Categories CRUD
- Payment Methods CRUD
- Bank Accounts CRUD

---

# Sledeći koraci

- Invoices
- Documents
- Dashboard
- Reports
- Bank Import
- Settings