FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml ./
COPY src ./src
RUN mvn -B -q -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app

RUN apt-get update && apt-get install -y ffmpeg && rm -rf /var/lib/apt/lists/*

COPY --from=build /workspace/target/VideoGenaration-0.0.1-SNAPSHOT.jar /app/app.jar

EXPOSE 8091

ENTRYPOINT ["java","-jar","/app/app.jar"]
