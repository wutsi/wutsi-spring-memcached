package com.wutsi.spring.memcached

import net.rubyeye.xmemcached.MemcachedClient
import net.rubyeye.xmemcached.XMemcachedClientBuilder
import net.rubyeye.xmemcached.auth.AuthInfo
import net.rubyeye.xmemcached.command.BinaryCommandFactory
import net.rubyeye.xmemcached.utils.AddrUtil

class MemcachedClientBuilder {
    private var servers: String = ""
    private var username: String = ""
    private var password: String = ""

    fun build(): MemcachedClient {
        val servers = AddrUtil.getAddresses(servers.replace(",", " "))
        val authInfo = AuthInfo.plain(username, password)

        val builder = XMemcachedClientBuilder(servers)
        servers.forEach {
            builder.addAuthInfo(it, authInfo)
        }
        builder.commandFactory = BinaryCommandFactory()
        builder.connectTimeout = 100

        return builder.build()
    }

    fun withUsername(username: String): MemcachedClientBuilder {
        this.username = username
        return this
    }

    fun withPassword(password: String): MemcachedClientBuilder {
        this.password = password
        return this
    }

    fun withServers(addresses: String): MemcachedClientBuilder {
        this.servers = addresses
        return this
    }
}
