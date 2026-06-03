group = "no.nav.syfo"
version = "0.0.41"
description = "Shared Kotlin utility library for iSyfo backend Ktor services."

val jacksonVersion = "2.21.4"
val ktorVersion = "3.5.0"
val logbackVersion = "1.5.34"
val micrometerVersion = "1.16.5"
val mockkVersion = "1.14.11"
val slf4jVersion = "2.0.18"

plugins {
    kotlin("jvm") version "2.3.21"
    `java-library`
    `java-test-fixtures`
    `maven-publish`
    id("org.jlleitschuh.gradle.ktlint") version "14.2.0"
    id("com.adarshr.test-logger") version "4.0.0"
    id("com.github.ben-manes.versions") version "0.54.0"
}

repositories {
    mavenCentral()
}

dependencies {
    // Exposed in public API — consumers need these on their compile classpath
    api("io.ktor:ktor-client-core:$ktorVersion")
    api("io.ktor:ktor-server-auth-jwt:$ktorVersion")
    // BOM keeps all Jackson artifacts (incl. transitive ones) on a single aligned version
    api(platform("com.fasterxml.jackson:jackson-bom:$jacksonVersion"))
    api("com.fasterxml.jackson.module:jackson-module-kotlin")
    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    // Internal — encapsulated behind library functions, not referenced directly by consumers
    implementation("io.micrometer:micrometer-core:$micrometerVersion")
    implementation("io.ktor:ktor-client-apache5:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-jackson:$ktorVersion")

    // Logging facade only; consuming apps own the runtime binding (e.g. logback-classic)
    implementation("org.slf4j:slf4j-api:$slf4jVersion")

    // Test fixtures (published as a separate -test-fixtures artifact); version supplied by the Jackson BOM
    testFixturesImplementation(platform("com.fasterxml.jackson:jackson-bom:$jacksonVersion"))
    testFixturesImplementation("com.fasterxml.jackson.core:jackson-annotations")

    // Tests
    testImplementation("io.ktor:ktor-client-mock:$ktorVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("ch.qos.logback:logback-classic:$logbackVersion")
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
