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

import static com.jayway.jsonassert.JsonAssert.with;
import static org.hamcrest.Matchers.is;

import java.util.Arrays;

import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Test for {@link PagedResources} serialization.
 */
@Slf4j
public class PagedResourcesTest {
	
	private static final ObjectMapper OM = new ObjectMapper();
	
	
	@Test
	public void testSerialize_Strings() throws Exception {
		// setup
		Page<String> page = new PageImpl<>(Arrays.asList("foo", "bar", "baz"));
		PagedResources<String> stringsPageResource = new PagedResources<>("strings", page);
		// exercise
		String actual = OM.writeValueAsString(stringsPageResource);
		// verify
		log.info(actual);
		with(actual)
			.assertThat("$._embedded.strings[0]", is("foo"))
			.assertThat("$._embedded.strings[1]", is("bar"))
			.assertThat("$._embedded.strings[2]", is("baz"))
			.assertThat("$.page.size", is(page.getSize()))
			.assertThat("$.page.total_pages", is(page.getTotalPages()))
			.assertThat("$.page.number", is(page.getNumber()))
			.assertThat("$.page.total_elements", is((int) page.getTotalElements()));
	}
}
