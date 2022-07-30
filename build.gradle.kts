plugins {
    kotlin("multiplatform") version "1.7.10"
    kotlin("plugin.serialization") version "1.7.10"
    `maven-publish`
    signing
}

group = "io.github.universeproject"
version = "1.0"

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

publishing {
    val publicationDefault = "default"
    val isReleaseVersion = !version.toString().endsWith("SNAPSHOT")
    /**
     * Whether the process has been invoked to publish in maven.
     */
    val isPublishToMaven = "true" == System.getenv("PUBLISH_MAVEN")

    publications {
        create<MavenPublication>(publicationDefault) {
            pom {
                name.set(project.name)
                description.set(project.description)

                issueManagement {
                    system.set("GitHub")
                    url.set("https://github.com/UniverseProject/kotlin-mojang-api/issues")
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

                scm {
                    connection.set("scm:git:https://github.com/UniverseProject/kotlin-mojang-api.git")
                    developerConnection.set("scm:git:git@github.com:UniverseProject/kotlin-mojang-api.git")
                    url.set("https://github.com/UniverseProject/kotlin-mojang-api")
                }

                distributionManagement {
                    downloadUrl.set("https://github.com/UniverseProject/kotlin-mojang-api/releases")
                }
            }
        }
    }

    if(isPublishToMaven) {
        repositories {
            maven {
                name = "OSSRH"
                url = if (isReleaseVersion) {
                    uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
                } else {
                    uri("https://oss.sonatype.org/content/repositories/snapshots/")
                }

                credentials {
                    username = System.getenv("MAVEN_USERNAME")
                    password = System.getenv("MAVEN_PASSWORD")
                }
            }

            signing {
                val signingKey = System.getenv("SIGNING_KEY")
                val signingPassword = System.getenv("SIGNING_PASSWORD")
                useInMemoryPgpKeys(signingKey, signingPassword)
                sign(publishing.publications[publicationDefault])
            }
        }
    }


}