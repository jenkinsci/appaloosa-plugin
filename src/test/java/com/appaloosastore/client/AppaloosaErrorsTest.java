package com.appaloosastore.client;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.Test;

public class AppaloosaErrorsTest {

	@Test
	public void testCreateFromJson() throws JsonParseException, JsonMappingException, IOException {
		AppaloosaErrors errors = AppaloosaErrors.createFromJson("{\"errors\":[\"error1\"]}");
		assertEquals(1, errors.errors.size());
		assertEquals("error1", errors.errors.get(0));
		assertEquals("error1", errors.toString());
		System.out.println(errors.toString());
	}

}
