# S5_BAO_taxi-brousse

A Spring Boot web application for managing intercity bus (taxi-brousse) operations in Madagascar. It covers the full lifecycle from scheduling routes and assigning buses, to passenger reservations, dynamic fare calculation, payment processing, and advertising revenue.

Built as a database-oriented project, the focus was on modeling realistic business rules — multi-dimensional pricing, age-based discounts, seat class management — and exposing them through a clean Thymeleaf interface.

## Features

### What it does

- Bus and route management: define voyages between stations (gares), assign buses to specific departures
- Seat class configuration (Standard, Premium, VIP) with per-class pricing
- Age-based fare rules: child and senior discounts applied automatically on top of base fares
- Passenger reservation workflow: seat selection, booking confirmation, and status tracking
- Multi-method payment: cash, Mobile Money, and card, logged through a caisse system
- Invoice generation (facture + facture lines) with payment history
- Advertising diffusion: companies can book ad slots on specific voyages with advance payments tracked
- Price history: all fare changes are logged for audit purposes

### Why this project matters

- It models the kind of pricing logic that looks straightforward on paper but gets complicated fast (overlapping rules, class-specific overrides, age groups)
- The database schema had to handle many-to-many relationships between buses, routes, and time slots without allowing impossible configurations
- Invoice and payment tracking added a second domain on top of the reservation system

## Screenshots

### Tableau de Bord

![Dashboard with revenue charts and demographics](docs/screenshots/Dashboard.png)

Central dashboard displaying real-time statistics: global revenue evolution, revenue by caisse (payment method), top 5 most reserved routes, top 10 most profitable clients, and passenger demographics by gender and age group. Period and revenue type filters allow dynamic analysis.

### Rechercher Voyage

![Voyage search criteria and results](docs/screenshots/RechercheVoyage.png)

Search interface for finding available bus routes by departure/arrival stations, date, and time. Results display bus details, available seat classes, and direct booking access.

### Nouvelle Réservation
#### Reservation Booking Flow (Step 1)

![Reservation creation with voyage and client selection](docs/screenshots/NewReservation_1.png)

Booking workflow: voyage details display, client selection, and reservation date/time configuration.

#### Seat Selection & Payment (Step 2)

![Seat selection grid and payment method configuration](docs/screenshots/NewReservation_2.png)

Continuation of booking: individual seat selection with visual grid layout, batch seat application, and multi-method payment (cash, Mobile Money, card).

### Liste Réservations

![Reservations list with filters and summary cards](docs/screenshots/ReservationList.png)

Complete view of all passenger reservations with revenue summaries. Filterable by client, bus, route, date, status, and seat class. Displays pricing and payment information per reservation.

### Gestion Bus

![Bus list showing configurations and revenue](docs/screenshots/BusList.png)

Fleet overview with bus immatriculation, configured seat classes, total capacity, and maximum revenue potential. Each bus card shows all active configurations.

### Ajouter Bus

![Bus creation form with place configuration](docs/screenshots/BusCreation.png)

Add new buses with unique immatriculation number. Configure seat types (Standard, Premium, VIP) with their quantities (each type has their own price, only editable via SQL so far).

### Bus Configuration

![Bus configuration list with types and values](docs/screenshots/BusConfList.png)

Define bus seat class types available across the fleet. Edit or delete configurations with constraints on deletion if buses are using them.

### Trajets/Voyages

![Routes list with duration and pricing](docs/screenshots/TrajetList.png)

Define available travel routes between stations. Each route has departure/arrival stations, duration, and base price.

### Gestion Bus-Voyage

![Bus-voyage assignments with filters and date scheduling](docs/screenshots/BusVoyageList.png)

Assign specific buses to specific routes with date/time scheduling. Manage voyage plannings by bus, route, and departure gates.

### Liste Diffusions

![Diffusion list with price config, payments, and remaining balance](docs/screenshots/DiffusionList.png)

Track all advertising slots with their configured pricing, paid amounts, and remaining balances. Filter by society, bus, route, date, and payment status.

### Configuration des Tarifs Diffusion

![Diffusion tariff configuration with date ranges](docs/screenshots/DiffusionTarifConf.png)

Define advertising pricing configurations with date ranges and price per slot. Prevent overlapping date periods to ensure consistent pricing.

### Liste Factures

![Invoices with summary metrics and line details](docs/screenshots/FactureList.png)

Generated invoices combining reservation revenue, advertising revenue (diffusions), and product orders. Summary cards display revenue breakdowns and remaining balances.

### Détails Facture

![Invoice detail showing reservations, diffusions, and products](docs/screenshots/FactureDetail.png)

Full invoice breakdown with separate sections for reservations, diffusions, and products. Displays per-line pricing and payment status with subtotals.

### Commandes Produits

![Product commands list interface](docs/screenshots/ProduitCommandeList.png)

View all product orders for societies on specific bus-voyages. Tracks orders and integrates into facture totals.

### Ajouter Commande Produit

![Product command creation form](docs/screenshots/ProduitCommandeAdd.png)

Add products to specific bus-voyages with unit pricing and quantity configuration.

## Tech Stack


- Backend: Java + Spring Boot
- Frontend: Thymeleaf
- Database: PostgreSQL
- Build: Maven (wrapper included)


## Database

The schema is in `sql/Taxi_brousse.sql`. A simplified dataset for testing is in `sql/dataV4-simple.sql`.

Main entities: `Bus`, `Voyage`, `BusVoyage`, `Gare`, `Reservation`, `Client`, `Paiement`, `ClassePlace`, `ClasseAge_Conf`, `Facture`, `Diffusion`

## Getting Started

### Prerequisites

- Java 21+
- Maven 3.9+ (optional if using wrapper)
- PostgreSQL

### Local setup

```bash
# 1) Clone the repository
git clone https://github.com/Snowlydial/S5_BAO_taxi-brousse.git
cd S5_BAO_taxi-brousse

# 2) Initialize the database
cd sql
psql -U postgres -f Taxi_brousse.sql

# Optional: load sample data
psql -U postgres -f dataV4-simple.sql
cd ..

# 3) Configure your database connection
# Edit src/main/resources/application.properties:
# spring.datasource.url=jdbc:postgresql://localhost:5432/your_db
# spring.datasource.username=your_user
# spring.datasource.password=your_password

# 4) Run the application
# Windows
./mvnw.cmd spring-boot:run

# macOS/Linux
./mvnw spring-boot:run
```

The application will be available at http://localhost:8080

## Academic context

Built during Semester 5 at university as a business application, serving as a practical exercise in team leadership and project management.