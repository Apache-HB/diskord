package com.serebit.diskord

import io.kotlintest.matchers.fail
import io.kotlintest.matchers.shouldNotBe
import io.kotlintest.specs.StringSpec

class ConnectionTest : StringSpec() {
    init {
        val token = System.getenv("DISCORD_TEST_TOKEN") ?: fail("Invalid test token.")

        "The builder should connect to Discord, i.e. not return null" {
            diskord(token) shouldNotBe null
        }
    }
}
