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

import java.util.List;

import org.yldt.ioc.annotations.Bean;

/**
 * ResourceManager is a bean registry and bean factory. Bean definitions(object or class) 
 * can be registered with the ResourceManager using one of the <code>bind()</code> methods. 
 * Beans instances(if needed) are not created until one of the <code>getBean()<code> or
 * the {@link #listBeans()} method, or {@link #startAll()} method is called. 
 * 
 * Calling the {@link #startAll()} method has the effect of obtaining 
 * creating all the beans in the ResourceManager.
 * 
 * If a bean implements {@link LifeCycle}, the {@link LifeCycle#onStart() method is
 * guaranteed to be called before the bean is returned to the called. The {@link LifeCycle#onShutdown()} 
 * method will be called when the {@link #shutdown()} method is called.
 * 
 * 
 * Bean names in the resource manager must be unique.
 * 
 * @author Yun Liu
 *  
 */
public interface ResourceManager {
    public static enum State {
        Running,
        Shutdown;
    }
    
    /**
     * Bind the <code>componentType</code> as a bean resource. If the type is not annotated with 
     * {@link Bean} annotation or the {@link Bean#name()} are not specified ,
     * the <code>componentType<code> itself is used to generate the bean name 
     * with <code>componentType.getName()</code>.
     * 
     * If the <code>componentType</code> annotated with {@link Bean} and {@link Bean#name()} are specified ,
     * the component is bind using the name provided by the annotation. 
     * 
     * A new singleton instance of the component will be constructed when the <code>getBean()</code>
     * method is invoked.
     * 
     * The <code>componentType</code> must have exactly one public constructor. If the constructor
     * takes arguments, the arguments must be beans maintained by this ResourceManager. 
     * 
     * @param componentType component class type. 
     */
    public void bind(Class<?> componentType);

    /**
     * Bind the <code>componentType<code> as a bean resource with name 
     * <code>interfaceType.getName()</code> and each of the class name of the additional 
     * types as names for the bean.
     * 
     * If <code>componentType<code> is not an instance of the <code>interfaceType</code> or 
     * instance of all the <code>additionalTypes</code>, the method throws {@link IllegalArgumentException}.
     * 
     * A new singleton instance of the component will be constructed when the <code>getBean()</code>
     * method is invoked.
     * 
     * The <code>componentType</code> must have exactly one public constructor. If the constructor
     * takes arguments, the arguments must be beans maintained by this ResourceManager. 
     * 
     * @param componentType component class type. 
     * @param interfaceType must be a interface or super class of component class type
     * @param additionalTypes must be a interface or super class of component class type
     */
    public void bind(Class<?> componentType, Class<?> interfaceType, Class<?>... additionalTypes);

    /**
     * Bind the <code>componentType</code> as a bean resource with name <code>name</code>
     * and <code>additionalNames</code> if available.
     * 
     * A new singleton instance of the component will be constructed when the <code>getBean()</code>
     * method is invoked.
     * 
     * The <code>componentType</code> must have exactly one public constructor. If the constructor
     * takes arguments, the arguments must be beans maintained by this ResourceManager. 
     * 
     * Bean name must be unique for each instance of ResourceManager. The method throws 
     * {@link ResourceException} when a duplicate name is detected.
     * 
     * @param componentType component class type.
     * @param name bean name
     * @param additionalNames additional bean name.
     */
    public void bind(Class<?> componentType, String name, String... additionalNames);

    /**
     * Bind the provided object to the ResourceManager with name <code>component.getClass().getName()</code>
     * 
     * Bean name must be unique for each instance of ResourceManager. The method throws 
     * {@link ResourceException} when a duplicate name is detected.
     * 
     * If the bean instance implements {@link LifeCycle}, the {@link LifeCycle#onStart()} will be invoked
     * before an instance is returned to the caller with one of the getBean() method.
     * 
     * @param component component instance
     */
    public void bindObject(Object component);

