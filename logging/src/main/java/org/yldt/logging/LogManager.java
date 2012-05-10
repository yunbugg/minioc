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
 * Factory class to create logger adapters.
 * 
 * @author Yun Liu
 * 
 */
public final class LogManager {
	private static final boolean isLog4jPresent = detectLog4j();

	private LogManager() {
	}

	public static Logger getLogger(Class<?> loggingClass) {
		if (isLog4jPresent)
			return new Log4jAdapter(loggingClass.getName());
		return new JavaLogger(java.util.logging.Logger.getLogger(loggingClass.getName()));
	}

	private static boolean detectLog4j() {
		try {
			Class.forName("org.apache.log4j.Logger");
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}
}
