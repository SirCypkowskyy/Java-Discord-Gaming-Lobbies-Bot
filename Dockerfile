FROM maven:3-amazoncorretto-17 AS builder

WORKDIR /build

COPY ./src /build/src
COPY ./pom.xml /build

RUN mvn -f /build/pom.xml clean package

FROM amazoncorretto:17 AS runner

COPY --from=builder /build/target/bot.jar /app/bot.jar

ENTRYPOINT ["java", "-jar", "/app/bot.jar"]
