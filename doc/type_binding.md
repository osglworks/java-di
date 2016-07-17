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
Genie genie = new Genie();
TomAndJen tj = genie.get(TomAndJen.class);
```

You will find Genie give you an error happily:

```txt
org.osgl.genie.InjectException: Cannot instantiate interface Person

	at org.osgl.genie.Genie.findProvider(Genie.java:399)
	at org.osgl.genie.Genie.get(Genie.java:369)
	at org.osgl.genie.Genie.get(Genie.java:324)
```

This is because the type `Person` is an interface and Genie doesn't know how to instantiate it.

Now comes to the point, we need to tell Genie to bind type `Person` to a concrete implementation type, e.g. `Man`. Here is how we get it done:

```java
Genie genie = new Genie(new org.osgl.genie.Module() {
	@Override
	protected void configure() {
		bind(Perosn.class).to(Man.class);
	}
});
TomAndJen tj = genie.get(TomAndJen.class);
```

Now you will get a `TomAndJen` type bean with both `tom` and `jen` instantiated as `Man` class. Nice! But what if I want to make `jen` be instantiated as `Woman` class? 

The solution is to define your [Qualifier](http://docs.oracle.com/javaee/6/api/javax/inject/Qualifier.html) annotation and use it to decorate Genie bindings. Here is how we do it:

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
Genie genie = new Genie(new org.osgl.genie.Module() {
	@Override
	protected void configure() {
		bind(Perosn.class).to(Man.class);
		bind(Person.class).annotatedWith(Female.class).to(Woman.class);
	}
});
TomAndJen tj = genie.get(TomAndJen.class);
```

Now the bean `tj` returned from Genie has `tom` instantiated as `Man` and `jen` instantiated as `Woman`.

So above shows how to configure type bindings by extending the `org.osgl.genie.Module` class and use binder API in the `configure` method. Genie also provide another approach to define the type bindings, which use `org.osgl.genie.Provides` annotation on a factory method. And this approach behavior exactly the same as using the binder API:

```java
Genie genie = new Genie(new Object() {
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

The code above again hard code the `Man` instantiation. Which is good for this simple case, but if the class `Man` got it's own dependency injection, then it won't work. So the correct way is:

```java
Genie genie = new Genie(new Object() {
	@Provides
	public Person man(Man man) {
		return man;
	} 
	
	@Provides
	@Female
	public Person woman(Woman woman) {
		return woman;
	}
});
TomAndJen tj = genie.get(TomAndJen.class);
```

This way we delegate instantiation of `Man` and `Woman` type bean to Genie and completely free us from dependency injection.

So it looks to me the binder API approach is cleaner and simpler than the `@Provides` factory method approach why do we need the latter. The reason is because binder API cannot handle the case when `Qualifier` annotation has state. Let's rewrite our `TomAndJen` class without using custom defined `@Female` qualifier, instead we use the [no-so-good](https://github.com/google/guice/wiki/BindingAnnotations#user-content-named) standard `javax.inject.Named` annotation:

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

And immediately we stucked when we writing our binding in the `configure` method. There is no way to tell Genie how to resolve the `@Named("male")` and `@Named("female")` annotation. Now the `@Provider` factory method is the rescure:

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

Genie genie = new Genie(new NamedBindings());
TomAndJen tj = genie.get(TomAndJen.class);
```

Now you understand how to use module to configure Genie for type bindings, let's keep moving on with [container injection and customized bean loading](beanloader.md).

