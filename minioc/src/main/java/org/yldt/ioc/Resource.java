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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Internal representation of a managed resource. This class is not thread safe
 * and requires synchronization.
 * @author Yun Liu
 */
final class Resource {
	private final Class<?> type;
	private final List<String> names;
	private Object object;
	private boolean underConstruction;
	private boolean started;

    public Resource(Class<?> type, String... names) {
		this.type = type;
		this.names = Collections.unmodifiableList(Arrays.asList(names));
		this.underConstruction = false;
		this.started = false;
	}

    public Class<?> getType() {
		return type;
	}

	public List<String> getNames() {
		return names;
	}

	public void setObject(Object object) {
		this.object = object;
	}

	public Object getObject() {
		return object;
	}
	
	public void markStarted() {
        this.started = true;
    }
	
	public void setUnderConstruction(boolean underConstruction) {
	    this.underConstruction = underConstruction;
	}

	public boolean isUnderConstruction() {
		return underConstruction;
	}

    public boolean isStarted() {
        return started;
    }
}