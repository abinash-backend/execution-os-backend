FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

RUN sed -i 's/\r$//' mvnw && chmod +x mvnw

COPY src/ src/

RUN ./mvnw clean package -DskipTests


FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

COPY --from=build /workspace/target/*.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
