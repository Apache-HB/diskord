package com.serebit.diskord.data

class EntityNotFoundException(message: String) : RuntimeException(message)

class UnknownTypeCodeException(message: String) : IllegalArgumentException(message)

class UnknownEntityTypeException(message: String) : IllegalArgumentException(message)

class UnknownOpcodeException(message: String) : IllegalArgumentException(message)
