# Introduction
This project is a simple Spring Boot trading system that lets users manage traders and accounts, store quotes,
and submit market orders. It pulls market data using the Finnhub API, caches quote data in PostgreSQL, and
gives REST API endpoints for CRUD trading operations. The project architecture is layered (Controller → Service → DAO) 
to keep responsibilities isolated and maintainable. This app is built using with Java 8, Spring Boot, JPA/Hibernate, and Apache HttpClient for sending API requests to Finnhub API. Testing used JUnit, Mockito for mocks, and Apache DBCP to create H2 based DataSource objects. Swagger UI support is also there for simple manual HTTP testing. Development has been done using Maven and IntelliJ IDE with additional deployment being added by the usage of Docker for containerisation.

# Quick Start

0) Clone the project and go into the project directory using `cd springboot`
## Options 1 (Local):
1) Start a local PostgreSQL instance and create the `trading_app` database:
```bash
psql -U postgres -c "CREATE DATABASE trading_app;"
```
2) Initialize schema:
```bash
psql -U postgres -d trading_app -f sql/trading_schema.sql
```
3) Configure credentials:
   - Update `src/main/resources/env.properties` for DB settings, and
     `src/main/resources/application.properties` for `marketdata.token`
     (or export `PSQL_*` and `FINNHUB_TOKEN` env vars instead).
4) Run the app:
```bash
mvn -f pom.xml spring-boot:run
```
5) (Optional) Build the runnable jar:
```bash
mvn -f pom.xml clean package -DskipTests
java -jar target/trading-1.0-SNAPSHOT.jar
```
5) Open Swagger UI at `http://localhost:8080/swagger-ui.html`.

## Options 2 (Docker):
Docker files:
- `psql/Dockerfile`: builds the `trading-psql` image with schema initialization.
- `Dockerfile`: multi-stage build for the `trading-app` image.

1) Build images:
```bash
cd psql
docker build -t trading-psql .

cd ../
docker build -t trading-app .
```

2) Create a docker network:
```bash
docker network create trading-net
```

3) Start containers:
```bash
# Postgres container (schema is initialized by init.sql)
docker run --name trading-psql \
  -e POSTGRES_PASSWORD=password \
  -e POSTGRES_DB=trading_app \
  -e POSTGRES_USER=postgres \
  --network trading-net \
  -d trading-psql

# Trading app (replace FINNHUB_TOKEN)
docker run --name trading-app \
  -e "PSQL_URL=jdbc:postgresql://trading-psql:5432/trading_app" \
  -e "PSQL_USER=postgres" \
  -e "PSQL_PASSWORD=password" \
  -e "FINNHUB_TOKEN=your_token" \
  --network trading-net \
  -p 8080:8080 -t trading-app
```

4) Try trading-app with Swagger UI at `http://localhost:8080/swagger-ui.html`

# Implementation
## Architecture
```
 +--------------------------------------------------------------------------------------+
|                                  SPRING BOOT APPLICATION                              |
|                                                                                       |
|  +-----------------------+      +-------------------+      +----------------------+   |
|  |     Controller(s)     | ---> |    Service(s)     | ---> |       DAO(s)         |   |
|  |  (@RestController)    |      |    (@Service)     |      |   (@Repository)      |   |
|  +-----------------------+      +-------------------+      +----------------------+   |
|                                          |                          |                 |
|                                          |                          v                 |
|                                          |                 +--------------------+     |
|                                          |                 |   PostgreSQL DB     |    |
|                                          |                 | (psql via JDBC/ORM) |    |
|                                          |                 +--------------------+     |
|                                          |                                            |
|                                          v                                            |
|                                 +-------------------+                                 |
|                                 |    HTTP Client    |                                 |
|                                 +-------------------+                                 |
+------------------------------------------|--------------------------------------------+
                                           v                                            
                                     +------------+                                     
                                     | Finnhub API|                                     
                                     +------------+                                    
                                                                                      

                
                
+------------------------+    HTTP (request/response)    +-------------------------+
|      HTTP Client       | <----------------------------> |   Apache Tomcat Server |
|  (Browser / Postman /  |                                | (embedded or external) |
|   Mobile / Script)     |                                +------------------------+
+------------------------+                                            |
                                                                      v
                                                         +----------------------------+
                                                         |      Web Servlet Layer     |
                                                         | (Spring DispatcherServlet) |
                                                         +----------------------------+
                                                                      |
                                                                      v
                                                           (routes to Controller)

```

