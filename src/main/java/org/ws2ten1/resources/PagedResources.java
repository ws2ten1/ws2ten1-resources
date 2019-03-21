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

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import org.springframework.data.domain.Page;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

import org.ws2ten1.resources.PagedResources.PageMetadata;

/**
 * {@link Resource} for {@link Page}s.
 *
 * @param <T>
 */
@ToString
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class PagedResources<T>extends Resource<PageMetadata> {
	
	private Map<String, Collection<T>> embeddedResources;
	
	
	/**
	 * Creates a {@link PagedResources} instance with {@link Page}.
	 *
	 * @param key must not be {@code null}.
	 * @param page The {@link Page}
	 * @param wrapperFunction function coverts {@code U} to {@code T}
	 */
	public <U> PagedResources(String key, Page<U> page, Function<U, T> wrapperFunction) {
		this(key, page.stream().map(wrapperFunction).collect(Collectors.toList()), new PageMetadata(page));
	}
	
	/**
	 * Creates a {@link PagedResources} instance with {@link Page}.
	 *
	 * @param key must not be {@code null}.
	 * @param page The {@link Page}
	 */
	public PagedResources(String key, Page<T> page) {
		this(key, page.getContent(), new PageMetadata(page));
	}
	
	/**
	 * Creates a {@link PagedResources} instance with embeddedResources collection.
	 *
	 * @param key must not be {@code null}.
	 * @param content The contents
	 */
	public PagedResources(String key, Collection<T> content) {
		this(key, content, new PageMetadata(content.size(), 0, content.size()));
	}
	
	/**
	 * Creates a {@link PagedResources} instance with iterable and metadata.
	 *
	 * @param key must not be {@code null}.
	 * @param content must not be {@code null}.
	 * @param metadata must not be {@code null}.
	 */
	public PagedResources(String key, Collection<T> content, PageMetadata metadata) {
		super(metadata);
		if (key == null) {
			throw new IllegalArgumentException("The key must not be null");
		}
		if (content == null) {
			throw new IllegalArgumentException("The embeddedResources must not be null");
		}
		if (metadata == null) {
			throw new IllegalArgumentException("The metadata must not be null");
		}
		this.embeddedResources = Collections.singletonMap(key, content);
	}
	
	@Override
	@JsonProperty("page")
	@JsonUnwrapped(enabled = false)
	public PageMetadata getValue() {
		return super.getValue();
	}
	
	@Override
	@JsonProperty("_embedded")
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public Map<String, ?> getEmbeddedResources() {
		return embeddedResources;
	}
	
	@Override
	public Resource<PageMetadata> embedResource(String relationship, Object resource) {
		throw new UnsupportedOperationException();
	}
	
	
	/**
	 * Value object for pagination metadata.
	 */
	@ToString
	@EqualsAndHashCode
	@AllArgsConstructor
	@NoArgsConstructor(access = AccessLevel.PACKAGE)
	public static class PageMetadata {
		
		/** the requested size of the page */
		@JsonProperty("size")
		@Getter(onMethod = @__(@JsonIgnore))
		private long size;
		
		/** the total number of elements available */
		@JsonProperty("total_elements")
		@JsonInclude(JsonInclude.Include.NON_NULL)
		@Getter(onMethod = @__(@JsonIgnore))
		private Long totalElements;
		
		/** how many pages are available in total */
		@JsonProperty("total_pages")
		@JsonInclude(JsonInclude.Include.NON_NULL)
		@Getter(onMethod = @__(@JsonIgnore))
		private Long totalPages;
		
		/** the number of the current page */
		@JsonProperty("number")
		@JsonInclude(JsonInclude.Include.NON_NULL)
		@Getter(onMethod = @__(@JsonIgnore))
		private Long number;
		
		
		/**
		 * Creates a new {@link PageMetadata} from the given size, numer and total elements.
		 *
		 * @param size the size of the page
		 * @param number the number of the page
		 * @param totalElements the total number of elements available
		 */
		public PageMetadata(long size, long number, long totalElements) {
			this(size, number, totalElements, size == 0 ? 0 : (long) Math.ceil((double) totalElements / (double) size));
		}
		
		public PageMetadata(Page<?> page) {
			this(
					page.getSize(),
					page.getTotalElements(),
					(long) page.getTotalPages(),
					(long) page.getNumber());
		}
	}
}
