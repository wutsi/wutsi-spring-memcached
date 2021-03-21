[![](https://github.com/wutsi/wutsi-spring-memcached/actions/workflows/master.yml/badge.svg)](https://github.com/wutsi/wutsi-codegen/actions/workflows/master.yml)
[![](https://github.com/wutsi/wutsi-spring-memcached/actions/workflows/pull_request.yml/badge.svg)](https://github.com/wutsi/wutsi-codegen/actions/workflows/pull_request.yml)

[![JDK](https://img.shields.io/badge/jdk-11-brightgreen.svg)](https://jdk.java.net/11/)
![](https://img.shields.io/badge/language-kotlin-blue.svg)
[![JDK](https://img.shields.io/badge/version-0.0.2-brightgreen.svg)](https://jdk.java.net/11/)

SpringCache implementation for Memcached

# Features
- `MemcacheHealthIndicator`: a Spring HealthCheckIndicator for Memcached.
- `MemcachedCache`: Implementation of `org.springframework.cache.Cache`

# Usage
```
@Configuration
class SpringConfiguration {
}
```
