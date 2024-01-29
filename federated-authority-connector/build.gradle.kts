/*
 *  Copyright (c) 2022 Microsoft Corporation
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Microsoft Corporation - initial API and implementation
 *
 */

plugins {
    `java-library`
    id("application")
    alias(libs.plugins.shadow)
}

dependencies {
    runtimeOnly(libs.edc.catalog.core)
    runtimeOnly(libs.edc.catalog.api)
    implementation(libs.edc.catalog.spi)

    // Filesystem config
    implementation(libs.edc.configuration.filesystem)
    implementation(libs.edc.vault.filesystem)

    implementation(libs.edc.util)
    runtimeOnly(libs.edc.spi.jsonld)

    runtimeOnly(libs.bundles.edc.connector)
    runtimeOnly(libs.edc.control.plane.core)
    runtimeOnly(libs.edc.data.plane.selector.core)

    // IDS stuff
    runtimeOnly(libs.edc.dsp)

    // Identity Hub
    runtimeOnly(libs.bundles.identity)
    runtimeOnly(libs.ih.core.verifier)
    runtimeOnly(libs.ih.ext.api)
    runtimeOnly(libs.ih.ext.credentials.jwt)
    runtimeOnly(libs.ih.ext.verifier.jwt)

    // Registration service
    runtimeOnly(libs.rs.core)
    runtimeOnly(libs.rs.core.credential.service)
    runtimeOnly(libs.rs.ext.api)
}

application {
    mainClass.set("org.eclipse.edc.boot.system.runtime.BaseRuntime")
}

var distTar = tasks.getByName("distTar")
var distZip = tasks.getByName("distZip")

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    mergeServiceFiles()
    archiveFileName.set("federated-authority-connector.jar")
    dependsOn(distTar, distZip)
}