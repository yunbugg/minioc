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

/**
 * Allows application to publish event to interested handlers.
 * @author Yun Liu
 */
public interface EventBus {

	/**
	 * Register a handler to handle a certain event type.
	 * 
	 * @param eventType event type to register.
	 * @param handler handler handler that handles the event type.
	 */
    public <T extends Event> void registerHandler(Class<T> eventType, EventHandler<T> handler);

    /**
     * Fire a event.
     * @param event event to be fired.
     */
    public <T extends Event> void fire(T event);

    /**
     * Scan the source object for any {@link Handles} event annotation and register the method 
     * as an event handler.
     * @param source source object
     */
    public void scanEventHandlers(Object source);
}
