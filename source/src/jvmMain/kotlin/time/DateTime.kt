package com.serebit.strife.time

import com.serebit.strife.IsoTimestamp
import com.serebit.strife.UnixTimestamp
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

actual class DateTime private constructor(private val time: OffsetDateTime) : Comparable<DateTime> {
    actual val hour get() = time.hour
    actual val minute get() = time.minute
    actual val second get() = time.second
    actual val nanosecond get() = time.nano
    actual val dayOfYear get() = time.dayOfYear
    actual val dayOfMonth get() = time.dayOfMonth
    actual val dayOfWeek get() = time.dayOfWeek.value
    actual val month get() = time.monthValue
    actual val year get() = time.year

    override operator fun compareTo(other: DateTime) = time.compareTo(other.time)

    override fun equals(other: Any?) = other === this || other is DateTime && time == other.time

    actual companion object {
        actual fun fromIsoTimestamp(timestamp: IsoTimestamp) = DateTime(OffsetDateTime.parse(timestamp))

        actual fun fromUnixTimestamp(timestamp: UnixTimestamp) =
            DateTime(Instant.ofEpochMilli(timestamp).atOffset(ZoneOffset.UTC))
    }
}