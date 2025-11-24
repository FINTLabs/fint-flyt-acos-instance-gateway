plugins {
    id("org.springframework.boot") version "3.5.7"
    id("io.spring.dependency-management") version "1.1.7"
    java
    id("com.github.ben-manes.versions") version "0.53.0"
}

group = "no.novari"

val apiVersion: String by project

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
    mavenLocal()
    maven {
        url = uri("https://repo.fintlabs.no/releases")
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.kafka:spring-kafka")

    compileOnly("org.projectlombok:lombok")
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.projectlombok:lombok")

    implementation("no.fintlabs:fint-model-resource:0.5.0")
    implementation("no.fint:fint-arkiv-resource-model-java:$apiVersion")
    implementation("no.fint:fint-administrasjon-resource-model-java:$apiVersion")

    implementation("no.novari:flyt-resource-server:6.0.0-rc-26")
    implementation("no.novari:kafka:5.0.0-rc-20")
    implementation("no.novari:flyt-cache:2.0.0-rc-2")
    implementation("no.novari:flyt-instance-gateway:7.0.0-rc-8")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    isEnabled = false
}
