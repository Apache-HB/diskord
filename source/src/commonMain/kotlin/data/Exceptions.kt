package com.serebit.diskord.data

class EntityNotFoundException(message: String) : RuntimeException(message)

class UnknownTypeCodeException(message: String) : RuntimeException(message)

class UnknownOpcodeException(message: String) : RuntimeException(message)
