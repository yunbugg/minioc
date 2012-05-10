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

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.yldt.ioc.annotations.Bean;
import org.yldt.ioc.annotations.Inject;
import org.yldt.logging.LogManager;
import org.yldt.logging.Logger;

/**
 * Implementation of the ResourceManager interface.
 * 
 * @author Yun Liu
 */
public class ApplicationBeanManager implements ResourceManager {
	private static final Logger logger = LogManager
			.getLogger(ApplicationBeanManager.class);
	private final ConcurrentMap<String, Resource> resources;
	// used to maintain resources that required shutdown as well as the order of
	// shutdown
	private State state;
	private final List<Resource> startedResources;
	private final Object lock = new Object();

	public ApplicationBeanManager() {
		resources = new ConcurrentHashMap<String, Resource>();
		startedResources = new LinkedList<Resource>();
		state = State.Running;
		bindObject(this, ResourceManager.class);
	}

	public void bind(final Class<?> componentType) {
		Bean bean = componentType.getAnnotation(Bean.class);
		if (bean != null && bean.name() != null && bean.name().length > 0)
			bind(componentType, bean.name());
		else
			bind(componentType, componentType);
	}

	public void bind(final Class<?> componentType,
			final Class<?> interfaceType, final Class<?>... additionalTypes) {

		assertValidTypes(componentType, interfaceType, additionalTypes);
		final String[] additionalNames = toNames(additionalTypes);

		bind(componentType, interfaceType.getName(), additionalNames);
	}

	private void assertValidTypes(Class<?> componentType,
			Class<?> interfaceType, Class<?>[] additionalTypes) {
		if (!interfaceType.isAssignableFrom(componentType))
			throw new IllegalArgumentException("Componenet type ["
					+ componentType + "] does not implement or extend ["
					+ interfaceType + "]");

		if (additionalTypes != null)
			for (Class<?> typeToCheck : additionalTypes)
				if (!typeToCheck.isAssignableFrom(componentType))
					throw new IllegalArgumentException("Componenet type ["
							+ componentType
							+ "] does not implement or extend [" + typeToCheck
							+ "]");
	}

	public void bind(final Class<?> componentType, final String name,
			final String... additionalNames) {
		final String[] names = join(name, additionalNames);

		bind(componentType, names);
	}

	private void bind(final Class<?> componentType, final String[] names) {
		final Resource bean = new Resource(componentType, names);
		register(bean);
	}

	public void bindObject(final Object component) {
		if (component == null)
			throw new ResourceException("Cannot bind null componenent");
		bindObject(component, component.getClass().getName());
	}

	public void bindObject(final Object component, final String name,
			final String... additionalNames) {
		if (component == null)
			throw new ResourceException("Cannot bind null componenent");
		final String[] names = join(name, additionalNames);

		final Resource resource = new Resource(component.getClass(), names);
		resource.setObject(component);

		register(resource);
	}

	public void bindObject(final Object component,
			final Class<?> interfaceType, final Class<?>... additionalTypes) {
		assertValidTypes(component.getClass(), interfaceType, additionalTypes);
		final String[] names = toNames(additionalTypes);
		bindObject(component, interfaceType.getName(), names);
	}

	private String[] join(final String name, final String... additionalNames) {
		final String[] names = new String[1 + additionalNames.length];
		names[0] = name;
		System.arraycopy(additionalNames, 0, names, 1, additionalNames.length);
		return names;
	}

	private String[] toNames(final Class<?>... additionalTypes) {
		final String[] additionalNames = new String[additionalTypes.length];
		for (int i = 0; i < additionalTypes.length; ++i)
			additionalNames[i] = additionalTypes[i].getName();
		return additionalNames;
	}

	private void register(final Resource resource) {
		synchronized (lock) {
			if (state != State.Running)
				throw new ResourceException(
						"Binding is not allowed when the ResourceManager is in ["
								+ this.state + "] state");

			for (final String name : resource.getNames()) {
				final Object putted = resources.putIfAbsent(name, resource);
				if (putted != null && putted != resource) {
					throw new ResourceException("Component type ["
							+ resource.getType() + "] already exists.");
				}
			}
		}
	}

	public <T> T getBean(final Class<T> componentType) {
		return getBean(componentType.getName(), componentType);
	}

	public <T> T getBean(final String name, final Class<T> componentType) {
		if (logger.isDebugEnabled())
			logger.debug("Obtaining bean with name [" + name + "] and type ["
					+ componentType + "]");
		final T bean = getBeanByName(name, componentType);
		if (logger.isDebugEnabled())
			logger.debug("Returning bean [" + bean.getClass() + "]");
		return bean;
	}

