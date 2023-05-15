plugins {
    kotlin("multiplatform") version "1.8.20"
    kotlin("plugin.serialization") version "1.8.20"

    id("org.jetbrains.dokka") version "1.8.10"
    id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
    id("net.researchgate.release") version "3.0.2"
    `maven-publish`
    signing
}

subprojects {
    apply(plugin = "signing")
    apply(plugin = "maven-publish")
    apply(plugin = "org.jetbrains.dokka")
    apply(plugin = "plugin.serialization")
}

repositories {
    mavenCentral()
}

val ktorVersion: String by project
val ktSerializationVersion: String by project
val coroutineVersion: String by project

kotlin {
    explicitApi = org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode.Strict
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = JavaVersion.VERSION_1_8.toString()
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
        val ktorVersion="2.3.0"
        val ktSerializationVersion="1.5.0"
        val coroutineVersion="1.7.1"

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
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutineVersion")
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

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
            username.set(System.getenv("REPOSITORY_USERNAME"))
            password.set(System.getenv("REPOSITORY_PASSWORD"))
        }
    }
}

configure(allprojects) {
    val signingKey: String? = System.getenv("SIGNING_KEY")
    val signingPassword: String? = System.getenv("SIGNING_PASSWORD")
    if(signingKey != null && signingPassword != null) {
        signing {
            useInMemoryPgpKeys(signingKey, signingPassword)
            sign(publishing.publications)
        }
    }

    val dokkaOutputDir = "$buildDir/dokka/${this@configure.name}"

    tasks {
        dokkaHtml.configure {
            outputDirectory.set(file(dokkaOutputDir))
        }
    }

    val deleteDokkaOutputDir by tasks.register<Delete>("deleteDokkaOutputDirectory") {
        delete(dokkaOutputDir)
    }

    val javadocJar = tasks.register<Jar>("javadocJar") {
        dependsOn(deleteDokkaOutputDir, tasks.dokkaHtml)
        archiveClassifier.set("javadoc")
        from(dokkaOutputDir)
    }

    publishing {
        publications {
            val projectGitUrl = "https://github.com/UniverseProject/kotlin-mojang-api"
            withType<MavenPublication> {
                artifact(javadocJar)
                pom {
                    name.set(this@configure.name)
                    description.set(project.description)
                    url.set(projectGitUrl)

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
                        }
                    }

                    developers {
                        developer {
                            name.set("Distractic")
                            email.set("Distractic@outlook.fr")
                            url.set("https://github.com/Distractic")
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
    }
}

release {
    tagTemplate.set("v${version}")
}