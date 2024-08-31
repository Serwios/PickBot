FROM maven:3.9.6-sapmachine-21 as build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY . .
RUN mvn clean package -DskipTests

FROM openjdk:21-slim
WORKDIR /app
COPY --from=build /app/target/pickbot-0.0.1-SNAPSHOT.jar pickbot.jar
EXPOSE 8080
ENTRYPOINT ["java", "--enable-preview", "-jar", "pickbot.jar"]