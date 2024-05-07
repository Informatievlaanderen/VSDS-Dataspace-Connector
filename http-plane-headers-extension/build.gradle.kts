plugins {
    `java-library`
    id("application")
    `maven-publish`
    signing
    alias(libs.plugins.shadow)
}

group = "org.eclipse.edc"
version = "1.0.0-SNAPSHOT"

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

tasks.register<Jar>("javadocJar") {
    archiveClassifier = "javadoc"
    from(tasks.named("javadoc"))
}

tasks.register<Jar>("sourcesJar") {
    archiveClassifier = "sources"
    from(sourceSets.getByName("main").allJava)
}

publishing {
    publications {
        val publication = create<MavenPublication>("shadowJar") {
            groupId = "be.vlaanderen.informatievlaanderen"
            artifactId = "http-plane-headers-extension"

            artifact(tasks["javadocJar"])
            artifact(tasks["sourcesJar"])

            pom {
                developers {
                    developer {
                        name = "Ferre"
                        email = "Ferre@users.noreply.github.com"
                        organization = "Cegeka"
                        organizationUrl = "https://www.cegeka.com/"
                    }
                    developer {
                        name = "Jonas"
                        email = "Jonas@users.noreply.github.com"
                        organization = "Cegeka"
                        organizationUrl = "https://www.cegeka.com/"
                    }
                    developer {
                        name = "Pieter-Jan"
                        email = "Pieter-Jan@users.noreply.github.com"
                        organization = "Cegeka"
                        organizationUrl = "https://www.cegeka.com/"
                    }
                    developer {
                        name = "Yalz"
                        email = "Yalz@users.noreply.github.com"
                        organization = "Cegeka"
                        organizationUrl = "https://www.cegeka.com/"
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
        project.shadow.component(publication)
    }
    signing {
        val signingKey = if(System.getenv("OSSRH_PGP_PRIVATE_KEY") != null) System.getenv("OSSRH_PGP_PRIVATE_KEY").trimIndent() else ""
        val signingPassword = System.getenv("OSSRH_PGP_SECRET_KEY_PASSPHRASE")
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(publishing.publications["shadowJar"])
    }
    repositories {
        maven {
            val releasesRepoUrl = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            val snapshotsRepoUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots")
            name = "ossrh"
            url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
            credentials {
                username = System.getenv("OSSRH_USERNAME")
                password = System.getenv("OSSRH_PASSWORD")
            }
        }
    }
}

project.tasks.findByName("jar")?.enabled = false

tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    mergeServiceFiles()
    dependsOn(distTar, distZip)
}
