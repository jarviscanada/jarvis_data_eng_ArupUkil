# Introduction
This app is a CLI Java stock trading platform that allows users to check real time stock information using the Alpha Vantage API and then potentially buy/sell stock positions that's saved through a PostgresSQL database. This app has been built using Core Java features, SLF4J for logging, JDBC to connect to the PSQL database, OkHttp to send HTTP requests/responses, and JUnit/Mockito/TestContainers for testing. Project architecture was a key focus with the usage of DAO and REST architecture, and Jackson Mixins so the app isn't dependent on the Alpha Vantage API. Development has been done using Maven and IntelliJ IDE with additional deployment being added by the usage of Docker for containerisation.

# Quick Start
## Building the App
0. Clone the project and go into the project directory using ```cd core_java/stockquote```
1. Compile and build the project using Maven (note need Docker downloaded or else test cases won't pass)
```
mvn clean package
```

### Option 1 (Local):
0. Have psql downloaded
1. Create the local psql database
```
psql -h localhost -U postgres -d postgres -f ./sql/ddl.sql
```
2. Run the build
```
java -jar target/stockquote-1.0-SNAPSHOT.jar
```
### Option 2 (Docker):
1. Create the database and app Docker containers and connect them using the Dockerfile and `docker-compose.yml` files
```
docker compose up -d
```
2. Run the program in the container
```
docker compose run --rm --use-aliases app
```

# Implementaiton
## ER Diagram
This following database is defined through the sql/ddl.sql file which also matches the ddl.sql file in the resources directory in the src/test folder.

```md
+------------------------------------+         +--------------------------------+
|                quote               |         |             position           |
+------------------------------------+         +--------------------------------+
| symbol (PK)        VARCHAR(10)     |<--------| symbol (PK/FK)   VARCHAR(10)   |
| open               DECIMAL(10,2)   |         | number_of_shares INT           |
| high               DECIMAL(10,2)   |         | value_paid       DECIMAL(10,2) |
| low                DECIMAL(10,2)   |         +--------------------------------+
| price              DECIMAL(10,2    |
| volume             INT             |
| latest_trading_day DATE            |
| previous_close     DECIMAL(10,2)   |
| change             DECIMAL(10,2)   |
| change_percent     VARCHAR(10)     |
| timestamp          TIMESTAMP       |
+------------------------------------+

Relationship: position.symbol (FK) -> quote.symbol (PK)
Cardinality:  quote 1  ----  0..1 position   (one position row per symbol)
```

## Design Patterns
The entities, which are the core data in the app, are the Quote and Position classes. They fulfill a part of the DTO design pattern which is used for designing how data may look while it's moving around layers. For most of the app, the data is usually just the entity classes but the json data obtained from API calls uses Mixins to help transfer data to entities. Mixins are specifically used as they can be unique to a specific API while keeping the entities generic to any given API.

The DAO design pattern was used as an interface layer to abstract any CRUD operations with the database. This hides all the intricate implementation details for other layers like the service and controller layer. Speaking of those layers, the service layer handles all the business logic of the app while the controller handles all the interactions with the user and app by calling the service operations. The Main class is used to do the initial setup work for the app like get all the properties from the properties file.

# Test
There are unit tests for all classes in the dao and service folder which have at least one test for all methods of those classes. Unit tests are meant to test in an isolated manner without dependencies hence I also have integration tests for the services to test how the services function in a real wiring. The testing has been done mainly using JUnit with help from Mockito for creating mocks for unit testing and TestContainers to create temporary databases. Note that TestContainers does force the need to have docker downloaded and running for the integration tests to work.

# Improvement
1. Currently, selling positions just deletes it in the database and does nothing else. The reason for this is that there is no permanent profit counter for a user so there is no value is calculating the profit of selling a position. So adding the profit value for a user and then selling positions to gain/lose money is a logical extension. This also means adding the ability to specify the number of shares you want to sell from your position.
2. Adding the ability to create different accounts to store different positions.
3. Move from the UI just being the CLI to having a proper GUI.
