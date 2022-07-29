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

The [Mojang API Implementation](src/commonMain/kotlin/org.universe.mojang/MojangAPI.kt) allows you to easily interact
with API. You just need to define a [Http client](https://ktor.io/docs/create-client.html) from [Ktor](https://ktor.io/).

```kotlin
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.universe.mojang.MojangAPI
import org.universe.mojang.MojangAPIImpl

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