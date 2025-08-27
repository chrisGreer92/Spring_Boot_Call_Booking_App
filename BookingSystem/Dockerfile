FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S app && adduser -S app -G app

WORKDIR /app

COPY target/*.jar app.jar

USER app

ENTRYPOINT ["java","-jar","/app/app.jar"]