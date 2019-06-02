package com.serebit.strife.data

/** Thrown when the client receives an unknown type code, usually one for a channel type. */
class UnknownTypeCodeException(message: String) : IllegalArgumentException(message)

/** Thrown when an attempt is made to convert data to an unknown [com.serebit.strife.entities.Entity]. */
class UnknownEntityTypeException(message: String) : IllegalArgumentException(message)

/**
 * Thrown when the client receives a payload with an unknown opcode.
 * [See this page in the Discord docs.](https://discordapp.com/developers/docs/topics/opcodes-and-status-codes).
 */
class UnknownOpcodeException(message: String) : IllegalArgumentException(message)