	private <T> T getBeanByName(final String name, final Class<T> componentType) {
		synchronized (lock) {
			if (state == State.Shutdown)
				throw new ResourceException(
						"Obtaining bean is not allowed when the ResourceManager is in ["
								+ this.state + "] state");

			final Resource resource = resources.get(name);
			if (resource == null) {
				throw new ResourceException(
						"Component with name ["
								+ name
								+ "] is not registerd. If an resource should be used for multiple name,"
								+ " make sure the resource is binded to the desrire names");
			}

			if (resource.getObject() != null) {
				if (!componentType.isAssignableFrom(resource.getObject()
						.getClass()))
					throw new ResourceException("Bean [" + name
							+ " is of type [" + resource.getObject().getClass()
							+ "] not an instance of [" + componentType + "]");
				@SuppressWarnings("unchecked")
				final T typped = (T) resource.getObject();
				startResouceIfNeeded(resource);
				return typped;
			}

			return this.contructObject(name, resource, componentType);
		}
	}

	private <T> T contructObject(final String name, final Resource resource,
			final Class<T> componentType) {

		if (!componentType.isAssignableFrom(resource.getType()))
			throw new ResourceException("Bean [" + name + "] is of type ["
					+ resource.getType() + "] not an instance of ["
					+ componentType + "]");

		if (resource.isUnderConstruction()) {
			throw new ResourceException(
					"Component with name ["
							+ name
							+ "] is currently under construction."
							+ " This indicates a cycular dependency is not supported in this"
							+ " resource manager implementation");
		}

		if (logger.isDebugEnabled())
			logger.debug("Creating bean with name [" + name + "] and type ["
					+ resource.getType() + "]");

		resource.setUnderConstruction(true);

		@SuppressWarnings("unchecked")
		final Constructor<T> constructor = (Constructor<T>) getConstructorFor(resource);
		final T object = instantiateBean(name, constructor);
		performFieldInjection(object, object.getClass());
		performMethodInjection(object, object.getClass());
		resource.setObject(object);
		resource.setUnderConstruction(false);

		startResouceIfNeeded(resource);
		return object;
	}

	private void performFieldInjection(Object object, Class<?> targetType) {
		if (targetType == Object.class)
			return;

		for (Field field : targetType.getDeclaredFields()) {
			Inject inject = field.getAnnotation(Inject.class);
			if (inject != null) {
				Object bean = getBeanForInject(object.getClass(), inject,
						field.getType());
				setToField(field, object, bean);
			}
		}

		performFieldInjection(object, targetType.getSuperclass());
	}

	private void setToField(Field field, Object object, Object bean) {
		if (!field.isAccessible())
			field.setAccessible(true);
		try {
			field.set(object, bean);
		} catch (Exception e) {
			throw new ResourceException("Failed to set bean to field ["
					+ field.getName() + "] for [" + object.getClass() + "]", e);
		}
	}

	private void performMethodInjection(Object object, Class<?> targetType) {
		if (targetType == Object.class)
			return;

		for (Method method : targetType.getDeclaredMethods()) {
			Inject inject = method.getAnnotation(Inject.class);
			if (inject != null) {
				final Class<?> declaringClass = method.getDeclaringClass();
				final Annotation[][] annotations = method
						.getParameterAnnotations();
				final Class<?>[] argumentTypes = method.getParameterTypes();

				final Object[] arguments = getBeansForInject(declaringClass,
						annotations, argumentTypes);

				applyMethod(method, object, arguments);
			}
		}

		performMethodInjection(object, targetType.getSuperclass());
	}

	private void applyMethod(Method method, Object object, Object[] arguments) {
		if (!method.isAccessible())
			method.setAccessible(true);
		try {
			method.invoke(object, arguments);
		} catch (Exception e) {
			throw new ResourceException("Failed to inject bean to method ["
					+ method.getName() + "] for [" + object.getClass() + "]", e);
		}
	}

	private void startResouceIfNeeded(final Resource resource) {
		if (resource.isStarted())
			return;

		final Object bean = resource.getObject();
		if (bean instanceof LifeCycle) {
			if (logger.isDebugEnabled()) {
				logger.debug("initializing resource ["
						+ bean.getClass().getName() + "]");
			}
			((LifeCycle) bean).onStart();
			startedResources.add(resource);
		}

		resource.markStarted();
	}

	private <T> Object[] getBeansForInject(Class<T> declaringClass,
			final Annotation[][] annotations, final Class<?>[] argumentTypes) {
		final Object[] arguments = new Object[argumentTypes.length];
		for (int i = 0; i < argumentTypes.length; ++i) {
			final Annotation[] argumentAnn = annotations[i];
			final Inject inject = extractAnnotation(argumentAnn, Inject.class);
			final Class<?> argumentType = argumentTypes[i];
			arguments[i] = getBeanForInject(declaringClass, inject,
					argumentType);
		}
		return arguments;
	}

