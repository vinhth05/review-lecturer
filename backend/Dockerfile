FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app

RUN addgroup --system app && adduser --system --ingroup app app

COPY --from=build /app/target/*.jar app.jar
RUN chown -R app:app /app

USER app
EXPOSE 8080
ENV JAVA_OPTS="-Xms256m -Xmx512m -Dspring.profiles.active=prod"
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]

# basic container healthcheck
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 CMD curl -f http://localhost:8080/actuator/health || exit 1
