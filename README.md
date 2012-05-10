minioc - Small and simple IoC container
==================================================

Why another IoC container
--------------------------------------

The minioc project is born as I evolve a simple swing based chess application. The original program is written in a Dependency Injection style without using any container. As the program develop with more and more objects/components, it becomes clear that an IoC container can help to reduce the object creation Factory code--in particular, the object creation order.

What I needed is a small(in size) IoC container that can be used and distributed with the chess program. The nice containers such as spring, guice, or even pico container are too big in size and has a lot more features than needed in the simple program. 

So minioc was created to fit this need. The binaries of the three components(logging, IoC, event bus) together are about 30KB in size.

As of now, the container only supports singleton bean management and has a limitation of bean names being unique.


Example code
--------------------------------------
 	ApplicationBeanManager manager = new ApplicationBeanManager();
	manager.bind(SimpleBean.class);
	manager.bind(AnotherBean.class);
	manager.bindObject(new Date(), "appStartDate");
	manager.startAll();

The container supports constructor injection(bean must have one contructor. In cases where a bean has two constructor, construct the object manually and use bindObject() method to bind the object), setter injection and field injection.

License
--------------------------------------
This work is licensed under apache [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)

