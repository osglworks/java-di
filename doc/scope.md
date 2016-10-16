# Scoping

Genie support the following scopes:

* `javax.inject.Singleton`
* `org.osgl.inject.annotation.RequestScoped`
* `org.osgl.inject.annotation.SessionScoped`

The first scope has built-in support. The `RequestSceoped` and `SessionScoped` is provided when Genie is used
in a framework (e.g. [ActFramework](https://github.com/actframework/actframework)). In which case the framework
will provide the implementation of the following services:

* `org.osgl.inject.ScopeCache.SessionScope`
* `org.osgl.inject.ScopeCache.RequestScope`


