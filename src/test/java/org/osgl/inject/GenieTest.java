package org.osgl.inject;

/*-
 * #%L
 * OSGL Genie
 * %%
 * Copyright (C) 2017 OSGL (Open Source General Library)
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgl.inject.ScopedObjects.*;
import org.osgl.inject.annotation.*;
import org.osgl.inject.loader.ConfigurationValueLoader;
import org.osgl.inject.loader.TypedElementLoader;
import osgl.ut.TestBase;

import java.lang.annotation.*;
import java.lang.annotation.ElementType;
import java.util.Collection;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.ValidationException;
import javax.validation.constraints.AssertTrue;

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
    public void testInitCollection() {
        Collection col = genie.get(Collection.class);
        yes(col instanceof Collection);
        col.add(new Object());
        same(1, col.size());
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

    @Test(expected = CircularReferenceException.class)
    public void itShallIgnoreSelfCircularDependency() {
        Circular.Self o = testSimple(Circular.Self.class);
        isNull(o.self);
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
    public void testDirectElementLoaderAnnotation() {
        FibonacciSeriesHolder3 bean = genie.get(FibonacciSeriesHolder3.class);
        eq("1,1,2,3,5,8", bean.toString());
    }

    @Test
    public void testElementLoaderAndFilterAnnotation() {
        EvenFibonacciSeriesHolder bean = genie.get(EvenFibonacciSeriesHolder.class);
        eq("2,8,34", bean.toString());
    }

    @Test
    public void testElementLoaderAndReverseFilterAnnotation() {
        OddFibonacciSeriesHolder bean = genie.get(OddFibonacciSeriesHolder.class);
        eq("1,1,3,5,13,21,55,89", bean.toString());
    }

    private <T> T testSimple(Class<T> c) {
        T o = genie.get(c);
        yes(c.isInstance(o));
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
    public void testConstructorBinding() {
        genie = new Genie(ModuleWithConstructorBinding.class);
        Person person = genie.get(Person.class);
        yes(person.gender().isFemale());
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
    public void testDynamicProvider() {
        genie = new Genie(LeatherSmoother.DynamicModule.class);
        genie.supportInjectionPoint(true);
        LeatherSmoother.Host bean = genie.get(LeatherSmoother.Host.class);
        yes(bean.smoother instanceof LeatherSmoother.RedLeatherSmoother);
    }

    @Test
    public void testTypeOfListAndMapInjection() {
        genie = new Genie(new Module() {
            @Override
            protected void configure() {
                bind(TypedElementLoader.class).to(SimpleTypeElementLoader.class);
            }
        }, ScopedFactory.class);
        ErrorDispatcher errorDispatcher = genie.get(ErrorDispatcher.class);
        eq(NotFoundHandler.class.getSimpleName(), errorDispatcher.handle(404));
        eq(NotFoundHandler.class.getSimpleName(), errorDispatcher.handle2(404));
        eq(NotFoundHandler.class.getSimpleName(), errorDispatcher.handle3("not-found"));
        eq(2, errorDispatcher.handlerList.size());
        yes(errorDispatcher.handlerList.contains(genie.get(InternalErrorHandler.class)));
        yes(errorDispatcher.handlerList.contains(genie.get(NotFoundHandler.class)));
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

        // Test Annotation in Binder
        SingletonBoundObject bound = genie.get(SingletonBoundObject.class);
        SingletonBoundObject bound2 = genie.get(SingletonBoundObject.class);
        same(bound, bound2);

        // Test Annotation on Factory method
        SingletonProduct product = genie.get(SingletonProduct.class);
        SingletonProduct product2 = genie.get(SingletonProduct.class);
        same(product, product2);
    }

    @Test(expected = InjectException.class)
    public void testConflictScope() {
        genie = Genie.create(ScopedFactory.class);
        genie.registerScopeAlias(Singleton.class, InheritedStateless.class);
        ConflictedScope conflictedScope = genie.get(ConflictedScope.class);
    }

    @Test
    public void testInheritedScopeStopper() {
        genie = Genie.create(ScopedFactory.class);
        genie.registerScopeAlias(Singleton.class, InheritedStateless.class);
        genie.registerScopeAlias(StopInheritedScope.class, Stateful.class);

        // Test inherited scope
        StatelessBar bar = genie.get(StatelessBar.class);
        StatelessBar bar2 = genie.get(StatelessBar.class);
        same(bar, bar2);

        // Test direct stop inherited scope
        StatefulZee zee = genie.get(StatefulZee.class);
        StatefulZee zee2 = genie.get(StatefulZee.class);
        assertNotSame(zee, zee2);

        // Test indirect stop inherited scope
        StatefulFoo foo = genie.get(StatefulFoo.class);
        StatefulFoo foo2 = genie.get(StatefulFoo.class);
        assertNotSame(foo, foo2);

        // Test compatible multiple scope annotation
        CompatibleScope compatibleScope = genie.get(CompatibleScope.class);
        CompatibleScope compatibleScope2 = genie.get(CompatibleScope.class);
        same(compatibleScope, compatibleScope2);
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

    private static class SimpleConfigurationValueLoader extends ConfigurationValueLoader {
        @Override
        protected Object conf(String key, String defaultValue) {
            return key.length();
        }
    }

    private static class ConfigurationValueModule {
        @Provides
        public ConfigurationValueLoader get() {
            return new SimpleConfigurationValueLoader();
        }
    }

    private static class Configured {
        @Configuration("foo.bar")
        int n;
    }

    @Test
    public void testLoadConfigurationValue() {
        genie = new Genie(new ConfigurationValueModule());
        Configured obj = genie.get(Configured.class);
        eq(7, obj.n);
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
    @InjectTag
    @LoadValue(BoolValueLoader.class)
    public @interface Bool {
        boolean value() default false;
    }

    private static class BoolValueLoader extends ValueLoader.Base<Boolean> {

        @Override
        public Boolean get() {
            return value();
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
        @Bool
        @MyAssertTrue
        private boolean val;
    }

    static class BarToPass {
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

    @Test
    public void testInjectListener() {
        genie = new Genie(new DaoInjectListener());
        UserService userService = genie.get(UserService.class);
        eq(userService.dao.modelType(), User.class);
        OrderService orderService = genie.get(OrderService.class);
        eq(orderService.dao.modelType(), Order.class);
    }

    @Test
    public void testMultipleConstructors() {
        MultipleConstructors mc = genie.get(MultipleConstructors.class);
        yes(mc.hasOrder());
        no(mc.hasId());
    }

    static class LuckyNumberValueLoader extends ValueLoader.Base<Integer> {
        @Override
        public Integer get() {
            return 666666;
        }
    }

    static class DirectLoadValueTestBed {
        @LoadValue(LuckyNumberValueLoader.class)
        int luckyNumber;

        public int getLuckyNumber() {
            return luckyNumber;
        }
    }

    @Test
    public void testDirectLoadValue() {
        DirectLoadValueTestBed target = genie.get(DirectLoadValueTestBed.class);
        eq(666666, target.getLuckyNumber());
    }
}
