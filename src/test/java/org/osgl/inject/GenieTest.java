package org.osgl.inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgl.$;
import org.osgl.inject.ScopedObjects.*;
import org.osgl.inject.annotation.TypeOf;
import org.osgl.inject.loader.TypedElementLoader;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Test Genie DI solution
 */
public class GenieTest extends TestBase {

    private Genie genie;

    @Before
    public void setup() {
        genie = Genie.create();
    }

    @After
    public void teardown() {
        BaseWithPostConstructor.current.remove();
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
    public void testElementLoaderAnnotation() {
        FibonacciSeriesHolder bean = genie.get(FibonacciSeriesHolder.class);
        eq("1,1,2,3,5,8,13", bean.toString());
    }

    @Test
    public void testElementLoaderAndFilterAnnotation() {
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
    public void testValueLoaderAnnotation() {
        RandomListHolder holder = genie.get(RandomListHolder.class);
        eq(holder.list().size(), 10);
    }

    @Test(expected = InjectException.class)
    public void itShallReportErrorIfValueLoaderUsedAlongWithOtherQualifiers() {
        genie.get(ValueLoaderAndQualifiers.class);
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

    @Test
    public void testCDIScope() {
        genie = new Genie(CDIScopedFactory.class);

        Context s1 = new Context();
        Context.set(s1);

        CDIScopedFactory.ProductHolder holder = genie.get(CDIScopedFactory.ProductHolder.class);
        yes(holder.product instanceof JEESessionObject);

        SessionProduct product = genie.get(SessionProduct.class);
        SessionProduct product2 = genie.get(SessionProduct.class);
        same(product, product2);

        Context s2 = new Context();
        Context.set(s2);
        SessionProduct product3 = genie.get(SessionProduct.class);
        SessionProduct product4 = genie.get(SessionProduct.class);
        same(product3, product4);
        no(product == product3);
    }

    void methodX(@TypeOf List<ErrorHandler> handlers, @Person.Female Person person) {
    }

    @Test
    public void testGetParams() throws Exception {
        genie = new Genie(new ModuleWithBindings(), new Module() {
            @Override
            protected void configure() {
                bind(TypedElementLoader.class).to(SimpleTypeElementLoader.class);
            }
        });
        Method methodX = GenieTest.class.getDeclaredMethod("methodX", new Class[]{List.class, Person.class});
        Object[] params = genie.getParams(methodX);
        eq(2, params.length);
        List<ErrorHandler> handlers = $.cast(params[0]);
        eq(2, handlers.size());
        Person person = $.cast(params[1]);
        yes(person.gender().isFemale());
    }

    @Test
    public void testAnnotatedWithLoader() throws Exception {
        genie = new Genie(AnnotatedClasses.class);
        AnnotatedClasses bean = genie.get(AnnotatedClasses.class);
        eq(1, bean.getPublicBeans().size());
        eq(2, bean.getWithPrivateBeans().size());
        eq(3, bean.getWithPrivateAndAbstractClasses().size());
    }

    @Test
    public void testTypedLoader() throws Exception {
        genie = new Genie(TypedClasses.class);
        TypedClasses bean = genie.get(TypedClasses.class);
        eq(1, bean.publicImpls.size());
        eq(2, bean.withNonPublic.size());
        eq(4, bean.allBaseTypes.size());
    }

    @Test
    public void testPostConstructor() {
        genie.get(BaseWithPostConstructor.Holder.class);
        assertNotNull(BaseWithPostConstructor.current.get());
    }

    @Test
    public void testDerivedPostConstructor() {
        genie = new Genie(DerivedFromBaseWithPostConstructor.Module.class);
        genie.get(DerivedFromBaseWithPostConstructor.Holder.class);
        BaseWithPostConstructor bean = BaseWithPostConstructor.current.get();
        assertNotNull(bean);
        yes(bean instanceof DerivedFromBaseWithPostConstructor);
    }

    @Test
    public void testOverwritePostConstructor() {
        genie = new Genie(OverwriteBaseWithPostConstructor.Module.class);
        OverwriteBaseWithPostConstructor.Holder holder = genie.get(OverwriteBaseWithPostConstructor.Holder.class);
        yes(holder.bean instanceof OverwriteBaseWithPostConstructor);
        BaseWithPostConstructor bean = BaseWithPostConstructor.current.get();
        assertNull(bean);
    }

    @Test
    public void testScopedPostConstruct() {
        genie = new Genie(ScopedFactory.class);
        eq(0, SingletonPostConstruct.instances.get());
        SingletonPostConstruct s1 = genie.get(SingletonPostConstruct.class);
        eq(1, SingletonPostConstruct.instances.get());
        SingletonPostConstruct s2 = genie.get(SingletonPostConstruct.class);
        eq(1, SingletonPostConstruct.instances.get());

        Context c1 = new Context();
        Context.set(c1);
        eq(0, SessionPostConstruct.instances.get());
        SessionPostConstruct a = genie.get(SessionPostConstruct.class);
        eq(1, SessionPostConstruct.instances.get());
        SessionPostConstruct b = genie.get(SessionPostConstruct.class);
        eq(1, SessionPostConstruct.instances.get());

        Context.set(new Context());
        SessionPostConstruct c = genie.get(SessionPostConstruct.class);
        eq(2, SessionPostConstruct.instances.get());
        SessionPostConstruct d = genie.get(SessionPostConstruct.class);
        eq(2, SessionPostConstruct.instances.get());
    }

}
