# CHANGE LOG

## 1.2.0

* Add method to `BeanSpec` to retrieve specfic annotation by type #9
* Add factory to construct `BeanSpec` with generic type #7 
* Introduce osgl-ut and osgl-bootstrap #15
* Improve maven build #14
* 

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
