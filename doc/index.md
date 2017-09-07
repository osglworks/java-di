# Introduction


Genie is a JSR330 Dependency Injection solution inspired by [Feather](https://github.com/zsoltherpai/feather). Genie is designed to provide richer feature set than Feather while remaining much lightweight in comparing to [Google Guice](https://github.com/google/guice).

Genie is a core component of [ActFramework](https://github.com/actframework/actframework). However Genie can be used in any Java application independently.

## Install

Genie is provided through [![Maven Central](https://img.shields.io/maven-central/v/org.osgl/genie.svg)](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22genie%22)

```xml
<dependency>
    <groupId>org.osgl</groupId>
    <artifactId>genie</artifactId>
    <version>${genie.version}</version>
</dependency>
```

## Where to start

If you are new to [Dependency Injection](https://en.wikipedia.org/wiki/Dependency_injection), I recommend reading the [Getting Started](getting_start.md) guide, which walks through the basics.

If you are a veteran [Guice](https://github.com/google/guice) user, go straight to [Binding](binding.md) and walk through into Genie specific features including

* [Inject Collection and Map](doc/container.md)
* [Load bean from other sources](doc/value.md)

If you are curious about Genie's performance, please checkout [performance benchmark](performance.md)
