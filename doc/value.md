# Inject customized value loader

Genie allows application developer to plugin customised value loading logic in an elegant and fast way

Suppose you have a Greeting service:

```java
public class GreeterService {

    @LocalizedMessage("greeterservice.greeting")
    private Message message;

    public String sayHello(String caller) {
        return message.format($.notNull(caller));
    }

}
```

You need the framework to inject the greeting message into the service based on the `@LocalizedMessage` 
annotation. There are two approaches to get this done.
 
1. Create a module and provides factory method that takes `BeanSpec` as parameter.

```java
// Declare LocalizedMessage with Qualifier annotation
@InjectTag
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
public @interface LocalizedMessage {

    String value() default "";

}

// Create the module to provides factory method
public class MyModule {
    @Provides
    @LocalizedMessage // make sure LocalizedMessage has been annotated with Qualifier
    public Message createMessage(BeanSpec spec, Injector injector) {
        LocalizedMessage localizedMessage = spec.getAnnotation(LocalizedMessage.class);
        if (null == localizedMessage) {
            return null;
        }
        MessageSource messageSource = injector.get(MessageSource.class);
        return new Message(localizedMessage.value(), messageSource);
    }
}
```

2. Use Genie's value loading mechanism

```java
// Declare LocalizedMessage with @LoadValue annotation
@InjectTag
@LoadValue(MessageLoader.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface LocalizedMessage {

    String value() default "";

}

// Implement MessageLoader class
public class MessageLoader extends ValueLoader.Base<Message> {

    @Inject
    private MessageSource messageSource;

    @Override
    public Message get() {
        return new Message(value(), messageSource);
    }

}
```

The first approach is very like configure with `InjectionPoint` in Spring 4.3. However it is not encouraged 
when the second approach is available, because:

1. It involves annotation look up for each injection request. While value loader approach has much faster runtime performance
2. Value loader approach code is more cleaner


