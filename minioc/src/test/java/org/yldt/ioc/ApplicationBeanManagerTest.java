/*
 * Copyright 2012 the original author or authors.
 *
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
 */ 
package org.yldt.ioc;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.concurrent.Callable;

import org.junit.Assert;
import org.junit.Test;

/**
 * 
 * @author Yun Liu
 *
 */
public class ApplicationBeanManagerTest {

    @Test
    public void testResourceManagerAvailable() {
        ApplicationBeanManager manager = new ApplicationBeanManager();
        assertTrue("Expect to be able to obtain resourceManager",
                manager == manager.getBean(ResourceManager.class));
    }

    @Test
    public void testSimpleInject() {
        ApplicationBeanManager manager = new ApplicationBeanManager();
        manager.bind(SimpleClass.class);
        SimpleClass bean = manager.getBean(SimpleClass.class);
        assertTrue("Expect getBean always return the same insance",
                bean == manager.getBean(SimpleClass.class));
        assertTrue("Expect simple bean has ResourceManager populated", manager == bean.getManager());
    }

    @Test
    public void testSlightlyMoreComplicatedCase() {
        ApplicationBeanManager manager = new ApplicationBeanManager();
        manager.bind(SimpleClass.class);
        manager.bind(MoreComplicatedClass.class);
        MoreComplicatedClass bean = manager.getBean(MoreComplicatedClass.class);
        assertTrue("Expect getBean always return the same insance",
                bean == manager.getBean(MoreComplicatedClass.class));
        assertTrue("Expect more complicate bean has Simple bean populated",
                bean.getSimpleClass() != null);
        assertTrue("Expect more complicate bean has Simple bean populated",
                bean.getSimpleClass() == manager.getBean(SimpleClass.class));
    }

    @Test
    public void testOnStartCalledOnce() {
        ApplicationBeanManager manager = new ApplicationBeanManager();
        manager.bind(ExpectOnStartBean.class);
        ExpectOnStartBean bean = manager.getBean(ExpectOnStartBean.class);
        manager.getBean(ExpectOnStartBean.class);
        Assert.assertEquals(1, bean.getOnStartCount());
    }

    @Test
    public void testShutdownDoesNotStartResource() {
        ApplicationBeanManager manager = new ApplicationBeanManager();
        manager.bind(ExpectOnStartBean.class);
        manager.shutdown();
    }

    @Test
    public void testApiTestBean()
    {
        ApplicationBeanManager manager = new ApplicationBeanManager();
        manager.bind(ApiTestBean.class, Runnable.class);
        manager.shutdown();

        manager = new ApplicationBeanManager();
        manager.bind(ApiTestBean.class, Runnable.class, Callable.class);
        manager.shutdown();

        try
        {
            manager = new ApplicationBeanManager();
            manager.bind(ApiTestBean.class, Runnable.class, List.class);
            fail("Expect IllegalArgumentException");
        } catch (IllegalArgumentException e)
        {
            // pass
        }

        try
        {
            manager = new ApplicationBeanManager();
            manager.bind(ApiTestBean.class, List.class);
            fail("Expect IllegalArgumentException");
        } catch (IllegalArgumentException e)
        {
            // pass
        }
    }

    @Test
    public void testBadBeanTypeOnGetBean()
    {
        String defaultBeanName = ApiTestBean.class.getName();

        ApplicationBeanManager manager = new ApplicationBeanManager();
        manager.bind(ApiTestBean.class, ApiTestBean.class, Runnable.class);
        assertTrue(manager.getBean(defaultBeanName, Runnable.class) instanceof ApiTestBean);
        try
        {
            manager.getBean(defaultBeanName, ApplicationBeanManager.class);
            fail("Expect error");
        } catch (ResourceException e)
        {
            // pass
        }
    }

    @Test
    public void testFieldAndMethodInjection() 
    {
        ApplicationBeanManager manager = new ApplicationBeanManager();
        manager.bind(FieldAndMethodInjectionBean.class);
        manager.bind(SimpleClass.class);
        FieldAndMethodInjectionBean bean = manager.getBean(FieldAndMethodInjectionBean.class);
        SimpleClass toBeInjected = manager.getBean(SimpleClass.class);
        assertTrue(bean.simpleClass == toBeInjected);
        assertTrue(bean.injectWithMethod == toBeInjected);
        assertTrue(toBeInjected != null);
    }
}
