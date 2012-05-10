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
package org.yldt.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * The class <code>ApplicationEventBusTest</code> contains tests for the class
 * <code>{@link ApplicationEventBus}</code>.
 * 
 * @author Yun Liu
 */
public class ApplicationEventBusTest {

	/**
	 * Run the void fire(T) method test.
	 * 
	 * @throws Exception
	 * 
	 */
	@Test
	public void testNullEvent() throws Exception {
		ApplicationEventBus fixture = new ApplicationEventBus();

		try {
			fixture.fire(null);
			fail("Expect IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// pass
		}
	}

	/**
	 * Run the void fire(T) method test.
	 * 
	 * @throws Exception
	 * 
	 */
	@Test
	public void testSimpleFire() throws Exception {
		ApplicationEventBus fixture = new ApplicationEventBus();
		InvocationAwareEventHandler<TestEvent> handler = new InvocationAwareEventHandler<TestEvent>();
		fixture.registerHandler(TestEvent.class, handler);
		fixture.fire(new TestEvent());

		assertEquals(1, handler.getCount());
	}

	/**
	 * Run the void fire(T) method test.
	 * 
	 * @throws Exception
	 * 
	 */
	@Test
	public void testEventInheritance() throws Exception {
		ApplicationEventBus fixture = new ApplicationEventBus();
		InvocationAwareEventHandler<Event> handlesAllEvents = new InvocationAwareEventHandler<Event>();
		InvocationAwareEventHandler<TestEvent> handler = new InvocationAwareEventHandler<TestEvent>();
		fixture.registerHandler(TestEvent.class, handler);
		fixture.registerHandler(Event.class, handlesAllEvents);
		fixture.fire(new Event() {
		});

		assertEquals(0, handler.getCount());
		assertEquals(1, handlesAllEvents.getCount());
	}

	private static class TestEvent implements Event {
	};

	/**
	 * Run the void scanEventHandlers(Object) method test.
	 * 
	 * @throws Exception
	 * 
	 */
	@Test
	public void testScanEventHandlers() throws Exception {
		ApplicationEventBus fixture = new ApplicationEventBus();
		TestEventHandlerSource source = new TestEventHandlerSource();

		fixture.scanEventHandlers(source);
		
		fixture.fire(new Event() {});

		assertEquals(0, source.getTestEventCount());
		assertEquals(1, source.getEventCount());
		
		
		
		
		fixture.fire(new TestEvent());

		assertEquals(1, source.getTestEventCount());
		assertEquals(2, source.getEventCount());
		
		fixture.fire(new TestEvent(){});

		assertEquals(2, source.getTestEventCount());
		assertEquals(3, source.getEventCount());
		
	}

	/**
	 * Run the void scanEventHandlers(Object) method test where the object has
	 * no event handling annotation.
	 * 
	 * @throws Exception
	 * 
	 */
	@Test
	public void testNoEventHandlerScan() throws Exception {
		ApplicationEventBus fixture = new ApplicationEventBus();
		Object source = new Object();

		fixture.scanEventHandlers(source);

	}
	
	@Test
	public void testErrorOnEventHandler() throws Exception {
		ApplicationEventBus fixture = new ApplicationEventBus();
		ErrorTestEventHandlerSource source = new ErrorTestEventHandlerSource();

		try
		{
			fixture.scanEventHandlers(source);
			fail("Expect EventBussException");
		}
		catch(EventBusException e){
		}
	}
	
	@Test
	public void testOtherVariantOfEventHandlerScan() throws Exception {
		ApplicationEventBus fixture = new ApplicationEventBus();
		OtherEventHandlerSource source = new OtherEventHandlerSource();

		fixture.scanEventHandlers(source);
		
		fixture.fire(new TestEvent());

		assertEquals(1, source.getNoArgCount());
		assertEquals(1, source.getEventCount());

	}

	static class TestEventHandlerSource {
		private int eventCount;
		private int testEventCount;

		public int getTestEventCount() {
			return testEventCount;
		}

		public int getEventCount() {
			return eventCount;
		}

		@Handles(Event.class)
		public void onRegularEvent(Event event) {
			++eventCount;
		}

		@Handles(TestEvent.class)
		public void onTestEvent(TestEvent event) {
			++testEventCount;
		}
	}
	
	static class OtherEventHandlerSource
	{
		private int eventCount;
		private int noArgCount;
		
		@Handles(TestEvent.class)
		public void onOtherTestEvent(Event event) {
			eventCount++;
		}
		
		@Handles(TestEvent.class)
		public void onOtherTestEventNoArg() {
			noArgCount++;
		}

		public int getEventCount() {
			return eventCount;
		}

		public int getNoArgCount() {
			return noArgCount;
		}
	}
	
	
	static class ErrorTestEventHandlerSource {
		private int eventCount;
		private int testEventCount;

		public int getTestEventCount() {
			return testEventCount;
		}

		public int getEventCount() {
			return eventCount;
		}

		@Handles(Event.class)
		public void onRegularEvent(TestEvent event) {
			++eventCount;
		}
	}
}