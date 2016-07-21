# Genie

Genie is a JSR330 Dependency Injection solution inspired by [Feather](https://github.com/zsoltherpai/feather). Genie is designed to provide richer feature set than Feather while remaining much lightweight in comparing to [Google Guice](https://github.com/google/guice)

## Maven Dependency

```xml
    <dependency>
      <groupId>org.osgl</groupId>
      <artifactId>genie</artifactId>
      <version>0.1.0-SNAPSHOT</version>
    </dependency>
```

**Note** Please add the following section to your `pom.xml` file to get the SNAPSHOT version software:

```xml
  <parent>
    <groupId>org.sonatype.oss</groupId>
    <artifactId>oss-parent</artifactId>
    <version>7</version>
  </parent>
```

## Documents

* [Getting started](doc/getting_start.md)
* [Type binding](doc/type_binding.md)
* [Inject Collection and Map](doc/container.md)
* [Scoping](scope.md) - TBD
* [IoC container integration](integration.md) - TBD

