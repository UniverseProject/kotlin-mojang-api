# kotlin-mojang-api

This project allows interaction with [Mojang API](https://mojang-api-docs.netlify.app/)
using [Kotlin](https://kotlinlang.org/) and [coroutine](https://kotlinlang.org/docs/coroutines-overview.html).

## Environment

We have chosen to use [Kotlin](https://kotlinlang.org/) to simplify our codes, learn the language and take advantage of
coroutines for the I/O operations.

[Gradle](https://gradle.org/) is used to manage dependencies because he's the more friendly with Kotlin.

The project is compiled to :
- [Java 17](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
- [JavaScript](https://www.javascript.com/)
- Native

See [Multiplatform documentation](https://kotlinlang.org/docs/multiplatform.html)

## Use in your projects

Currently, no artifact is published. It will be done soon.

### API

The [Mojang API Implementation](src/commonMain/kotlin/io/github/universeproject/MojangAPI.kt) allows you to easily interact
with API. You just need to define a [Http client](https://ktor.io/docs/create-client.html) from [Ktor](https://ktor.io/).

```kotlin
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import io.github.universeproject.MojangAPI
import io.github.universeproject.MojangAPIImpl

suspend fun main() {
    // We advise to ignore unknown keys in case of api change
    val json = Json {
        ignoreUnknownKeys = true
    }

    // You can use another engine (other than CIO) for your http client
    val httpClient = HttpClient(CIO) {
        // We advise to expect success, with that, an exception will be thrown
        // if any issue with the request
        expectSuccess = true
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

To build the project, you need to use the gradle app in the application [gradlew.bat](gradlew.bat) for windows
and [gradlew](gradlew) for linux.
`gradlew` is a wrapper to run gradle command without install it on our computer.

````shell
gradlew build
````

## Test

The tests are created using [Kotlin test](https://kotlinlang.org/api/latest/kotlin.test/).
To simplify the conception of tests, the common tests are placed in [JVM test](src/jvmTest) module.

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
gradle release -Prelease.useAutomaticVersion=true -Prelease.releaseVersion=1.2.2

# Minor release
gradle release -Prelease.useAutomaticVersion=true -Prelease.releaseVersion=1.3.0

# Major release
gradle release -Prelease.useAutomaticVersion=true -Prelease.releaseVersion=2.0.0
````

For the convention, you can check this [link](http://semver.org/).

After these commands, several commits has been made on `main` branch and a new `tag` was created.

At this moment, no artifact is published in repository.

### Create release

#### Credentials (if necessary)

If you need new credentials to publish in repository, you need to follow this tutorial :

- You need that I add you as publisher of the repository in [Sonatype issues](https://issues.sonatype.org/browse/OSSRH-83171)
    - Create an account
    - Ask me to add you in the publisher
- Generate a [Gpg key pair](https://central.sonatype.org/publish/requirements/gpg/#deployment)
- When you get the permission, you can check in the [Sonatype staging](https://s01.oss.sonatype.org/#stagingRepositories) if you can log in and see artifacts.
- Generate [Access User Token](https://s01.oss.sonatype.org/#profile;User%20Token) for security
- You can publish in repository artifact !

## Automatic

To publish the artifacts, you need to create a release on the repository github.
When you create your release, select your created tag and explain all changes.

When the release is published, **automatically**, the CI/CD will try to create and publish the artifacts in repository artifacts.

The CI/CD uses Github Secret to publish in repository artifact, so you could need to replace the current secret of the github repository.

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

After defining the variables, you can execute one of this commande :

##### Manual close

Manual close action allows to verify in the [Sonatype staging](https://s01.oss.sonatype.org/#stagingRepositories) repository if all files are correctly published.

````shell
gradle publishToSentry
````

You can check if the artifact is correctly in [Sonatype staging](https://s01.oss.sonatype.org/#stagingRepositories) repository.
If he's present, you can `close` to active the check by repository and if all is good, you can `release` to publish the artifact in production.

##### Automatic close

This following command allows to publish, close instantly the artifact to trigger checks and finally release it.

````shell
gradle publishToSonatype closeAndReleaseSonatypeStagingRepository 
````