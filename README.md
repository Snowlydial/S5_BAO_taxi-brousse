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

Built during Semester 5 at IT University as a business application and object-oriented design project.
