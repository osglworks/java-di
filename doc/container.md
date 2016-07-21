# Container injection

## A tour of `ElementLoader` mechanism

Genie provides support on injecting Collection and Map type data. Unlike other type of Java bean injection, Collection/Map type injection require an additional step after the bean has been constructed: loading element into the container. Take an example below:
 
```java
/**
 * Print out a Fibonacci number series injected
 */
public class FibonacciDemo {
    @Inject
    private List<Integer> series;
    
    public void print(PrintStream ps) {
        for (int n: series) {
            ps.println(n);
        }
    }
}
```

We want to create a type binding module:

```java
public FibonacciModule extends Module {
    public void configure() {
        bind(List.class).to(ArrayList.class);
    }
}
```

**Tips** You don't have to do type binding for `List` and `Map` in your application because Genie has already done that for you.

Now if we load the bean with Genie:

```java
FibonacciDemo demo = genie.get(FibonacciDemo.class);
```

We will get a `FibonacciDemo` with an empty `ArrayList`. One possible way is to use `@Provides` method:

```java
public FibonacciModule {
    @Provides
    FibonacciDemo createDemo(ArrayList list) {
        // do logic to populate the list
    }
}
```

Now a problem is how many numbers should we populate into the list. We can either hard code a number, or anyway inject a number into the `@Provides` method. None of them satisfied our needs. 

Genie comes up with an nice solution that allows application developer to define their data loading logic:

 
First, define an `ElementLoader` that loads the fibonacci series:

```java
class FibonacciSeriesLoader extends ElementLoaderBase<Integer> {
    @Override
    public List<Integer> load(Map<String, Object> options, BeanSpec container, Genie genie) {
        int max = toInt(options.get("max")); // here "max" is provided by annotation class as shown below 
        int n1 = 1, n2 = 1, f;
        List<Integer> list = C.newList();
        list.add(n1);
        list.add(n2);
        for (;;) {
            f = n1 + n2;
            n1 = n2;
            n2 = f;
            if (f < max) {
                list.add(f);
            } else {
                break;
            }
        }
        return list;
    }
    ...
}
```

Note: the full code of the `FibonacciSeriesLoader.java` can be found at https://github.com/osglworks/java-di/blob/master/src/test/java/org/osgl/genie/FibonacciSeriesLoader.java
 
Now define the `Loader` annotation:

```java
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Loader(FibonacciSeriesLoader.class)
public @interface FibonacciSeries {
    /**
     * The max value in the series
     * @return the max value
     */
    int max() default 100;
}
```

Finally modify our demo app by adding the `@FibonacciSeries` annotation:

```java
/**
 * Print out a Fibonacci number series injected
 */
public class FibonacciDemo {
    @Inject
    @FibonacciSeries(max = 1000)
    private List<Integer> series;
    
    public void print(PrintStream ps) {
        for (int n: series) {
            ps.println(n);
        }
    }
}
```

Then you can use Genie to get the demo app and the series will get populated:

```java
FibonacciDemo demo = genie.get(FibonacciDemo.class);
demo.print(System.out);
```

## Inject Map type bean

Things is a little bit different if we need to populate a `Map` type bean. In addition to `ElementLoader` and `Loader` annotation, we need another annotation `MapKey` to specify how to get the `key` from the element so we can `put` the element along with it's `key` into the map. 

```java
/**
 * Dispatch error to proper handlers
 */
class ErrorDispatcher {
    @Inject
    @TypeOf
    @MapKey("errorCode")
    Map<Integer, ErrorHandler> registry;

    String handle(int error) {
        ErrorHandler handler = registry.get(error);
        return null == handler ? "unknown" : handler.toString();
    }
}
```

In the above code the `Map` type bean `registry` has been tagged with `@MapKey("errorCode")` and `@TypeOf` annotation. Where `@TypeOf` is a built-in element Loader annotation which tells Genie to load element who close is sub class or implementation of `ErrorHandler`. And `@MapKey("errorCode")` is the annotation to tell Genie how to retrieve the `key`: get the key from bean's "errorCode" property. And here is the code of `ErrorHandler`:

```java
/**
 * A test class: define a abstract class for error handlers
 */
public abstract class ErrorHandler {
    /**
     * Returns the error code the implementation is looking for
     * @return the interested error code
     */
    public abstract int getErrorCode();

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
```


## Built-in `ElementLoader`(s)

Genie provides three built-in Element loader and their corresponding annotation

| ---- | ---- | ----- |
| Loader | Annotation | Purpose |
| `AnnotatedElementLoader` | `AnnotatedWith` | Load element whose class has been annotated with specified annotation class |
| `TypedElementLoader` | `TypeOf` | Load element whose class is sub type or implementation of the specified class |

**Note** It requires Genie to be setup property with certain IoC container to use the `@AnnotatedWith` and `@TypeOf` element loader. 

Please refer the following section: [integration](integration.md)
