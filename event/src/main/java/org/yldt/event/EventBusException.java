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
 * Exception throw by EventBus
 * @author Yun Liu
 *
 */
public class EventBusException extends RuntimeException{
	private static final long serialVersionUID = 4464389851324325230L;

	public EventBusException(String message, Throwable cause) {
		super(message, cause);
	}

	public EventBusException(String message) {
		super(message);
	}

	public EventBusException(Throwable cause) {
		super(cause);
	}
}
