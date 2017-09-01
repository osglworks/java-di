# Genie

[![APL v2](https://img.shields.io/badge/license-Apache%202-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html) 
[![Maven Central](https://img.shields.io/maven-central/v/org.osgl/genie.svg)](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22genie%22)
[![Build Status](https://travis-ci.org/osglworks/java-di.svg?branch=master)](https://travis-ci.org/osglworks/java-di)
[![codecov](https://codecov.io/gh/osglworks/java-di/branch/master/graph/badge.svg)](https://codecov.io/gh/osglworks/java-di)
[![Javadocs](http://www.javadoc.io/badge/org.osgl/genie.svg?color=red)](http://www.javadoc.io/doc/org.osgl/genie)

Genie is a JSR330 Dependency Injection solution inspired by [Feather](https://github.com/zsoltherpai/feather). 
Genie is designed to provide richer feature set than Feather while remaining much lightweight 
in comparing to [Google Guice](https://github.com/google/guice).

Genie is a core component of [ActFramework](https://github.com/actframework/actframework).

## Maven Dependency

```xml
<dependency>
    <groupId>org.osgl</groupId>
    <artifactId>genie</artifactId>
    <version>${genie.version}</version>
</dependency>
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

## Benchmark

Genie has very high runtime performance when comparing to Guice and Spring. See [this benchmark](https://github.com/greenlaw110/di-benchmark)

## Documents

* [Getting started](doc/getting_start.md)
* [Type binding](doc/type_binding.md)
* [Inject Collection and Map](doc/container.md)
* [Load bean from other sources](doc/value.md)
* [Scoping](scope.md)
* [IoC container integration](integration.md)

## Demos

* [Implement Spring 4.3 style InjectionPoint style binding](https://github.com/greenlaw110/hello-genie-injectionPoint)
* [Implement Spring 4.3 style customized annotation](https://github.com/greenlaw110/genie-custom-annotation-demo)
