# Use a imagem do OpenJDK 21
FROM openjdk:21

# Argumento para receber o JAR na build
ARG JAR_FILE=build/libs/dixton-spring-api-0.0.1-SNAPSHOT.jar

# Copia o JAR para o container
COPY ${JAR_FILE} app.jar

# Exponha a porta 8080
EXPOSE 8080

# Comando de entrada
ENTRYPOINT ["java", "-jar", "/app.jar"]