package org.osgl.genie;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgl.genie.loader.TypedBeanLoader;

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
        genie = new Genie(new ModuleWithFactories());
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
                bind(TypedBeanLoader.class).to(SimpleTypeBeanLoader.class);
            }
        });
        ErrorDispatcher errorDispatcher = genie.get(ErrorDispatcher.class);
        eq(NotFoundHandler.class.getSimpleName(), errorDispatcher.handle(404));
    }
}
