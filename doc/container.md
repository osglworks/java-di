# Container injection

Defining type binding is not enough for injecting complex data structures like container (Collections and Maps). For example, for the given Java class:
 
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

Genie comes up with a solution called `BeanLoader`. 



