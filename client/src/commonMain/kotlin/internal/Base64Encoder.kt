package com.serebit.strife.internal

class Base64Encoder {

    companion object {
        @UseExperimental(ExperimentalStdlibApi::class)
        private val encodingTable =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray()

        private const val paddingFour = 4
        private const val bytesGroupThree = 3
        private const val shiftForCalc = 18
        private const val shiftForFirst = 16
        private const val shiftForSecond = 8
        private const val shiftDoneLeft = 6

        fun encodeBase64(bytes: ByteArray): String {
            val inputLength = bytes.size

            require(inputLength > 0) { "Input length must be greater than zero (was $inputLength)" }

            val outputLength = paddingFour * inputLength / bytesGroupThree + bytesGroupThree and bytesGroupThree.inv()
            val output = CharArray(outputLength)

            val lastIndex = outputLength - 1
            val beforeLastIndex = outputLength - 2

            var segment = 0
            var index = 0
            var toPad = 0

            while (segment < inputLength) {
                var binaryOperations = bytes[segment++].toInt().and(0xFF).shl(shiftForFirst).and(0xFFFFFF)
                    .or(
                        if (segment < inputLength) bytes[segment++].toInt().and(0xFF).shl(shiftForSecond)
                        else toPad++
                    ).or(if (segment < inputLength) bytes[segment++].toInt().and(0xFF) else toPad++)

                for (iterateCalc in 0 until paddingFour - toPad) {
                    val curb = binaryOperations and 0xFC0000 shr shiftForCalc
                    output[index++] = encodingTable[curb]
                    binaryOperations = binaryOperations shl shiftDoneLeft
                }
            }

            output[beforeLastIndex] = if (output[beforeLastIndex] == '\u0000') '=' else output[beforeLastIndex]
            output[lastIndex] = if (output[lastIndex] == '\u0000') '=' else output[lastIndex]

            return String(output)
        }
    }
}