	private Object getBeanForInject(final Class<?> declaringClass,
			final Inject inject, final Class<?> beanClass) {
		try {
			Object bean;
			if (inject == null || "".equals(inject.value())) {
				if (logger.isDebugEnabled())
					logger.debug("Required dependency of type [" + beanClass
							+ "]");
				bean = getBean(beanClass);
			} else {
				if (logger.isDebugEnabled())
					logger.debug("Required dependency of name ["
							+ inject.value() + "] and type [" + beanClass + "]");
				bean = getBean(inject.value(), beanClass);
			}
			return bean;
		} catch (final ResourceException e) {
			throw new UnsatisfiedDependencyException(
					"Unable to statisfy depdenency for class ["
							+ declaringClass.getName()
							+ "]. Failed to obtain arugment of type ["
							+ beanClass.getName() + "]", e);
		}
	}

	public List<Object> listBeans() {
		// can be improved
		final IdentityHashMap<Object, String> beans = new IdentityHashMap<Object, String>(
				resources.size());
		for (final String name : this.resources.keySet()) {
			final Object bean = this.getBean(name, Object.class);
			beans.put(bean, "");
		}
		return new ArrayList<Object>(beans.keySet());
	}

	public void startAll() {
		this.listBeans();
	}

	public void shutdown() {
		logger.debug("ResourceManager is shutting down.");
		synchronized (lock) {
			for (final Resource resource : this.startedResources) {
				final Object bean = resource.getObject();

				if (bean instanceof LifeCycle) {
					if (logger.isDebugEnabled()) {
						logger.debug("Shutting down resource ["
								+ bean.getClass().getName() + "]");
					}
					((LifeCycle) bean).onShutdown();
				}
			}
			this.state = State.Shutdown;
		}
	}

	public State getState() {
		return this.state;
	}

	@SuppressWarnings("unchecked")
	private <T extends Annotation> T extractAnnotation(final Annotation[] anns,
			final Class<T> targetAnnotation) {
		for (final Annotation ann : anns)
			if (targetAnnotation.isAssignableFrom(ann.getClass()))
				return (T) ann;
		return null;
	}

	private <T> T instantiateBean(final String beanName,
			final Constructor<T> constructor) {
		final Class<T> declaringClass = constructor.getDeclaringClass();
		final Annotation[][] annotations = constructor
				.getParameterAnnotations();
		final Class<?>[] argumentTypes = constructor.getParameterTypes();

		final Object[] arguments = getBeansForInject(declaringClass,
				annotations, argumentTypes);

		try {
			return constructor.newInstance(arguments);
		} catch (final IllegalArgumentException e) {
			final String errorMessage = detailedIllegalArgumentMessage(
					beanName, constructor, arguments);

			throw new ResourceException(errorMessage, e);
		} catch (final InstantiationException e) {
			throw new ResourceException("Error instantiating bean [" + beanName
					+ "]", e);
		} catch (final IllegalAccessException e) {
			throw new ResourceException(
					"Illegal access when intantiating bean [" + beanName + "]",
					e);
		} catch (final InvocationTargetException e) {
			throw new ResourceException("Error intantiating bean [" + beanName
					+ "]. Constructor throws exception.", e.getCause());
		}
	}

	private <T> String detailedIllegalArgumentMessage(final String beanName,
			final Constructor<T> constructor, final Object[] arguments) {
		final Class<?>[] declaredTypes = constructor.getParameterTypes();
		final StringBuilder errorMessageBuilder = new StringBuilder();
		errorMessageBuilder.append("Illegal argument when constructing bean [")
				.append(beanName).append("]. ");
		if (declaredTypes.length != arguments.length) {
			errorMessageBuilder.append("Argument length mismatch. Expect [")
					.append(declaredTypes.length).append("] but got [")
					.append(arguments.length).append("].");
		} else {
			for (int i = 0; i < declaredTypes.length; ++i) {
				if (!declaredTypes[i].isAssignableFrom(arguments[i].getClass())) {
					errorMessageBuilder
							.append("Type mismatch at index [")
							.append(i)
							.append("]. Actual type [")
							.append(arguments[i].getClass())
							.append("] does not implement or extend the declared type [")
							.append(declaredTypes[i]).append("]. ");
				}
			}
		}
		return errorMessageBuilder.toString();
	}

	private Constructor<?> getConstructorFor(final Resource bean) {
		final Constructor<?>[] constructors = bean.getType().getConstructors();
		if (constructors.length != 1) {
			throw new ResourceException(
					"When binding with type, component is required to have at least one public constructor."
							+ "If component does not meet the criteria, using bindObject() instead.");
		}
		return constructors[0];
	}
	
	public void install(Module module) {
		module.configure(this);
	}
}
