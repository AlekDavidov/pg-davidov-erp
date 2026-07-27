> This document evolves together with the project.
> Any architectural decision that affects future development must be documented here before implementation.
> 
> # PG Davidov ERP
## Architecture & Development Standards

Version: 1.0

---

# 1. Purpose

PG Davidov ERP is a business application for managing agricultural operations.

The project is designed to be:

- Simple for everyday users.
- Easy to maintain.
- Easy to extend.
- Consistent across all modules.
- Ready for future growth.

Every architectural decision should follow the standards defined in this document.

---

# 2. Golden Rules

## Rule 1 – No duplicated data

The same business information must exist only once.

Every piece of information has a single source of truth.

---

## Rule 2 – Every entity has its own ID

Every entity must have:

- Technical database ID (Primary Key)
- Business ID (when applicable)

Example:

| Database ID | Business ID |
|-------------|-------------|
| 15 | SUP0015 |

Database IDs are used for relations.

Business IDs are shown to users.

---

## Rule 3 – Controlled selections

Whenever users select existing data, they must use predefined values.

Allowed:

- Dropdown
- Autocomplete
- Search dialog

Not allowed:

- Free text when a predefined value exists.

Examples:

- Supplier
- Category
- Currency
- Payment Method
- Bank Account

---

## Rule 4 – Automatic data population

If the system already knows a value, the user should never type it manually.

Examples:

- Supplier ID
- Category ID
- Invoice Status
- Balance
- Last Payment Date

---

## Rule 5 – No duplicated business logic

A business rule must exist only once.

Examples:

- Status calculation
- Balance calculation
- Validation
- Tax calculation

Business logic must never be copied into multiple classes.

---

## Rule 6 – Relations are stored using IDs

The database stores only IDs.

The frontend always displays readable values.

Example:

Database:

supplier_id = 5

Frontend:

Dobavljač = Elektrodistribucija Srbije

---

## Rule 7 – English backend, Serbian frontend

Backend uses English.

Frontend uses Serbian.

### Backend

- Database
- Java code
- API
- DTOs
- Enums
- Documentation
- Comments

### Frontend

- Labels
- Menus
- Buttons
- Validation messages
- Forms

Example:

| Backend | Frontend |
|----------|----------|
| Supplier | Dobavljač |
| Invoice | Faktura |
| Payment Method | Način plaćanja |
| Registration Number | Matični broj |

---

## Rule 8 – Business logic never depends on labels

Business logic must use:

- IDs
- Codes
- Enums

Never labels.

Correct:

payment_method_id = 2

or

BANK_TRANSFER

Incorrect:

"Bankovni prenos"

Labels may change.

Business logic must never break because text changed.

---

# 3. Naming Standards

## Database

- English only
- snake_case

Examples:

suppliers

invoice_items

payment_methods

created_at

updated_at

supplier_id

---

## Java

- English only
- PascalCase for classes
- camelCase for variables

Examples:

SupplierController

SupplierService

paymentMethod

createdAt

---

## REST API

Plural resources.

Examples:

GET /api/suppliers

POST /api/suppliers

PUT /api/suppliers/{id}

DELETE /api/suppliers/{id}

---

# 4. Database Standards

Database engine:

- PostgreSQL

Schema management:

- Flyway

Every database change must be introduced through a new Flyway migration.

Existing migrations should never be modified after deployment.

Every table should contain:

- id
- created_at
- updated_at

Whenever business history is important:

Use Active/Inactive instead of physical delete.

---

# 5. Package Structure

The backend follows Feature-Based Architecture.

Example:

supplier/

controller/

service/

repository/

entity/

dto/

mapper/

Each module owns its implementation.

Only shared technical functionality belongs inside the common package.

---

# 6. API Standards

Entities are never exposed directly.

The API communicates only through DTOs.

Validation belongs to the backend.

Errors must have a consistent response format.

---

# 7. Frontend Standards

The frontend communicates only through the REST API.

It never talks directly to the database.

Frontend rules:

- Serbian labels
- Readable values instead of IDs
- Consistent forms
- Consistent validation
- Consistent colors
- Responsive layout

The frontend should be prepared for future multilingual support.

---

# 8. Git Standards

Never develop directly on main.

Feature branches:

feature/suppliers

feature/categories

feature/invoices

Commit messages follow Conventional Commits.

Examples:

feat: Add supplier management

fix: Prevent duplicate invoice numbers

docs: Update architecture document

refactor: Simplify supplier service

chore: Update dependencies

---

# 9. Development Workflow

Every new module follows the same order.

1. Business analysis
2. Database design
3. Flyway migration
4. Entity
5. Repository
6. Service
7. DTO
8. Mapper
9. Controller
10. API testing
11. Frontend implementation
12. Functional testing
13. Visual improvements

Core functionality always has priority over visual appearance.

---

# 10. Future Goals

The architecture should support future extensions without major refactoring.

Examples:

- Multiple organizations
- Multiple users
- Role-based permissions
- Multiple languages
- Mobile application
- Reporting
- AI assistant
- Public API