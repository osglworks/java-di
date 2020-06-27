CHANGE LOG

1.13.2 27/Jun/2020
* Update to osgl-tool 1.25.0

1.13.1 02/Mar/2020
* Exception when calling `genie.get(Integer.class)` #59
* `Genie.subjectToInject(BeanSpec)` issue #58 
* `ArrayIndexOutOfBoundsException` on calling `BeanSpec.fields()` #57

1.13.0 02/Jan/2020
* Simplify injection of Enum instances #56
* `ElementType.BEAN` - support transform from an `Enum` class #55
* MapLoader - allow load into non-String key typed map #54

1.12.0 03/Nov/2019
* udpate to osgl-tool 1.21.0

1.11.0 21/Jul/2019
* update to osgl-tool 1.20.0
* Enable inject map typed structure based `@Named` annotation on class definition #53

1.10.0 19/Apr/2019
* update to osgl-tool 1.19.2
* BeanSpec - equals and hashCode method shall take raw type into consideration #52

1.9.4 04/Feb/2019
* `BeanSpec` - improve field spec inference - support nested type params #51
* `NullPointerException` triggered on calling `Binder.register` #49
* `UnexpectedException` on getting fields spec when there are multiple level inheritance #50

1.9.3 23/Dec/2018
* BeanSpec - it shall handle `GenericArrayType` type #47
* BeanSpec - `parent()` might raise `UnexpectedException` #46

1.9.2 09/Dec/2018
* `BeanSpec` - when `typeParamImplLookup` is provided it shall resolve type parameters #45
* `BeanSpec` error message format issue #44

1.9.1 28/Nov/2018
* `BeanSpec.resolveAnnotations` shall take `typeParamImplLookup` into consideration #43

1.9.0 30/Oct/2018
* update to osgl-tool-1.18.0
* Add default implemetation for `ScopeCache.SingletonScope` #41
* BeanSpec - Qualifier shall not be count as loadValueImcompatible #39
* Getting `LinkedHashMap` cause error #38
* `InjectionException` encountered when parent field type is a type variable #37
* update to osgl-tool 1.18.0
* BeanSpec - support passing type param lookup for generic type variable resolving #36

1.8.0 14/Jun/2018
* update to osgl-tool 1.15.1

1.7.3 13/May/2018
* update osgl-tool to 1.13.0

1.7.2 13/May/2018
* update osgl-tool to 1.12.0

1.7.1 8/May/2018
* `ArrayLoader` - when loading primitive type elements it shall convert `null` to default value #35
* `BeanSpec.parent()` shall populate type param implementation #33
* Super type's type parameter info lost in field provider lookup logic #34
* update osgl-tool to 1.11.1

1.7.0 6/Apr/2018
* Make `BeanSpec.withoutName()` be public #32
* update osgl-tool to 1.10.0

1.6.4 - 02/Apr/2018
* update osgl-tool to 1.9.0

1.6.3 - 25/Mar/2018
* update osgl-tool to 1.8.1

1.6.2 - 25/Mar/2018
* update osgl-tool to 1.8.0

## 1.6.1 - 20/Mar/2018
* BeanSpec - make `fields()` returns field specs indexed by original name #31
* Update osgl-tool to 1.7.3

## 1.6.0 - 13/Mar/2018
* ConfigurationLoader - support default value #30
* Update osgl-tool to 1.7.2

## 1.5.0 - 4/Mar/2018

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
