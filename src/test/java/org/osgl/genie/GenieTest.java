package org.osgl.genie;

import org.junit.Before;
import org.junit.Test;
import org.osgl.genie.ScopedObjects.*;
import org.osgl.genie.loader.TypedElementLoader;

/**
 * Test Genie DI solution
 */
public class GenieTest extends TestBase {

    private Genie genie;

    @Before
    public void setup() {
        genie = Genie.create();
    }

    @Test
    public void testSimpleEmptyConstructor() {
        testSimple(SimpleEmptyConstructor.class);
    }

    @Test
    public void testSimpleConstructorInjection() {
        SimpleConstructorInjection bean = testSimple(SimpleConstructorInjection.class);
        assertNotNull(bean.foo());
    }

    @Test
    public void testSimpleFieldInjection() {
        SimpleFieldInjection bean = testSimple(SimpleFieldInjection.class);
        assertNotNull(bean.foo());
    }

    @Test
    public void testSimpleMethodInjection() {
        SimpleMethodInjection bean = testSimple(SimpleMethodInjection.class);
        assertNotNull(bean.foo());
    }

    @Test(expected = InjectException.class)
    public void itShallCryIfCircularDependencyFound() {
        testSimple(Circular.A.class);
    }

    @Test(expected = InjectException.class)
    public void itShallCryIfSelfCircularDependencyFound() {
        testSimple(Circular.Self.class);
    }

    @Test
    public void testInjectProvider() {
        SimpleConstructorInjectionByProvider bean = testSimple(SimpleConstructorInjectionByProvider.class);
        assertNotNull(bean.foo());
        SimpleMethodInjectionByProvider bean2 = testSimple(SimpleMethodInjectionByProvider.class);
        assertNotNull(bean2.foo());
    }

    @Test
    public void testLoaderAnnotation() {
        FibonacciSeriesHolder bean = genie.get(FibonacciSeriesHolder.class);
        eq("1,1,2,3,5,8,13", bean.toString());
    }

    @Test
    public void testLoaderAndFilterAnnotation() {
        EvenFibonacciSeriesHolder bean = genie.get(EvenFibonacciSeriesHolder.class);
        eq("2,8,34", bean.toString());
    }

    private <T> T testSimple(Class<T> c) {
        T o = genie.get(c);
        eq(c.getSimpleName(), o.toString());
        return o;
    }

    @Test
    public void testModuleWithBindings() {
        genie = new Genie(new ModuleWithBindings());
        testModules();
    }

    @Test
    public void testModuleWithFactoryMethods() {
        genie = new Genie(ModuleWithFactories.class);
        testModules();
    }

    @Test
    public void testModuleWithStaticFactoryMethods() {
        genie = new Genie(ModuleWithStaticFactories.class);
        testModules();
    }

    private void testModules() {
        Person person = genie.get(Person.class);
        no(person.gender().isFemale());
        Person.Family family = genie.get(Person.Family.class);
        no(family.dad.gender().isFemale());
        yes(family.mom.gender().isFemale());
        assertNull(family.son);
        assertNull(family.daughter);
    }

    @Test
    public void testNamedInjection() {
        genie = new Genie(new ModuleWithNamedBindings());
        TomAndJen tj = genie.get(TomAndJen.class);
        no(tj.tom.gender().isFemale());
        yes(tj.jen.gender().isFemale());
    }

    @Test
    public void testMapInjection() {
        genie = new Genie(new Module() {
            @Override
            protected void configure() {
                bind(TypedElementLoader.class).to(SimpleTypeElementLoader.class);
            }
        });
        ErrorDispatcher errorDispatcher = genie.get(ErrorDispatcher.class);
        eq(NotFoundHandler.class.getSimpleName(), errorDispatcher.handle(404));
    }

    @Test
    public void testSingletonScope() {
        genie = Genie.create(ScopedFactory.class);

        // Test Annotation on Type
        SingletonObject obj = genie.get(SingletonObject.class);
        SingletonObject obj2 = genie.get(SingletonObject.class);
        same(obj, obj2);

        // Test Annotation on Factory method
        SingletonProduct product = genie.get(SingletonProduct.class);
        SingletonProduct product2 = genie.get(SingletonProduct.class);
        same(product, product2);

        // Test Annotation in Binder
        SingletonBoundObject bound = genie.get(SingletonBoundObject.class);
        SingletonBoundObject bound2 = genie.get(SingletonBoundObject.class);
        same(bound, bound2);
    }

    @Test
    public void testSessionScope() {
        genie = Genie.create(ScopedFactory.class);


        // Session 1:
        // Test annotation on Type
        Context s1 = new Context();
        Context.set(s1);
        SessionObject bean = genie.get(SessionObject.class);
        SessionObject bean2 = genie.get(SessionObject.class);
        same(bean, bean2);
        // Test annotation on Factory method
        SessionProduct product = genie.get(SessionProduct.class);
        SessionProduct product2 = genie.get(SessionProduct.class);
        same(product, product2);

        // Session 2:
        // Test annotation on type
        Context s2 = new Context();
        Context.set(s2);
        SessionObject bean3 = genie.get(SessionObject.class);
        SessionObject bean4 = genie.get(SessionObject.class);
        same(bean3, bean4);
        no(bean == bean3);
        // Test annotation on Factory method
        SessionProduct product3 = genie.get(SessionProduct.class);
        SessionProduct product4 = genie.get(SessionProduct.class);
        same(product3, product4);
        no(product == product3);

    }
}
