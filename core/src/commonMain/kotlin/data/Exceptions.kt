package com.serebit.strife.data

/** Thrown when the client receives an unknown type code. */
class UnknownTypeCodeException(message: String) : IllegalArgumentException(message)

/** Thrown when an attempt is made to convert data to an unknown [Entity][com.serebit.strife.entities.Entity]. */
class UnknownEntityTypeException(message: String) : IllegalArgumentException(message)

/**
 * Thrown when the client receives an unknown [Opcode][com.serebit.strife.internal.Opcodes].
 * [see](https://discordapp.com/developers/docs/topics/opcodes-and-status-codes).
 */
class UnknownOpcodeException(message: String) : IllegalArgumentException(message)
