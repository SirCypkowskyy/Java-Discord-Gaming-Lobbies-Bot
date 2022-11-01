/*
 * This file was generated by the Gradle 'init' task.
 */

plugins {
    java
    application
}


repositories {
    mavenLocal()
    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }
}

dependencies {
    implementation("net.dv8tion:JDA:5.0.0-alpha.17")
    implementation("io.github.cdimascio:dotenv-java:2.2.4")
    implementation("org.mongodb:mongo-java-driver:3.12.11")
}

group = "org.sircypkowskyy"
version = "1.0-SNAPSHOT"
description = "Java_GamingLobbiesBot"
java.sourceCompatibility = JavaVersion.VERSION_17

application {
    mainClass.set("org.sircypkowskyy.gaminglobbiesbot.Main")
}
