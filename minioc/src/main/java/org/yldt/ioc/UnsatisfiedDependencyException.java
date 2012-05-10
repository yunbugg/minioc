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

/**
 * Thrown when a dependency cannot be satisfied.
 * @author Yun Liu
 *
 */
public class UnsatisfiedDependencyException extends ResourceException {

	private static final long serialVersionUID = -6406006590283206087L;

	public UnsatisfiedDependencyException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnsatisfiedDependencyException(String message) {
		super(message);
	}

	public UnsatisfiedDependencyException(Throwable cause) {
		super(cause);
	}
}
