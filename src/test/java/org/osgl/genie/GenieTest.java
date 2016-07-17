package org.osgl.genie;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgl.genie.builder.ListBuilder;

/**
 * Test Air Genie DI solution
 */
public class GenieTest extends TestBase {

    private Genie genie;

    @Before
    public void setup() {
        genie = new Genie();
        Builder.Factory.Manager.found(ListBuilder.Factory.class);
    }

    @After
    public void teardown() {
        Builder.Factory.Manager.destroy();
    }

    @Test
    public void testSimpleEmptyConstructor() {
        test(SimpleEmptyConstructor.class);
    }

    @Test
    public void testSimpleConstructorInjection() {
        SimpleConstructorInjection bean = test(SimpleConstructorInjection.class);
        assertNotNull(bean.foo());
    }

    @Test
    public void testSimpleFieldInjection() {
        SimpleFieldInjection bean = test(SimpleFieldInjection.class);
        assertNotNull(bean.foo());
    }

    @Test
    public void testSimpleMethodInjection() {
        SimpleMethodInjection bean = test(SimpleMethodInjection.class);
        assertNotNull(bean.foo());
    }

    @Test(expected = InjectException.class)
    public void itShallCryIfCircularDependencyFound() {
        test(Circular.A.class);
    }

    @Test(expected = InjectException.class)
    public void itShallCryIfSelfCircularDependencyFound() {
        test(Circular.Self.class);
    }

    @Test
    public void testBeanLoaderAnnotation() {
        FibonacciSeriesHolder bean = genie.get(FibonacciSeriesHolder.class);
        eq("1,1,2,3,5,8,13", bean.toString());
    }

    @Test
    public void testBeanLoaderAndFilterAnnotation() {
        EvenFibonacciSeriesHolder bean = genie.get(EvenFibonacciSeriesHolder.class);
        eq("2,8,34", bean.toString());
    }

    @Test
    public void testModuleWithBindings() {
        Module module = new Module() {
            @Override
            protected void configure() {
                bind(Person.class).to(Person.Man.class);
                bind(Person.class).withAnnotation(Person.Female.class).to(Person.Woman.class);
            }
        };
        genie = new Genie(module);
        Person person = genie.get(Person.class);
        no(person.gender().isFemale());
        Person.Family family = genie.get(Person.Family.class);
        no(family.dad.gender().isFemale());
        yes(family.mom.gender().isFemale());
        assertNull(family.son);
        assertNull(family.daughter);
    }

    private <T> T test(Class<T> c) {
        T o = genie.get(c);
        eq(c.getSimpleName(), o.toString());
        return o;
    }
}
