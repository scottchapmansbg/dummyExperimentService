plugins {
    java
    id("org.springframework.boot") version "3.2.3"
    id("io.spring.dependency-management") version "1.1.4"
}

group = "com.experiments"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-graphql")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework:spring-context:6.1.4")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.16.1")
    implementation ("com.fasterxml.jackson.core:jackson-core:2.16.1")
    implementation ("com.fasterxml.jackson.core:jackson-databind:2.16.1")
    implementation ("com.fasterxml.jackson.core:jackson-annotations:2.16.1")
    implementation ("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.16.1")
    implementation("org.springframework:spring-context-support:6.1.4")
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")
    implementation("commons-codec:commons-codec:1.15")

    compileOnly("org.projectlombok:lombok")

    developmentOnly("org.springframework.boot:spring-boot-devtools")

    annotationProcessor("org.projectlombok:lombok")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.springframework.graphql:spring-graphql-test")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
