package com.wutsi.spring.memcached

import net.rubyeye.xmemcached.MemcachedClient
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator

open class MemcachedHealthIndicator(
    private val client: MemcachedClient
) : HealthIndicator {
    companion object {
        val KEY = "__health_check__"
    }

    override fun health(): Health {
        val start = System.currentTimeMillis()
        try {
            client.get<String>(KEY)
            return Health.up()
                .withDetail("key", KEY)
                .withDetail("latency", System.currentTimeMillis() - start)
                .build()
        } catch (ex: Exception) {
            return Health.down()
                .withDetail("key", KEY)
                .withDetail("latency", System.currentTimeMillis() - start)
                .withException(ex)
                .build()
        }
    }
}
