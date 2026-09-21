FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY iam-auth-server/target/*.jar app.jar
EXPOSE 9000
ENTRYPOINT ["java", "-jar", "app.jar"]