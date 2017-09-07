# Performance benchmark

We have created a [project](https://github.com/greenlaw110/di-benchmark) to benchmark Genie's performance against the following Java dependency injection libraries on the market:

* [Guice](https://github.com/google/guice) - 4.1.0
* [Feather](https://github.com/zsoltherpai/feather) - 1.0
* [Dagger](https://github.com/square/dagger) - 1.2.5
* [Pico](http://picocontainer.com/) - 2.15
* [Spring](http://projects.spring.io/spring-framework/) - 4.3.2.RELEASE

The result shows Genie is a very fast library:

### Startup and first time fetch benchmark

```
Split Starting up DI containers & instantiating a dependency graph 4999 times:
-------------------------------------------------------------------------------
                     Feather| start:     8ms   fetch:    73ms
                       Genie| start:   478ms   fetch:    98ms (*)
                      Dagger| start:    46ms   fetch:   130ms
                        Pico| start:   166ms   fetch:   161ms
                       Guice| start:   458ms   fetch:   800ms
     SpringJavaConfiguration| start: 13956ms   fetch:  1149ms
     SpringAnnotationScanned| start: 22302ms   fetch:  2738ms
```

### Runtime bean injection benchmark

```
Runtime benchmark, fetch new bean for 50K times:
---------------------------------------------------------
                      Dagger|    28ms
                       Genie|    45ms (*)
                     Feather|    68ms
                       Guice|   188ms
                        Pico|   353ms
     SpringJavaConfiguration|  1936ms
     SpringAnnotationScanned|  2369ms
```

```
Runtime benchmark, fetch new bean for 5M times:
---------------------------------------------------------
                      Dagger|   842ms
                       Genie|  1043ms (*)
                     Feather|  1748ms
                       Guice|  3022ms
                        Pico| 13185ms
     SpringJavaConfiguration| Timeout
     SpringAnnotationScanned| Timeout
```

```
Runtime benchmark, fetch singleton bean for 5M times:
---------------------------------------------------------
                       Genie|   118ms (*)
                     Feather|   180ms
                        Pico|   225ms
     SpringAnnotationScanned|   228ms
     SpringJavaConfiguration|   245ms
                       Guice|   559ms
                      Dagger|   746ms
``` 

For detail information about the benchmark project, please go visit the [project github repository](https://github.com/greenlaw110/di-benchmark/)