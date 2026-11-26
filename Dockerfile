FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src src
RUN mvn -B -q package -DskipTests -Dspotbugs.skip=true -Ddependency-check.skip=true -Dmaven.javadoc.skip=true

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/tecnicoya-api-*.jar app.jar
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+UseSerialGC -Dfile.encoding=UTF-8"
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
