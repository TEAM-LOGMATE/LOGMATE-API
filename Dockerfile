FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY build/libs/*.jar app.jar
COPY src/main/resources/keystore.p12 keystore.p12
ENTRYPOINT ["java","-jar","/app/app.jar"]