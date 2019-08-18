package com.serebit.strife.internal

import kotlinx.io.core.String

@UseExperimental(ExperimentalStdlibApi::class)
private val encodingTable = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".encodeToByteArray()

/** Encodes a [byte array][bytes] into a Base64 [String]. Returns the encoded [String]. */
internal fun encodeBase64(bytes: ByteArray): String {
    require(bytes.isNotEmpty()) { "Input length must be greater than zero (was ${bytes.size})" }

    val outputLength = 4 * bytes.size / 3 + 3 and 3.inv()
    val output = ByteArray(outputLength)

    var segment = 0
    var index = 0

    var toPad = 0
    val initialBytesLen = bytes.size - bytes.size % 3

    while (segment < initialBytesLen) {
        val binaryOperations = (bytes[segment++].toInt() and 0xFF shl 16)
            .or(bytes[segment++].toInt() and 0xFF shl 8)
            .or(bytes[segment++].toInt() and 0xFF)

        output[index++] = encodingTable[binaryOperations ushr 18 and 0x3f]
        output[index++] = encodingTable[binaryOperations ushr 12 and 0x3f]
        output[index++] = encodingTable[binaryOperations ushr 6 and 0x3f]
        output[index++] = encodingTable[binaryOperations and 0x3f]
    }

    if (segment < bytes.size) {
        var binaryOperations = (bytes[segment++].toInt() and 0xFF shl 16)
            .or(if (segment < bytes.size) bytes[segment++].toInt() and 0xFF shl 8 else toPad++)
            .or(if (segment < bytes.size) bytes[segment].toInt() and 0xFF else toPad++)

        repeat(4 - toPad) {
            output[index++] = encodingTable[binaryOperations and 0xFC0000 shr 18]
            binaryOperations = binaryOperations shl 6
        }
    }

    if (output[outputLength - 2] == '\u0000'.toByte()) output[outputLength - 2] = '='.toByte()
    if (output[outputLength - 1] == '\u0000'.toByte()) output[outputLength - 1] = '='.toByte()

    return String(output)
}
