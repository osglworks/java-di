# Genie

Genie is a JSR330 Dependency Injection solution inspired by [Feather](https://github.com/zsoltherpai/feather). Genie is designed to provide richer feature set than Feather while remaining much lightweight in comparing to [Google Guice](https://github.com/google/guice).

Genie is a core component of [ActFramework](https://github.com/actframework/actframework). However Genie can be used in any Java application independently.

## Install

Genie is provided as a standard Java library through maven repository:

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

If you are curious about Genie's performance, please checkout [performance benchmark](performance.md)

* [Inject collection and map](container.md)
* [Inject value object](value.md)