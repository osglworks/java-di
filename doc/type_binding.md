# Type binding

Now let's say you have the following Java code:

```java
public enum Gender {MALE, FEMALE}

public interface Person {
	Gender getGender();
}

public class Man implement Person {
	public Gender getGender() {
		return Gender.MALE;
	}
}

public class Woman implement Person {
	public Gender getGender() {
		return Gender.FEMALE;
	}
}
```

And you want to inject `Person` into the following class:

```java
public class TomAndJen {
	@Inject 
	private Person tom;

	@Inject
	private Person jen;
}
```

You call the following code:

```java
Genie genie = Genie.create();
TomAndJen tj = genie.get(TomAndJen.class);
```

Genie will raise an exception in this case

```txt
org.osgl.genie.InjectException: Cannot instantiate interface org.osgl.genie.Person

	at org.osgl.genie.Genie.findProvider(Genie.java:293)
	at org.osgl.genie.Genie.fieldInjector(Genie.java:391)
	at org.osgl.genie.Genie.fieldInjectors(Genie.java:377)
	at org.osgl.genie.Genie.buildFMInjector(Genie.java:331)
	at org.osgl.genie.Genie.buildProvider(Genie.java:311)
	at org.osgl.genie.Genie.findProvider(Genie.java:296)
	at org.osgl.genie.Genie.get(Genie.java:264)
	at org.osgl.genie.Genie.get(Genie.java:173)
```

This is because the type `Person` is an interface and Genie doesn't know how to instantiate it.

Now comes to the point, we need to tell Genie to how to use a concrete class to instantiate an interface type, in our case, the `Person`. This process is called type binding. Here is how to do type binding in Genie: 

```java
Genie genie = Genie.create(new org.osgl.genie.Module() {
	@Override
	protected void configure() {
		bind(Perosn.class).to(Man.class);
	}
});
TomAndJen tj = genie.get(TomAndJen.class);
```

Now you will get a `TomAndJen` type bean with both `tom` and `jen` instantiated with `Man` class. Nice! But what if I want to make `jen` to be instantiated with `Woman` class? 

The solution is to define your [Qualifier](http://docs.oracle.com/javaee/6/api/javax/inject/Qualifier.html) annotation and use it to decorate Genie bindings. Here is how to do it:

```java
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@interface Female {}
```

And now update the `TomAndJen` class by tag `jen` field with `Female` annotation:

```java
public class TomAndJen {
	@Inject 
	private Person tom;

	@Inject
	@Female
	private Person jen;
}
```

And we also need to tell Genie to treat it different when `Female` annotation is presented:

```java
Genie genie = Genie.create(new org.osgl.genie.Module() {
	@Override
	protected void configure() {
		bind(Perosn.class).to(Man.class);
		bind(Person.class).annotatedWith(Female.class).to(Woman.class);
	}
});
TomAndJen tj = genie.get(TomAndJen.class);
```

Now the bean `tj` returned from Genie has `tom` instantiated as `Man` and `jen` instantiated as `Woman`.

So above shows how to configure type bindings by extending the `org.osgl.genie.Module` class and use binder API in the `configure` method. Genie also provide another approach to define the type bindings, which use `org.osgl.genie.Provides` annotation on a factory method. And this approach behavior exactly the same as the binder API approach:

```java
Genie genie = Genie.create(new Object() {
	@Provides
	public Person man() {
		return new Man();
	} 
	
	@Provides
	@Female
	public Person woman() {
		return new Woman();
	}
});
TomAndJen tj = genie.get(TomAndJen.class);
```

So far so good. However there is a little problem with the following code:

```java
@Provides
public Person man() {
	return new Man();
}
```

The code above hard code the `Man` instantiation. Which is good for this simple case, but if the class `Man` got it's own dependency injection, then it won't work. And the correct way is:

```java
@Provides
public Person man(Man man) {
    return man;
}
 
@Provides
public Person woman(Woman woman) {
    return woman;
}
```

This way we delegate instantiation of `Man` and `Woman` type bean to Genie and completely free us from dependency injection.

The binder API approach is cleaner and simpler than the `@Provides` factory method approach. However the factory method approach can be used to handle cases that cannot be handled by binder API. E.g. when `Qualifier` annotation has state. Let's rewrite our `TomAndJen` class without using custom defined `@Female` qualifier, instead we use the [no-so-good](https://github.com/google/guice/wiki/BindingAnnotations#user-content-named) `javax.inject.Named` annotation:

```java
public class TomAndJen {
	@Inject 
	@Named("male")
	private Person tom;

	@Inject
	@Named("female")
	private Person jen;
}
```

And immediately we stuck when we writing our binding in the `configure` method. There is no way to tell Genie how to resolve the `@Named("male")` and `@Named("female")` annotation. Now the `@Provider` factory method is the rescue:

```java
class NamedBindings {

	@Provides
	@Named("male")
	public Person man(Man man) {
		return man;
	}
	
	@Provides
	@Named("female")
	public Person woman(Woman woman) {
		return woman;
	}
}

Genie genie = Genie.create(new NamedBindings());
TomAndJen tj = genie.get(TomAndJen.class);
```

Next topic: [how to inject container](container.md).

