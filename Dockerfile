FROM gradle:jdk17

WORKDIR /app

COPY ./src /app/src
COPY ./gradle /app/gradle
COPY ./build.gradle.kts .
COPY ./settings.gradle.kts .

RUN gradle build

CMD ["gradle", ":run"]
