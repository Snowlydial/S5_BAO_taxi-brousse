# Prerequisites
- Maven
- JDK21
- PostgresSQL

Don't forget to change the DB connection lines in ```src/main/resources/application.properties```

# Run the program
```bash
mvn spring-boot:run
```

Execute the sql script:
```bash
cd sql

psql -U postgres -f Taxi_brousse.sql
```