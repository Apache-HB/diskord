package com.serebit.diskord.data

import com.serebit.diskord.IsoTimestamp
import com.serebit.diskord.UnixTimestamp

expect class DateTime : Comparable<DateTime> {
    val hour: Int
    val minute: Int
    val second: Int
    val nanosecond: Int
    val dayOfYear: Int
    val dayOfMonth: Int
    val dayOfWeek: Int
    val month: Int
    val year: Int

    companion object {
        fun now(): DateTime

        fun fromIsoTimestamp(timestamp: IsoTimestamp): DateTime

        fun fromUnixTimestamp(timestamp: UnixTimestamp): DateTime
    }
}

fun IsoTimestamp.toDateTime() = DateTime.fromIsoTimestamp(this)

fun UnixTimestamp.toDateTime() = DateTime.fromUnixTimestamp(this)
