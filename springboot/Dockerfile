#
# Build stage
#
FROM maven:3.6-jdk-8-slim AS build
COPY src /build/src
COPY pom.xml /build/
RUN mvn -f /build/pom.xml clean package -DskipTests

#
# Package stage
#
FROM eclipse-temurin:8-jre-alpine
COPY --from=build /build/target/trading-app-1.0-SNAPSHOT.jar /usr/local/app/trading/lib/trading_app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/usr/local/app/trading/lib/trading_app.jar"]
