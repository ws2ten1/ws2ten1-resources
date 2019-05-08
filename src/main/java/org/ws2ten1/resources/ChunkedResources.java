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

import org.ws2ten1.chunks.Chunk;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

import org.ws2ten1.resources.ChunkedResources.ChunkMetadata;

/**
 * {@link Resource} for {@link Chunk}s.
 *
 * @param <T>
 */
@ToString
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class ChunkedResources<T>extends Resource<ChunkMetadata> {
	
	private Map<String, Collection<T>> embeddedResources;
	
	
	/**
	 * Creates a {@link ChunkedResources} instance with {@link Chunk}.
	 *
	 * @param key must not be {@code null}.
	 * @param chunk The {@link Chunk}
	 * @param wrapperFunction function coverts {@code U} to {@code T}
	 */
	public <U> ChunkedResources(String key, Chunk<U> chunk, Function<U, T> wrapperFunction) {
		this(key, chunk.stream().map(wrapperFunction).collect(Collectors.toList()), new ChunkMetadata(chunk));
	}
	
	/**
	 * Creates a {@link ChunkedResources} instance with {@link Chunk}.
	 *
	 * @param key must not be {@code null}.
	 * @param chunk The {@link Chunk}
	 */
	public ChunkedResources(String key, Chunk<T> chunk) {
		this(key, chunk.getContent(), new ChunkMetadata(chunk));
	}
	
	/**
	 * Creates a {@link ChunkedResources} instance with embeddedResources collection.
	 *
	 * @param key must not be {@code null}.
	 * @param content The contents
	 */
	public ChunkedResources(String key, Collection<T> content) {
		this(key, content, new ChunkMetadata(content.size(), null));
	}
	
	/**
	 * Creates a {@link ChunkedResources} instance with iterable and metadata.
	 *
	 * @param key must not be {@code null}.
	 * @param content must not be {@code null}.
	 * @param metadata must not be {@code null}.
	 */
	public ChunkedResources(String key, Collection<T> content, ChunkMetadata metadata) {
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
		if (content.isEmpty() == false) {
			this.embeddedResources = Collections.singletonMap(key, content);
		}
	}
	
	@Override
	@JsonProperty("chunk")
	@JsonUnwrapped(enabled = false)
	public ChunkMetadata getValue() {
		return super.getValue();
	}
	
	@Override
	@JsonProperty("_embedded")
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public Map<String, ?> getEmbeddedResources() {
		return embeddedResources;
	}
	
	@Override
	public Resource<ChunkMetadata> embedResource(String relationship, Object resource) {
		throw new UnsupportedOperationException();
	}
	
	
	/**
	 * Value object for pagination metadata.
	 */
	@ToString
	@EqualsAndHashCode
	@AllArgsConstructor
	@NoArgsConstructor(access = AccessLevel.PACKAGE)
	public static class ChunkMetadata {
		
		@JsonProperty("size")
		@Getter(onMethod = @__(@JsonIgnore))
		private long size;
		
		@JsonProperty("pagination_token")
		@JsonInclude(JsonInclude.Include.NON_NULL)
		@Getter(onMethod = @__(@JsonIgnore))
		private String paginationToken;
		
		
		public ChunkMetadata(Chunk<?> chunk) {
			this(chunk.getContent().size(), chunk.getPaginationToken());
		}
	}
}
