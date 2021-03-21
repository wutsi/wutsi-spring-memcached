package com.wutsi.spring.memcached

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import net.rubyeye.xmemcached.MemcachedClient
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.concurrent.Callable

internal class MemcachedCacheTest {
    companion object {
        const val TTL = 86400
        const val NAME = "default"
    }

    lateinit var memcached: MemcachedClient
    lateinit var cache: MemcachedCache

    @BeforeEach
    fun setUp() {
        memcached = mock()
        cache = MemcachedCache(NAME, TTL, memcached)
    }

    @Test
    fun getName() {
        assertEquals(NAME, cache.name)
    }

    @Test
    fun `getNativeCache should return memcached instance`() {
        assertEquals(memcached, cache.nativeCache)
    }

    @Test
    fun `get should return null when no value`() {
        assertNull(cache.get("foo"))
    }

    @Test
    fun `get should return null when Memcached throws an exception`() {
        doThrow(RuntimeException()).whenever(memcached).get<String>("foo")
        assertNull(cache.get("foo"))
    }

    @Test
    fun `get should returns Object value from memcached`() {
        val entry = CacheEntry(
            classname = "java.util.Map",
            data = """
                {
                  "firstName": "yo",
                  "lastName": "man"
                }
            """.trimIndent()
        )
        doReturn(entry).whenever(memcached).get<String>("$NAME#foo")

        val value = cache.get("foo")
        val result = value?.get() as Map<String, String>

        assertEquals("yo", result["firstName"])
        assertEquals("man", result["lastName"])
    }

    @Test
    fun `get should returns primitive value from memcached`() {
        val entry = CacheEntry(
            classname = "java.lang.Integer",
            data = "2"
        )
        doReturn(entry).whenever(memcached).get<String>("$NAME#foo")

        val value = cache.get("foo")
        val result = value?.get()

        assertEquals(2, result)
    }

    @Test
    fun `get should returns String value from memcached`() {
        val entry = CacheEntry(
            classname = "java.lang.String",
            data = "2"
        )
        doReturn(entry).whenever(memcached).get<String>("$NAME#foo")

        val value = cache.get("foo")
        val result = value?.get()

        assertEquals("2", result)
    }

    @Test
    fun `getByType should returns value from memcached when type matches`() {
        val entry = CacheEntry(
            classname = "java.util.Map",
            data = """
                {
                  "firstName": "yo",
                  "lastName": "man"
                }
            """.trimIndent()
        )
        doReturn(entry).whenever(memcached).get<String>("$NAME#foo")

        val result = cache.get("foo", Map::class.java)
        assertEquals("yo", result?.get("firstName"))
        assertEquals("man", result?.get("lastName"))
    }

    @Test
    fun `getByType should returns null when type don't matches`() {
        val entry = CacheEntry(
            classname = "java.util.Map",
            data = """
                {
                  "firstName": "yo",
                  "lastName": "man"
                }
            """.trimIndent()
        )
        doReturn(entry).whenever(memcached).get<String>("$NAME#foo")

        val result = cache.get("foo", String::class.java)
        assertNull(result)
    }

    @Test
    fun `getWithCallable returns value from memcached when available`() {
        val entry = CacheEntry(
            classname = "java.util.Map",
            data = """
                {
                  "firstName": "yo",
                  "lastName": "man"
                }
            """.trimIndent()
        )
        doReturn(entry).whenever(memcached).get<String>("$NAME#foo")

        val callable = mock<Callable<Map<String, String>>>()

        val result = cache.get("foo", callable)
        assertEquals("yo", result?.get("firstName"))
        assertEquals("man", result?.get("lastName"))
    }

    @Test
    fun `getWithCallable returns value from callable when not available from memcache`() {
        doReturn(null).whenever(memcached).get<String>("$NAME#foo")

        val callable = mock<Callable<Map<String, String>>>()
        doReturn(mapOf("firstName" to "yo")).whenever(callable).call()

        val result = cache.get("foo", callable)
        assertEquals("yo", result?.get("firstName"))
    }

    @Test
    fun `getWithCallable returns null from callable failes`() {
        doReturn(null).whenever(memcached).get<String>("$NAME#foo")

        val callable: Callable<String> = mock()
        doThrow(RuntimeException()).whenever(callable).call()

        val result = cache.get("foo", callable)
        assertNull(result)
    }

    @Test
    fun `put should store into memcached`() {
        cache.put("foo", "bar")

        val name = argumentCaptor<String>()
        val ttl = argumentCaptor<Int>()
        val value = argumentCaptor<CacheEntry>()
        verify(memcached).set(name.capture(), ttl.capture(), value.capture())

        assertEquals("$NAME#foo", name.firstValue)
        assertEquals(TTL, ttl.firstValue)
        assertEquals("java.lang.String", value.firstValue.classname)
        assertEquals("\"bar\"", value.firstValue.data)
    }

    @Test
    fun `putIfAbsent should store into memcached when value not available`() {
        cache.putIfAbsent("foo", "bar")

        val name = argumentCaptor<String>()
        val ttl = argumentCaptor<Int>()
        val value = argumentCaptor<CacheEntry>()
        verify(memcached).set(name.capture(), ttl.capture(), value.capture())

        assertEquals("$NAME#foo", name.firstValue)
        assertEquals(TTL, ttl.firstValue)
        assertEquals("java.lang.String", value.firstValue.classname)
        assertEquals("\"bar\"", value.firstValue.data)
    }

    @Test
    fun `putIfAbsent should not store into memcached when value available`() {
        val entry = CacheEntry(
            classname = "java.util.Map",
            data = """
                {
                  "firstName": "yo",
                  "lastName": "man"
                }
            """.trimIndent()
        )
        doReturn(entry).whenever(memcached).get<String>("$NAME#foo")

        cache.putIfAbsent("foo", "bar")

        verify(memcached, never()).set(any(), any(), any())
    }

    @Test
    fun `evict should remove value`() {
        cache.evict("foo")
        verify(memcached).delete("$NAME#foo")
    }

    @Test
    fun `clear should flush the cache`() {
        cache.clear()
        verify(memcached).flushAll()
    }
}
