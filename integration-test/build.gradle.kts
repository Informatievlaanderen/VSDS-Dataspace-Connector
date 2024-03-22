plugins {
    java
    id("org.springframework.boot") version "3.2.3"
    id("io.spring.dependency-management") version "1.1.4"
}

group = "be.vlaanderen.informatievlaanderen.ldes.connector"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        url = uri("https://repo.danubetech.com/repository/maven-public/")
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")

    implementation("org.springframework.boot:spring-boot-testcontainers")
    implementation("org.testcontainers:mongodb")

    implementation("org.apache.jena:jena-core:4.10.0")
    implementation("org.apache.jena:jena-arq:4.10.0")

    implementation("com.auth0:java-jwt:4.4.0")
    implementation("org.web3j:crypto:5.0.0")
    implementation("decentralized-identity:did-common-java:1.11.0")
    implementation("io.fusionauth:fusionauth-jwt:5.3.2")


    testImplementation("org.testcontainers:nginx:1.19.6")
    testImplementation("io.cucumber:cucumber-java:6.10.4")
    testImplementation("io.cucumber:cucumber-junit:6.10.4")
    testImplementation("org.junit.vintage:junit-vintage-engine:5.7.2")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.testcontainers:junit-jupiter")

}

springBoot {
    mainClass.set("connector.LdesServerContainer")
}

tasks.withType<Test> {
    useJUnitPlatform()
}