    /**
     * Bind the provided object to the ResourceManager with name <code>name</code>
     * and <code>additionalNames</code> if available.
     * 
     * Bean name must be unique for each instance of ResourceManager. The method throws 
     * {@link ResourceException} when a duplicate name is detected.
     * 
     * If the bean instance implements {@link LifeCycle}, the {@link LifeCycle#onStart()} will be invoked
     * before an instance is returned to the caller with one of the getBean() method.
     * 
     * @param component component instance
     * @param name bean name
     * @param additionalNames additional names
     */
    public void bindObject(Object component, String name, String... additionalNames);

    /**
     * Bind the provided object to the ResourceManager as a bean resource with name 
     * <code>interfaceType.getName()</code> and each of the class name of the additional 
     * types as names for the bean.
     * 
     * Bean name must be unique for each instance of ResourceManager. The method throws 
     * {@link ResourceException} when a duplicate name is detected.
     * 
     * If the bean instance implements {@link LifeCycle}, the {@link LifeCycle#onStart()} will be invoked
     * before an instance is returned to the caller with one of the getBean() method.
     * 
     * @param component component instance
     * @param interfaceType must be a interface or super class of component class type
     * @param additionalTypes must be a interface or super class of component class type
     */
    public void bindObject(Object component, Class<?> interfaceType, Class<?>... additionalTypes);

    /**
     * Obtain a bean instance registered for the type(more precisely, registered as <code>componentType.getClass().getName()</code>).
     * 
     * 
     * The method throws {@link ResourceException} if the ResourceManager fails to construct the bean. If the ResourceManager failed
     * to obtain dependent beans, the method throws {@link UnsatisfiedDependencyException}.
     * 
     * If the bean implements {@link LifeCycle}, the bean's {@link LifeCycle#onStart()} method is called when it is first retrieved.
     * 
     * @param componentType component type
     * @return a bean instance 
     */
    public <T> T getBean(Class<T> componentType);

    /**
     * Obtain a bean instance registered for the type(more precisely, registered as <code>componentType.getClass().getName()</code>).
     * 
     * 
     * The method throws {@link ResourceException} if the ResourceManager fails to construct the bean. If the ResourceManager failed
     * to obtain dependent beans, the method throws {@link UnsatisfiedDependencyException}.
     * 
     * If the bean bind to the name <code>name</code> is not an instance of <code>componentType</code>, the method throws
     * {@link ResourceException}.
     * 
     * If the bean implements {@link LifeCycle}, the bean's {@link LifeCycle#onStart()} method is called when it is first retrieved.
     * 
     * @param name bean name
     * @param componentType expected component type
     * @return a bean instance 
     */
    public <T> T getBean(String name, Class<T> componentType);

    /**
     * The state of the ResourceManager. The ResourceManager is in {@link State#Running} until the {@link #shutdown()} method
     * is called.
     * 
     * @return the current state of the ResourceManager.
     */
    public State getState();
    
    /**
     * Starts all the beans in the ResourceManager. 
     * For any bean that implements {@link LifeCycle}, the bean's {@link LifeCycle#onStart()} method 
     * is called if this is first time the bean is retrieved.
     * 
     * The method returns all the maintained bean in the ResourceManager in no particular order.
     * 
     * @return a collection of all the beans currently maintained in the ResourceManager.
     */
    public List<Object> listBeans();
    
    /**
     * Starts all the beans in the ResourceManager. 
     * For any bean that implements {@link LifeCycle}, the bean's {@link LifeCycle#onStart()} method 
     * is called if this is first time the bean is retrieved.
     * 
     * This method is not required for the ResourceManager to function. 
     * 
     */
    public void startAll();

    /**
     * Shutdown the ResourceManager. For any bean that implements {@link LifeCycle} and is already started,
     * the bean's {@link LifeCycle#onShutdown()} method is called.
     * Once this method is called, the ResourceManager is no longer functioning and all the bind/getBean 
     * methods will throw exception.
     */
    public void shutdown();
    
    /**
     * Install a module to the ResourceManager.
     * @param module module to be installed.
     */
    public void install(Module module);
}
