package com.serebit.strife.internal

import kotlinx.datetime.Instant

internal fun Instant.Companion.parseSafe(isoTimestamp: String) = parse(isoTimestamp.replace("+00:00", "Z"))
