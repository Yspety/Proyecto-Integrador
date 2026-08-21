# ─── Etapa de compilación ────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# El pom se copia solo primero: mientras las dependencias no cambien, Docker
# reusa esta capa y no vuelve a bajar medio Maven Central en cada build.
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
# Sin tests: ProyectoIntegradorApplicationTests levanta el contexto completo y
# necesita MySQL, que en esta etapa no existe. Los tests se corren aparte.
RUN mvn -B clean package -DskipTests

# ─── Imagen final ────────────────────────────────────────────────────────────
# Solo el JRE: la imagen resultante pesa una fracción de la de compilación,
# y no lleva Maven ni el código fuente adentro.
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

# Punto de montaje de las imágenes de producto. Va como volumen en compose para
# que sobrevivan a un rebuild del contenedor.
RUN mkdir -p /app/uploads
VOLUME ["/app/uploads"]

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