Component overview:
- Controller layer: Receives HTTP requests, validates inputs, and maps exceptions to HTTP status
  codes. Controllers are thin and delegate all business logic to services.
- Service layer: Orchestrates workflows such as market data refresh, trader/account management, and
  order execution. This is where validation, business rules, and conversions happen.
- DAO layer: Encapsulates persistence operations using Spring Data JPA repositories and JDBC
  access. DAOs provide CRUD, lookups, and aggregate queries against PostgreSQL.
- Spring Boot (Tomcat/WebServlet, IoC): The embedded Tomcat server handles HTTP requests and
  routes them to controllers. Spring IoC wires beans and manages lifecycle and configuration.
- PSQL and Finnhub: PostgreSQL stores traders, accounts, quotes, and orders, while Finnhub is the
  external market data provider used to refresh quote data.

## REST API Usage
### Swagger
Swagger is an API documentation framework that generates interactive docs from code annotations.
It helps developers and testers explore endpoints and try requests without writing clients.

### Quote Controller
This controller exposes endpoints to fetch live quotes from Finnhub and manage cached quote data
in the database.
- `GET /quote/fh/ticker/{ticker}`: fetch a live Finnhub quote by ticker.
- `PUT /quote/fhMarketData`: refresh quote table using Finnhub market data.
- `PUT /quote/`: upsert a quote row (manual update).
- `POST /quote/tickerId/{tickerId}`: add a ticker to the quote table using market data.
- `GET /quote/dailyList`: list all quotes in the quote table.

### Trader Controller
Manages traders and their accounts, including deposits and withdrawals.
- `POST /trader/`: create a trader + account using a JSON body.
- `POST /trader/firstname/{firstname}/lastname/{lastname}/dob/{dob}/country/{country}/email/{email}`:
  create a trader using path parameters.
- `DELETE /trader/traderId/{traderId}`: delete trader if balance is zero and no open positions.
- `PUT /trader/deposit/traderId/{traderId}/amount/{amount}`: deposit funds.
- `PUT /trader/withdraw/traderId/{traderId}/amount/{amount}`: withdraw funds.

### Order Controller
Submits market orders and persists security orders.
- `POST /order/marketOrder`: submit a BUY or SELL market order.

# Test
Tests use JUnit for integration tests and unit tests for the service and DAO classes. Mockito is used in
unit tests to create mocks to not create dependencies. Integration tests use an temporary H2 database with `schema.sql`. 
Code line coverage was above 70% for all the tested classes.

# Deployment
The app can be run using Docker as shown prior which allows the app to be distributed much more easily.
The two containers msut be created using the images below but note a shared network must be created for the two docker containers to communicate with each other.

Image details:
- `trading-psql`: built from `postgres:9.6-alpine`. It runs `psql/init.sql` during
  container initialization to create the `trading_app` schema and tables.
- `trading-app`: multi-stage build that compiles the Spring Boot app with Maven and runs the
  packaged jar on a JRE base image. It connects to PostgreSQL via env vars
  (`PSQL_URL`, `PSQL_USER`, `PSQL_PASSWORD`) and expects `FINNHUB_TOKEN` for market data.

Other than that you can build the jar file for the project to share it with others though a local psql database must be still made.

# Improvements
- Add a proper user friendly dashboard so that app can be used by everyone.
- Add rate-limit handling as Finnhub's free API calls are limited.
- Add authentication so that not everyone can access all trader accounts.
