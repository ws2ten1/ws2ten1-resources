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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.junit.Test;
import org.ws2ten1.chunks.Chunk;
import org.ws2ten1.chunks.ChunkImpl;
import org.ws2ten1.chunks.ChunkRequest;
import org.ws2ten1.chunks.PaginationTokenEncoder;
import org.ws2ten1.chunks.SimplePaginationTokenEncoder;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Test for {@link ChunkedResources}.
 */
@Slf4j
public class ChunkedResourcesTest {
	
	private static final ObjectMapper OM = new ObjectMapper()
		.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
	
	private static final PaginationTokenEncoder ENCODER = new SimplePaginationTokenEncoder();
	
	
	@Test
	public void testSerialize_String() throws Exception {
		// setup
		String paginationToken = ENCODER.encode("aaa", "ccc");
		List<String> content = Arrays.asList("aaa", "bbb", "ccc");
		Chunk<String> chunk = new ChunkImpl<>(content, paginationToken, new ChunkRequest(10));
		ChunkedResources<String> stringsChunkResource = new ChunkedResources<>("resources", chunk);
		// exercise
		String actual = OM.writeValueAsString(stringsChunkResource);
		// verify
		log.info(actual);
		with(actual)
			.assertThat("$._embedded.resources[0]", is("aaa"))
			.assertThat("$._embedded.resources[1]", is("bbb"))
			.assertThat("$._embedded.resources[2]", is("ccc"))
			.assertThat("$.chunk.size", is(chunk.size()))
			.assertThat("$.chunk.pagination_token", is(chunk.getPaginationToken()));
	}
	
	@Test
	public void testSerialize_Bean() throws Exception {
		// setup
		String paginationToken = ENCODER.encode("aaa", "ccc");
		List<SampleBean> content = Arrays.asList(new SampleBean("aaa", "bbb"), new SampleBean("ccc", "ddd"));
		Chunk<SampleBean> chunk = new ChunkImpl<>(content, paginationToken, new ChunkRequest(10));
		ChunkedResources<SampleBean> stringsChunkResource = new ChunkedResources<>("beans", chunk);
		// exercise
		String actual = OM.writeValueAsString(stringsChunkResource);
		// verify
		log.info(actual);
		with(actual)
			.assertThat("$._embedded.beans[0].foo", is("aaa"))
			.assertThat("$._embedded.beans[0].bar", is("bbb"))
			.assertThat("$._embedded.beans[1].foo", is("ccc"))
			.assertThat("$._embedded.beans[1].bar", is("ddd"))
			.assertThat("$.chunk.size", is(chunk.size()))
			.assertThat("$.chunk.pagination_token", is(chunk.getPaginationToken()));
	}
	
	@Test
	public void testSerialize_WithoutToken() throws Exception {
		// setup
		List<String> content = Arrays.asList("aaa", "bbb", "ccc");
		Chunk<String> chunk = new ChunkImpl<>(content, null, new ChunkRequest(10));
		ChunkedResources<String> stringsChunkResource = new ChunkedResources<>("resources", chunk);
		// exercise
		String actual = OM.writeValueAsString(stringsChunkResource);
		// verify
		log.info(actual);
		with(actual)
			.assertThat("$._embedded.resources[0]", is("aaa"))
			.assertThat("$._embedded.resources[1]", is("bbb"))
			.assertThat("$._embedded.resources[2]", is("ccc"))
			.assertThat("$.chunk.size", is(chunk.size()))
			.assertNotDefined("$.chunk.pagination_token");
	}
	
	@Test
	public void testDeserialize_Bean() throws Exception {
		// setup
		String paginationToken = ENCODER.encode("aaa", "ccc");
		List<SampleBean> content = Arrays.asList(new SampleBean("aaa", "bbb"), new SampleBean("ccc", "ddd"));
		Chunk<SampleBean> chunk = new ChunkImpl<>(content, paginationToken, new ChunkRequest(10));
		ChunkedResources<Resource<SampleBean>> expected = new ChunkedResources<>("beans", chunk, Resource::new);
		String json = "{\n"
				+ "  'chunk': {\n"
				+ "    'size': 2,\n"
				+ "    'pagination_token': '" + paginationToken + "'\n"
				+ "  },\n"
				+ "  '_embedded': {\n"
				+ "    'beans': [\n"
				+ "      { 'foo': 'aaa', 'bar': 'bbb' },\n"
				+ "      { 'foo': 'ccc', 'bar': 'ddd' }\n"
				+ "    ]\n"
				+ "  }\n"
				+ "}";
		// exercise
		ChunkedResources<? extends Resource<SampleBean>> actual = OM.readValue(json,
				new TypeReference<ChunkedResources<? extends Resource<SampleBean>>>() {
				});
		// verify
		log.info("{}", actual);
		assertThat(actual.getValue(), is(expected.getValue()));
		assertThat(actual.getEmbeddedResources(), is(expected.getEmbeddedResources()));
		assertThat(actual.getEmbeddedResources().get("beans"), is(expected.getEmbeddedResources().get("beans")));
		
		Collection<? extends Resource<SampleBean>> resources =
				(Collection<? extends Resource<SampleBean>>) actual.getEmbeddedResources().get("beans");
		Iterator<? extends Resource<SampleBean>> itr = resources.iterator();
		Resource<SampleBean> res1 = itr.next();
		assertThat(res1.getValue().getFoo(), is("aaa"));
		assertThat(res1.getValue().getBar(), is("bbb"));
		Resource<SampleBean> res2 = itr.next();
		assertThat(res2.getValue().getFoo(), is("ccc"));
		assertThat(res2.getValue().getBar(), is("ddd"));
	}
	
	
	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	@SuppressWarnings("javadoc")
	public static class SampleBean {
		
		private String foo;
		
		private String bar;
	}
}
