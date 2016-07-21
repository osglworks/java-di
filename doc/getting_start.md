# Getting started

Suppose you have the the following Java classes:

```java
class Foo {}

class Bar {
	Foo foo;
	public Bar(Foo foo) {
		this.foo = foo;
	}
}
```

Now you need an instance of `Bar`. Here is how you do that in an ordinary Java application:

```java
Foo foo = new Foo();
Bar bar = new Bar(foo);
```

The problem with above code is you hard code the dependency management into your application and it makes it hard to refactor the code when you want to replace the current `Foo` implementation with another `SuperFoo` implementation. And if your `Foo` is a very complicated object and is expensive to construct but you want to unit test `Bar` then you also introduce unnecessary complexity into unit test.

Now here is how Genie and other JSR330 DI solutions handle the case. First add `javax.inject.Inject` annotation to your `Bar` class:

```java
class Bar {
	private Foo foo;
	@Inject
	public Bar(Foo foo) {
		this.foo = foo;
	}
}
```

And now Genie way to get your `Bar` instance:

```java
Genie genie = Genie.create(); 
Bar bar = genie.get(Bar.class);
```

An important thing is the application developer doesn't need to touch `Foo`, the dependency of `Bar`. Genie manage to instantiate the instance of `Foo` when it need it to construct the `Bar` instance. This abstraction leaves the space for application developer to centralize the configuration of dependency management and they doesn't need to get distracted from that when programming the business logic. It also makes it flexible to replace implementations if needed. See [Type binding](type_binding.md) for more details.

In the above `Bar` code we have implemented one of three kind of injections: Constructor injection. Genie also support the other two injection types:

Field injection:

```java
class Bar {
	@Inject
	private Foo foo
}
```

Method injection:

```java
class Bar {
	private Foo foo;
	@Inject
	public void foo(Foo foo) {
		this.foo = foo;
	}
}
```

Here are some points about the three injection types:

* Field/Method injection will be ignored when Constructor injection presented
* Field injection is not very unit test friendly because it force you to create new module to inject your mock dependencies.
* Method injection is not preferred when you favor immutability or you want to protect your object internal state

Whatever injection type you are using in your application, use it consistently. 

Now that you have some basic idea of dependency injection with Genie, you can move to the [next chapter](type_binding.md) to see how to use Genie to bind your interface to a concrete implementation.

