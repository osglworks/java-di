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

## Features:

* JSR330 Dependency injection support on Constructor/Field/Method
* Module and `@Provides` factory methods
* Fully support on `@Qualifier` tags
* Support `Singleton`, `SessionScoped`, `RequestScoped` annotation from built-in and CDI-api-1.2, plus Guice version
* Support Collection/Map loader
* Support inject array type bean
* Support generic value loader and more specific configuration value loader
* Support `@javax.annotations.PostConstruct` semantic
* High performance (Much faster than Guice and Spring). See [this benchmark](https://github.com/greenlaw110/di-benchmark)

## Documents

* [Getting started](doc/getting_start.md)
* [Type binding](doc/type_binding.md)
* [Inject Collection and Map](doc/container.md)
* [Load bean from other sources](doc/value.md)
* [Scoping](scope.md)
* [IoC container integration](integration.md) - TBD

## Demos

* [Implement Spring 4.3 style InjectionPoint style binding](https://github.com/greenlaw110/hello-genie-injectionPoint)
* [Implement Spring 4.3 style customized annotation](https://github.com/greenlaw110/genie-custom-annotation-demo)