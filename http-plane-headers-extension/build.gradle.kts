plugins {
    `java-library`
    id("application")
    id("maven-publish")
    id("signing")
    alias(libs.plugins.shadow)
}

group = "org.eclipse.edc"
version = "1.0.0"

tasks.register<Jar>("sourcesJar") {
    from(sourceSets.getByName("main").allSource)
    archiveClassifier = "sources"
}

tasks.register<Jar>("javadocJar") {
    from(tasks.named("javadoc"))
    archiveClassifier = "javadoc"
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifact("build/libs/http-plane-headers-extension.jar")
            artifact(tasks.named("javadocJar"))
            artifact(tasks.named("sourcesJar"))

            signing {
                sign(publishing.publications["mavenJava"])
            }

            pom {
                developers {
                    developer {
                        name = "Ferre"
                        email = "Ferre@users.noreply.github.com"
                        organization = "Cegeka"
                        organizationUrl = "http://www.sonatype.com"
                    }
                    developer {
                        name = "Jonas"
                        email = "Jonas@users.noreply.github.com"
                        organization = "Cegeka"
                        organizationUrl = "http://www.sonatype.com"
                    }
                    developer {
                        name = "Pieter-Jan"
                        email = "Pieter-Jan@users.noreply.github.com"
                        organization = "Cegeka"
                        organizationUrl = "http://www.sonatype.com"
                    }
                }
                licenses {
                    license {
                        name = "EUPL"
                        url = "https://eupl.eu/1.2/en"
                    }
                }
                scm {
                    connection = "scm:git:git@github.com:Informatievlaanderen/VSDS-Dataspace-Connector.git"
                    developerConnection = "scm:git:git@github.com:Informatievlaanderen/VSDS-Dataspace-Connector.git"
                    url = "git@github.com:Informatievlaanderen/VSDS-Dataspace-Connector.git"
                    tag = "HEAD"
                }
            }
        }
    }
    repositories {
        maven {
            name = "ossrh"
            url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots")
            credentials {
                username = System.getenv("OSSRH_USERNAME")
                password = System.getenv("OSSRH_PASSWORD")
            }
            mavenContent {
                snapshotsOnly()
            }
        }
        maven {
            name = "ossrh"
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = System.getenv("OSSRH_USERNAME")
                password = System.getenv("OSSRH_PASSWORD")
            }
            mavenContent {
                releasesOnly()
            }
        }
    }
}


repositories {
    mavenCentral()
}

dependencies {

    implementation(libs.edc.data.plane.util)
    implementation(libs.edc.util)
    implementation(libs.edc.jersey.core)

    implementation(libs.edc.control.plane.core)
    implementation(libs.edc.dsp)
    implementation(libs.edc.configuration.filesystem)
    implementation(libs.edc.vault.filesystem)
    implementation(libs.edc.iam.mock)
    implementation(libs.edc.management.api)
    implementation(libs.edc.transfer.data.plane)
    implementation(libs.edc.transfer.pull.http.dynamic.receiver)

    implementation(libs.edc.control.plane.api.client)
    implementation(libs.edc.data.plane.http.spi)

    implementation(libs.edc.data.plane.selector.api)
    implementation(libs.edc.data.plane.selector.core)
    implementation(libs.edc.data.plane.selector.client)

    implementation(libs.edc.data.plane.core)

}

application {
    mainClass.set("$group.boot.system.runtime.BaseRuntime")
}

var distTar = tasks.getByName("distTar")
var distZip = tasks.getByName("distZip")

tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    mergeServiceFiles()
    archiveFileName.set("http-plane-headers-extension.jar")
    archiveBaseName.set("http-plane-headers-extension")
    dependsOn(distTar, distZip)
}

tasks.named("publishMavenJavaPublicationToOssrhRepository") {
    dependsOn("shadowJar")
}