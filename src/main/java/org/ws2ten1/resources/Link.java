/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ws2ten1.resources;

import java.io.Serializable;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Link element in HAL.
 */
@SuppressWarnings("serial")
@JsonIgnoreProperties("templated")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@Data
public class Link implements Serializable { // NOPMD DataClass
	
	public static final String ATOM_NAMESPACE = "http://www.w3.org/2005/Atom";
	
	public static final String REL_SELF = "self";
	
	public static final String REL_FIRST = "first";
	
	public static final String REL_PREVIOUS = "prev";
	
	public static final String REL_NEXT = "next";
	
	public static final String REL_LAST = "last";
	
	/**
	 * the actual URI the link is pointing to.
	 */
	private String href;
	
	private boolean templated;
	
	
	public Link(String href) {
		this(href, false);
	}
}
