# kotlin-mojang-api

[![Discord](https://img.shields.io/discord/977166213467734056.svg?color=&label=Discord&logo=discord&style=for-the-badge)](https://discord.gg/EQyycAUZtt)
[![Download](https://maven-badges.herokuapp.com/maven-central/io.github.universeproject/kotlin-mojang-api/badge.svg?style=for-the-badge&logo=appveyor)](https://search.maven.org/search?q=g:io.github.universeproject)

This project allows interaction with [Mojang API](https://mojang-api-docs.netlify.app/)
using [Kotlin](https://kotlinlang.org/) and [coroutine](https://kotlinlang.org/docs/coroutines-overview.html).

## Environment

We have chosen to use [Kotlin](https://kotlinlang.org/) to simplify our codes, learn the language and take advantage of
coroutines for the I/O operations.

[Gradle](https://gradle.org/) is used to manage dependencies because he's the more friendly with Kotlin.

The project is compiled to :
- [Java 8](https://www.oracle.com/java/technologies/javase/javase8-archive-downloads.html)
- [JavaScript](https://www.javascript.com/)
- Native

See [Multiplatform documentation](https://kotlinlang.org/docs/multiplatform.html)

## Installation

Replace `{version}` with the latest version number on **Maven central**.

[![Download](https://maven-badges.herokuapp.com/maven-central/io.github.universeproject/kotlin-mojang-api/badge.svg?style=flat)](https://search.maven.org/search?q=g:io.github.universeproject)

### Gradle (groovy)

```groovy
repositories {
    mavenCentral()
}
```

---

```groovy
dependencies {
  // for jvm environment
  implementation("io.github.universeproject:kotlin-mojang-api-jvm:{version}")
  // for js environment
  implementation("io.github.universeproject:kotlin-mojang-api-js:{version}")
  // for native environment
  implementation("io.github.universeproject:kotlin-mojang-api-native:{version}")
}
```

### Gradle (kotlin)

```kotlin
repositories {
    mavenCentral()
}
```

---

```kotlin
dependencies {
  // for jvm environment
  implementation("io.github.universeproject:kotlin-mojang-api-jvm:{version}")
  // for js environment
  implementation("io.github.universeproject:kotlin-mojang-api-js:{version}")
  // for native environment
  implementation("io.github.universeproject:kotlin-mojang-api-native:{version}")
}
```

### Maven

```xml
<dependencies>
  <!-- for jvm environment -->
  <dependency>
      <groupId>io.github.universeproject</groupId>
      <artifactId>kotlin-mojang-api-jvm</artifactId>
      <version>{version}</version>
  </dependency>
  
  <!-- for js environment -->
  <dependency>
    <groupId>io.github.universeproject</groupId>
    <artifactId>kotlin-mojang-api-js</artifactId>
    <version>{version}</version>
  </dependency>
  
  <!-- for native environment -->
  <dependency>
    <groupId>io.github.universeproject</groupId>
    <artifactId>kotlin-mojang-api-native</artifactId>
    <version>{version}</version>
  </dependency>
</dependencies>
```

## API

The [Mojang API Implementation](src/commonMain/kotlin/io/github/universeproject/kotlinmojangapi/MojangAPI.kt) allows you to easily interact
with API. You just need to define a [Http client](https://ktor.io/docs/create-client.html) from [Ktor](https://ktor.io/).

```kotlin
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import io.github.universeproject.kotlinmojangapi.MojangAPI
import io.github.universeproject.kotlinmojangapi.MojangAPIImpl

suspend fun main() {
    // We advise to ignore unknown keys in case of api change
    val json = Json {
        ignoreUnknownKeys = true
    }

    // You can use another engine (other than CIO) for your http client
    val httpClient = HttpClient(CIO) {
        expectSuccess = false
        // Necessary to transform the response from api to a data object
        install(ContentNegotiation) {
            json(json)
        }
    }

    val mojangApi: MojangAPI = MojangAPIImpl(httpClient)
    // you can interact with Mojang api
    println(mojangApi.getUUID("Notch"))
}
```

# Maintainer

This section is dedicated to the maintainers of the project.

## Build

To build the project, you need to use the gradle app ([gradlew.bat](gradlew.bat) for windows
and [gradlew](gradlew) for linux).
`gradlew` is a wrapper to run gradle command without install it on your computer.

````shell
gradlew build
````

## Test

The tests are created using [Kotlin test](https://kotlinlang.org/api/latest/kotlin.test/).
To simplify the conception of tests, some common tests are placed in [JVM test](src/jvmTest) module.

### Run tests

````shell
gradlew allTests
````

## Release

### Create tag

When you make change on the projet, you need to create a release of the main branch.

Firstly, you need to create a specific tag version.

A [gradle plugin](https://github.com/researchgate/gradle-release) is used to simplify this operation.
Basically, you can check the official documentation, but you only need to make this following commands.

To keep coherence, you need to check the last version release for the project.

````shell
gradle release -Prelease.useAutomaticVersion=true -Prelease.releaseVersion=<version to release>
````

For example, if the latest version is `1.2.1`, you can create the next release tag using :

````shell
# Patch release
gradle release -Prelease.useAutomaticVersion=true -Prelease.releaseVersion=1.2.2 -Prelease.newVersion=1.2.3-SNAPSHOT

# Minor release
gradle release -Prelease.useAutomaticVersion=true -Prelease.releaseVersion=1.3.0 -Prelease.newVersion=1.3.1-SNAPSHOT

# Major release
gradle release -Prelease.useAutomaticVersion=true -Prelease.releaseVersion=2.0.0 -Prelease.newVersion=2.0.1-SNAPSHOT
````

For the convention, you can check this [link](http://semver.org/).

After these commands, several commits has been made on `main` branch and a new `tag` was created.

At this moment, no artifact is published in repository.

### Create release

#### Credentials (if necessary)

If you need new credentials to publish in repository, you need to follow this tutorial :

- You need me to add you as publisher of the repository in [Sonatype issues](https://issues.sonatype.org/browse/OSSRH-83171)
  - Create an account
  - Ask me to add you in the publishers list
- Generate a [Gpg key pair](https://central.sonatype.org/publish/requirements/gpg/#deployment)
- When you get the permission, you can check in the [Sonatype staging](https://s01.oss.sonatype.org/#stagingRepositories) if you can log in and see artifacts.
- Generate [Access User Token](https://s01.oss.sonatype.org/#profile;User%20Token) for security
- You can publish in repository artifact !

## Automatic

To publish the artifacts, you need to create a release on the GitHub repository.
When you create your release, select your created tag and explain all changes.

When the release is published, **automatically**, the CI/CD will try to create and publish in the artifact repository.

The CI/CD uses GitHub Secret to publish in artifact repository, so you may need to replace the current secret of the GitHub repository.

#### Manual

If, for X reason, you want to publish the artifact yourself, you need to define several environment variable using by gradle script :

- REPOSITORY_USERNAME (Access User Token username)
- REPOSITORY_PASSWORD (Access User Token password)
- SIGNING_KEY (Gpg private key)
````shell
# Export your private key in the file privatekey.key
# replace <keyid> by you gpg key id
gpg --export-secret-keys -a <keyid> > privatekey.key
````
- SIGNING_PASSWORD (Gpg password)

After defining the variables, you can run one the following commands :

##### Manual close

Manual close action allows to verify in the [Sonatype staging](https://s01.oss.sonatype.org/#stagingRepositories) repository if all files are correctly published.

````shell
gradle publishToSentry
````

You can check if the artifact is correctly in [Sonatype staging](https://s01.oss.sonatype.org/#stagingRepositories) repository.
If he's present, you can `close` to trigger the check by repository and if all is good, you can `release` to publish the artifact in production.

##### Automatic close

This following command allows to publish, close instantly the artifact to trigger checks and finally release it.

````shell
gradle publishToSonatype closeAndReleaseSonatypeStagingRepository 
````