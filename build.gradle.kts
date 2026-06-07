group = "no.nav.syfo"
version = "0.0.45"
description = "Shared Kotlin utility library for iSyfo backend Ktor services."

plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-library`
    `java-test-fixtures`
    `maven-publish`
    alias(libs.plugins.ktlint)
    alias(libs.plugins.test.logger)
    alias(libs.plugins.versions)
}

repositories {
    mavenCentral()
}

dependencies {
    // Exposed in public API — consumers need these on their compile classpath
    api(libs.ktor.client.core)
    api(libs.ktor.server.auth.jwt)
    // BOM keeps all Jackson artifacts (incl. transitive ones) on a single aligned version
    api(platform(libs.jackson.bom))
    api(libs.jackson.module.kotlin)
    api(libs.jackson.datatype.jsr310)

    // Internal — encapsulated behind library functions, not referenced directly by consumers
    implementation(libs.micrometer.core)
    implementation(libs.ktor.client.apache5)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.jackson)

    // Logging facade only; consuming apps own the runtime binding (e.g. logback-classic)
    implementation(libs.slf4j.api)

    // Test fixtures (published as a separate -test-fixtures artifact); version supplied by the Jackson BOM
    testFixturesImplementation(platform(libs.jackson.bom))
    testFixturesImplementation(libs.jackson.annotations)

    // Tests
    testImplementation(libs.ktor.client.mock)
    testImplementation(libs.mockk)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.logback.classic)
    testImplementation(kotlin("test"))
}

kotlin {
    explicitApi()
    jvmToolchain(21)
}

java {
    withSourcesJar()
}

tasks {
    register("printVersion") {
        doLast {
            println(project.version)
        }
    }

    test {
        useJUnitPlatform()
        testlogger {
            showFullStackTraces = true
            showPassed = false
        }
    }
}

tasks.withType<PublishToMavenRepository> {
    doFirst {
        if (System.getenv("GITHUB_ACTIONS") != "true") {
            throw GradleException("Publishing must only be done from GitHub Actions CI.")
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = "no.nav.syfo"
            artifactId = "isyfo-backend-common"
            version = project.version.toString()
            from(components["java"])

            pom {
                name.set("isyfo-backend-common")
                description.set(project.description)
                url.set("https://github.com/navikt/isyfo-backend-common")
                scm {
                    url.set("https://github.com/navikt/isyfo-backend-common")
                    connection.set("scm:git:https://github.com/navikt/isyfo-backend-common.git")
                    developerConnection.set("scm:git:ssh://git@github.com/navikt/isyfo-backend-common.git")
                }
            }
        }
    }
    repositories {
        maven {
            url = uri("https://maven.pkg.github.com/navikt/isyfo-backend-common")
            credentials {
                username = System.getenv("ORG_GRADLE_PROJECT_githubUser")
                password = System.getenv("ORG_GRADLE_PROJECT_githubPassword")
            }
        }
    }
}
