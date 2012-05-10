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
package org.yldt.logging;

import java.util.logging.Level;

/**
 * Java Logger adaptor
 * @author Yun Liu
 *
 */
class JavaLogger implements Logger {
	private java.util.logging.Logger logger;

    public JavaLogger(java.util.logging.Logger logger) {
	   this.logger = logger;
    }

    public void debug(Object message) {
        this.logger.log(Level.FINE, message == null? "null" : message.toString());
	}

	public void debug(Object message, Throwable error) {
	    this.logger.log(Level.FINE, message == null? "null" : message.toString(), error);
	}

	public boolean isDebugEnabled() {
		return this.logger.isLoggable(Level.FINE);
	}

	public void info(Object message) {
	    this.logger.log(Level.INFO, message == null? "null" : message.toString());
	}

	public void info(Object message, Throwable error) {
	    this.logger.log(Level.INFO, message == null? "null" : message.toString(), error);
	}

	public boolean isInfoEnabled() {
	    return this.logger.isLoggable(Level.INFO);
	}

	public void error(Object message) {
	    this.logger.log(Level.SEVERE, message == null? "null" : message.toString());
	}

	public void error(Object message, Throwable error) {
	    this.logger.log(Level.SEVERE, message == null? "null" : message.toString(), error);
	}
}