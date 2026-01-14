FROM eclipse-temurin:8-jre-alpine
COPY target/stock*.jar /usr/local/app/stockquote/lib/stockquote.jar
ENTRYPOINT ["java","-jar","/usr/local/app/stockquote/lib/stockquote.jar","/usr/local/app/stockquote/config/properties.txt"]
