package com.wutsi.spring.memcached

import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import net.rubyeye.xmemcached.MemcachedClient
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.actuate.health.Status
import kotlin.test.assertEquals

internal class MemcachedHealthIndicatorTest {
    lateinit var client: MemcachedClient
    lateinit var health: MemcachedHealthIndicator

    @BeforeEach
    fun setUp() {
        client = mock()
        health = MemcachedHealthIndicator(client)
    }

    @Test
    fun up() {
        assertEquals(Status.UP, health.health().status)
    }

    @Test
    fun down() {
        doThrow(RuntimeException()).whenever(client).get<String>(MemcachedHealthIndicator.KEY)
        assertEquals(Status.DOWN, health.health().status)
    }
}
