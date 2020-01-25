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
package org.polycreo.resources;

import static com.jayway.jsonassert.JsonAssert.with;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Test for {@link Resource} serialization and deserialization.
 */
@Slf4j
public class ResourceTest {
	
	private static final ObjectMapper OM = new ObjectMapper()
		.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
	
	
	@Test
	public void testSerialize_String() throws Exception {
		// setup
		Resource<String> resource = new Resource<>("foo");
		// exercise
		String actual = OM.writeValueAsString(resource);
		// verify
		with(actual)
			.assertThat("$.value", is("foo"))
			.assertNotDefined("$._links")
			.assertNotDefined("$._embedded");
	}
	
	@Test
	public void testSerialize_Integer() throws Exception {
		// setup
		Resource<Integer> resource = new Resource<>(123);
		// exercise
		String actual = OM.writeValueAsString(resource);
		// verify
		with(actual)
			.assertThat("$.value", is(123))
			.assertNotDefined("$._links")
			.assertNotDefined("$._embedded");
	}
	
	@Test
	public void testSerialize_Bean() throws Exception {
		// setup
		Resource<SampleBean> resource = new Resource<>(new SampleBean("aaa", "bbb"));
		// exercise
		String actual = OM.writeValueAsString(resource);
		with(actual)
			.assertThat("$.foo", is("aaa"))
			.assertThat("$.bar", is("bbb"))
			.assertNotDefined("$._links")
			.assertNotDefined("$._embedded");
	}
	
	@Test
	public void testSerialize_WithLink() throws Exception {
		// setup
		Resource<String> resource = new Resource<>("foo")
			.addLink(Link.REL_FIRST, new Link("http://example.com/0000"))
			.addLink(Link.REL_PREVIOUS, new Link("http://example.com/0009"))
			.addLink(Link.REL_SELF, new Link("http://example.com/0010"))
			.addLink(Link.REL_NEXT, new Link("http://example.com/0011"))
			.addLink(Link.REL_LAST, new Link("http://example.com/9999"));
		// exercise
		String actual = OM.writeValueAsString(resource);
		// verify
		log.info(actual);
		with(actual)
			.assertThat("$.value", is("foo"))
			.assertThat("$._links.first.href", is("http://example.com/0000"))
			.assertThat("$._links.prev.href", is("http://example.com/0009"))
			.assertThat("$._links.self.href", is("http://example.com/0010"))
			.assertThat("$._links.next.href", is("http://example.com/0011"))
			.assertThat("$._links.last.href", is("http://example.com/9999"))
			.assertNotDefined("$._embedded");
	}
	
	@Test
	public void testSerialize_WithEmbedded() throws Exception {
		// setup
		Resource<String> resource = new Resource<>("foo");
		resource.embedResource("sample", new SampleBean("aaa", "bbb"));
		// exercise
		String actual = OM.writeValueAsString(resource);
		// verify
		log.info(actual);
		with(actual)
			.assertThat("$.value", is("foo"))
			.assertNotDefined("$._links")
			.assertThat("$._embedded.sample.foo", is("aaa"))
			.assertThat("$._embedded.sample.bar", is("bbb"));
	}
	
	@Test
	public void testDeserialize_Bean() throws Exception {
		// setup
		Resource<SampleBean> expected = new Resource<>(new SampleBean("aaa", "bbb"))
			.embedResource("rel", "embedded-value")
			.addLink("self", new Link("http://example.com/self"));
		String json = "{"
				+ "  'foo': 'aaa',"
				+ "  'bar': 'bbb',"
				+ "  '_embedded': {"
				+ "    'rel': 'embedded-value'"
				+ "  },"
				+ "  '_links': {"
				+ "    'self': { 'href': 'http://example.com/self' }"
				+ "  }"
				+ "}";
		// exercise
		Resource<SampleBean> actual = OM.readValue(json, new TypeReference<Resource<SampleBean>>() {
		});
		// verify
		assertThat(actual, is(expected));
	}
	
	
	@Data
	@SuppressWarnings("javadoc")
	public static class SampleBean {
		
		private String foo;
		
		private String bar;
		
		
		@JsonCreator
		public SampleBean(@JsonProperty("foo") String foo, @JsonProperty("bar") String bar) {
			this.foo = foo;
			this.bar = bar;
		}
	}
}
