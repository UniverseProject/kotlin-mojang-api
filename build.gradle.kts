plugins {
    kotlin("multiplatform") version "1.7.10"
    kotlin("plugin.serialization") version "1.7.10"
    id("org.jetbrains.dokka") version "1.7.10"
    `maven-publish`
    signing
}

group = "io.github.universeproject"
version = "1.0"
description = "Allows interaction with Mojang API using Kotlin and coroutine"

repositories {
    mavenCentral()
}

val ktorVersion: String by project
val ktSerializationVersion: String by project

kotlin {
    explicitApi = org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode.Strict
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }
    js(IR) {
        nodejs()
    }
    val hostOs = System.getProperty("os.name")
    val isMingwX64 = hostOs.startsWith("Windows")
    val nativeTarget = when {
        hostOs == "Mac OS X" -> macosX64("native")
        hostOs == "Linux" -> linuxX64("native")
        isMingwX64 -> mingwX64("native")
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-core:$ktorVersion")
                implementation("io.ktor:ktor-client-serialization:$ktorVersion")
                implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
                implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")

                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$ktSerializationVersion")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
            }
        }
        val jvmMain by getting
        val jvmTest by getting {
            dependencies {
                implementation("io.ktor:ktor-client-cio:$ktorVersion")
            }
        }
        val jsMain by getting
        val jsTest by getting
        val nativeMain by getting
        val nativeTest by getting
    }
}

/**
 * Whether the process has been invoked to publish in maven.
 */
val isPublishToMaven = "true" == System.getenv("PUBLISH_MAVEN")

val dokkaHtml by tasks.getting(org.jetbrains.dokka.gradle.DokkaTask::class)

val javadocJar: TaskProvider<Jar> by tasks.registering(Jar::class) {
    dependsOn(dokkaHtml)
    archiveClassifier.set("javadoc")
    from(dokkaHtml.outputDirectory)
}

publishing {
    val isReleaseVersion = !version.toString().endsWith("SNAPSHOT")

    publications {
        create<MavenPublication>(project.name) {
            from(components["kotlin"])
            artifact(javadocJar)

            pom {
                val projectGitUrl = "https://github.com/UniverseProject/kotlin-mojang-api"
                name.set(project.name)
                description.set(project.description)
                url.set(projectGitUrl)

                inceptionYear.set("2022")
                issueManagement {
                    system.set("GitHub")
                    url.set("$projectGitUrl/issues")
                }

                ciManagement {
                    system.set("GitHub Actions")
                }

                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://mit-license.org/")
                        distribution.set("repo")
                        comments.set("A business-friendly OSS license")
                    }
                }

                developers {
                    developer {
                        id.set("Distractic")
                        name.set("Distractic")
                        email.set("Distractic@outlook.fr")
                        url.set("https://github.com/Distractic")
                        timezone.set("Europe/Paris")
                    }
                }

                scm {
                    connection.set("scm:git:$projectGitUrl.git")
                    developerConnection.set("scm:git:git@github.com:UniverseProject/kotlin-mojang-api.git")
                    url.set(projectGitUrl)
                }

                distributionManagement {
                    downloadUrl.set("$projectGitUrl/releases")
                }
            }
        }
    }

    if(isPublishToMaven) {
        repositories {
            maven {
                name = "OSSRH"
                url = if (isReleaseVersion) {
                    uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                } else {
                    uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
                }

                credentials {
                    username = System.getenv("REPOSITORY_USERNAME")
                    password = System.getenv("REPOSITORY_PASSWORD")
                }
            }
        }
    }
}

if(isPublishToMaven) {
    signing {
        val signingKey = System.getenv("SIGNING_KEY")
        val signingPassword = System.getenv("SIGNING_PASSWORD")
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(publishing.publications[project.name])
    }
}
