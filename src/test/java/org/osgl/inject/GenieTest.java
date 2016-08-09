package org.osgl.inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.osgl.$;
import org.osgl.exception.NotAppliedException;
import org.osgl.inject.ScopedObjects.*;
import org.osgl.inject.annotation.LoadValue;
import org.osgl.inject.annotation.PostConstructProcess;
import org.osgl.inject.annotation.Provided;
import org.osgl.inject.annotation.TypeOf;
import org.osgl.inject.loader.TypedElementLoader;
import org.osgl.util.C;
import org.osgl.util.S;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.validation.ValidationException;
import javax.validation.constraints.AssertTrue;
import java.lang.annotation.*;
import java.lang.annotation.ElementType;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

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
        BaseWithPostConstructor.reset();
        Context.reset();
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
    public void testGeneralQualifier() {
        genie = new Genie(LeatherSmoother.Module.class);
        LeatherSmoother.Host bean = genie.get(LeatherSmoother.Host.class);
        yes(bean.smoother instanceof LeatherSmoother.RedLeatherSmoother);
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
        genie.get(SingletonPostConstruct.class);
        eq(1, SingletonPostConstruct.instances.get());
        genie.get(SingletonPostConstruct.class);
        eq(1, SingletonPostConstruct.instances.get());

        Context c1 = new Context();
        Context.set(c1);
        eq(0, SessionPostConstruct.instances.get());
        genie.get(SessionPostConstruct.class);
        eq(1, SessionPostConstruct.instances.get());
        genie.get(SessionPostConstruct.class);
        eq(1, SessionPostConstruct.instances.get());

        Context.set(new Context());
        genie.get(SessionPostConstruct.class);
        eq(2, SessionPostConstruct.instances.get());
        genie.get(SessionPostConstruct.class);
        eq(2, SessionPostConstruct.instances.get());
    }

    private static class AssertTrueHandler implements PostConstructProcessor<Boolean> {
        @Override
        public void process(Boolean bean, Annotation annotation) {
            if (null == bean || !bean) {
                throw new ValidationException("true expected");
            }
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.PARAMETER, ElementType.FIELD})
    @Documented
    @LoadValue(BoolValueLoader.class)
    public @interface Bool {
        boolean value() default false;
    }

    private static class BoolValueLoader implements ValueLoader<Boolean> {
        @Override
        public Boolean load(Map options, BeanSpec spec) {
            return (Boolean) options.get("value");
        }
    }

    static class FooToFail {
        @AssertTrue
        @Bool
        @Inject
        private boolean val;
    }

    static class FooToPass {
        @AssertTrue
        @Bool(true)
        @Inject
        private boolean val;
    }

    @Test
    public void testRegisteredPostConstructProcessor() {
        genie.registerPostConstructProcessor(AssertTrue.class, new AssertTrueHandler());
        yes(genie.get(FooToPass.class).val);
        try {
            genie.get(FooToFail.class);
            fail("Expect ValidationException here");
        } catch (ValidationException e) {
            // test pass
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.PARAMETER, ElementType.FIELD})
    @PostConstructProcess(AssertTrueHandler.class)
    @Documented
    public @interface MyAssertTrue {
    }

    static class BarToFail {
        @Inject
        @Bool
        @MyAssertTrue
        private boolean val;
    }

    static class BarToPass {
        @Inject
        @Bool(true)
        @MyAssertTrue
        private boolean val;
    }

    @Test
    public void testAnnotatedPostConstructProcessor() {
        yes(genie.get(BarToPass.class).val);
        try {
            genie.get(BarToFail.class);
            fail("Expect ValidationException here");
        } catch (ValidationException e) {
            // test pass
        }
    }

    @Test
    public void testArrayInject() {
        FibonacciSeriesHolder2 bean = genie.get(FibonacciSeriesHolder2.class);
        eq("1,1,2,3,5,8,13", bean.toString());
    }

}
