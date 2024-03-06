plugins {
    `java-library`
    id("application")
    alias(libs.plugins.shadow)
    id("com.google.cloud.tools.jib") version "3.4.1"
}

group = "org.eclipse.edc"

repositories {
    mavenCentral()
}

dependencies {

    implementation(libs.edc.data.plane.util)
    implementation(libs.edc.util)
    implementation(libs.edc.jersey.core)

    implementation(libs.edc.control.plane.core)
    implementation(libs.edc.dsp)

    // Filesystem config
    implementation(libs.edc.configuration.filesystem)
    implementation(libs.edc.vault.filesystem)

    // DID Web
    runtimeOnly(libs.bundles.identity)

    // Identity Hub
    runtimeOnly(libs.ih.core.verifier)
    runtimeOnly(libs.ih.ext.api)
    runtimeOnly(libs.ih.ext.credentials.jwt)
    runtimeOnly(libs.ih.ext.verifier.jwt)


    implementation(libs.edc.management.api)
    implementation(libs.edc.transfer.data.plane)
    implementation(libs.edc.transfer.pull.http.dynamic.receiver)

    implementation(libs.edc.control.plane.api.client)
    implementation(libs.edc.data.plane.http.spi)

    implementation(libs.edc.data.plane.selector.api)
    implementation(libs.edc.data.plane.selector.core)
    implementation(libs.edc.data.plane.selector.client)

    implementation(libs.edc.data.plane.core)
//    must not be added so our implementation is used by the dataplane
//    implementation(libs.edc.data.plane.api)
//    implementation(libs.edc.data.plane.http)

}

application {
    mainClass.set("$group.boot.system.runtime.BaseRuntime")
}

var distTar = tasks.getByName("distTar")
var distZip = tasks.getByName("distZip")

var appName = "http-pull"

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    mergeServiceFiles()
    archiveFileName.set("$appName.jar")
    dependsOn(distTar, distZip)
}

afterEvaluate {
    tasks.named("build") {
        dependsOn("jibDockerBuild")
    }
}

jib {
    from {
        image = "openjdk:21-ea-bullseye"
    }
    to {
        image = "vsds-dataspace-connector/$appName"
        tags = setOf("local")
    }
    container {
        mainClass = application.mainClass.get()
        args = listOf("-Dedc.keystore=\$EDC_KEYSTORE", "-Dedc.keystore.password=\$EDC_KEYSTORE_PASSWORD", "-Dedc.vault=\$EDC_VAULT", "-Dedc.fs.config=\$EDC_FS_CONFIG")
    }
}