# CHANGE LOG

## 1.5.0

* Update osgl-tool to 1.7.0
* `BeanSpec.componentSpec()` returns `null` for `List<String>` #29
* It shall not load value into static fields #28

## 1.4.0 - 15/Jan/2018

* `Genie.subjectToInject(BeanSpec beanSpec)` shall return `false` from simple types and collection of simple types #27
* BeanSpec - add parent() method
* BeanSpec - add method to get bean spec of all fields #26
* Support dynamic named provider #25
* Do not cache provider for simple types without inject tag #24
* BeanSpec - provide a method to check if there are annotation impact inject #22

## 1.3.4 - 14/Jan/2018

* performance tune: `ScopeProvider` shall not put bean into scope cache when it is retrieved from cache

## 1.3.3 - 1/Jan/2018

* Revert changes in #18 and create new method for accessing array element type #20

## 1.3.2 - 31/Dec/2017

* `BeanSpec` - add `isInterface()` API #19

## 1.3.1 - 31/Dec/2017

* `BeanSpec` - make it easy to get array element type #18

## 1.3.0 - 19/Dec/2017

* Update to osgl-tool-1.5

## 1.2.1-BETA-2

* Update osgl-bootstrap and osgl-ut dependency version

## 1.2.1-BETA-1

* `ScopeCache` cannot handle same type with different generic type parameter case #16

## 1.2.0

* Add method to `BeanSpec` to retrieve specfic annotation by type #9
* Add factory to construct `BeanSpec` with generic type #7 
* Introduce osgl-ut and osgl-bootstrap #15
* Improve maven build #14
* Add factory to construct `BeanSpec` with generic type #7

## 1.1.3

* `BeanSpec` force adding `@TypeOf` annotation to Collection conflict with existing value loader annotation #8

## 1.0.2

* take out version range. See https://issues.apache.org/jira/browse/MNG-3092

## 1.0.1

* Use version range for osgl dependencies

## 1.0.0

* Baseline on 0.5.0

## 0.5.0-SNAPSHOT

* BeanSpec: add isInstance(Object) API
* BeanSpec: add Set<Annotation> qualifiers() API
* Genie.Binder: public constructor and register method so that Binder can be used independently

## 0.4.0-SNAPSHOT
* BeanSpec: API to test field modifiers (only applied to bean spec constructed from a field

## 0.3.0-SNAPSHOT

* update tool to 0.10.0

## 0.2.0-SNAPSHOT

* Refactory code and simplified Loader/Filter API
* Inject event dispatching

## 0.1.0-SNAPSHOT

* The initial version
