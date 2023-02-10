package io.github.universeproject.kotlinmojangapi.serializer

import io.github.universeproject.kotlinmojangapi.extension.formatUUID
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Serializer to format a string to a valid uuid string format.
 * @property stringSerializer The string serializer.
 * @see formatUUID
 */
public object StringUUIDSerializer : KSerializer<String> {

    private val stringSerializer = String.serializer()

    override val descriptor: SerialDescriptor = stringSerializer.descriptor

    override fun serialize(encoder: Encoder, value: String) {
        stringSerializer.serialize(encoder, value.formatUUID())
    }

    override fun deserialize(decoder: Decoder): String = stringSerializer.deserialize(decoder).formatUUID()
}