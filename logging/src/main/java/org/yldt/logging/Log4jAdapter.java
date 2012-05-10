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

/**
 * Log4j Logger adapter
 * @author Yun Liu
 *
 */
class Log4jAdapter implements Logger{
	private org.apache.log4j.Logger logger;
	
	public Log4jAdapter(String loggerName)
	{
		logger = org.apache.log4j.Logger.getLogger(loggerName);
	}
	
	public void debug(Object message) {
		logger.debug(message);	
	}
	
	public void debug(Object message, Throwable error) {
		logger.debug(message, error);
	}
	
	public boolean isDebugEnabled() {
		return logger.isDebugEnabled();
	}
	
	public void info(Object message) {
		logger.info(message);
	}
	
	public void info(Object message, Throwable error) {
		logger.info(message,error);
	}
	
	public boolean isInfoEnabled() {
		return logger.isInfoEnabled();
	}
	
	public void error(Object message) {
		logger.error(message);
	}
	
	public void error(Object message, Throwable error) {
		logger.error(message, error);
	}
}
