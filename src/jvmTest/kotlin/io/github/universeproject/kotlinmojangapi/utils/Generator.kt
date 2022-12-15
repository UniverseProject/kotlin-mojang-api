package io.github.universeproject.kotlinmojangapi.utils

import java.util.*

val stringGenerator = generateSequence { UUID.randomUUID().toString() }.distinct().iterator()

fun getRandomString() = stringGenerator.next()

fun generateRandomNameWithInvalidSymbol() = generateRandomName().drop(1) + "&"

fun generateRandomName(): String {
    val validCharRanges: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    return generateSequence { validCharRanges.random() }.take(16).joinToString("")
}

fun generateUUIDWithInvalidSymbol() = generateRandomUUID().drop(1) + "&"

fun generateRandomUUID() = getRandomString()