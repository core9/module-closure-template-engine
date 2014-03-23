package io.core9.plugin.template.closure;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.junit.Test;

public class EngineTest {
	private ClosureTemplateEngineImpl engine = new ClosureTemplateEngineImpl();
	
	@Test
	public void testStandardTemplateFile() throws IOException {
		engine.addFile(new File(EngineTest.class.getResource("/templates/test.soy").getPath()));
		engine.createCache();
		assertEquals("This is a test", engine.render("io.core9.test", new HashMap<String,Object>()));
	}
	
	@Test
	public void testStandardTemplateString() {
		String template = "{namespace io.core9}\n\n/**\n *\n */\n{template .string}\nString\n{/template}";
		engine.addString("string.soy", template);
		engine.createCache();
		assertEquals("String", engine.render("io.core9.string", new HashMap<String,Object>())); 
	}
	
	@Test
	public void testRandomBuilderTemplate() throws IOException {
		engine.addFile(new File(EngineTest.class.getResource("/templates/random.soy").getPath()));
		engine.createCache();
		assertEquals("This is a random template", engine.render("io.core9.random", new HashMap<String,Object>()));
		assertEquals("This is a random template", engine.render("io.core9.random", new HashMap<String,Object>()));
	}
	
	@Test
	public void testTwoBuilders() throws IOException {
		engine.addFile(new File(EngineTest.class.getResource("/templates/builder2.soy").getPath()));
		engine.addFile(new File(EngineTest.class.getResource("/templates/builder1.soy").getPath()));
		engine.createCache();
		assertEquals(engine.render("io.core9.builder1", new HashMap<String,Object>()), "This is a builder1 template");
		assertEquals(engine.render("io.core9.builder2", new HashMap<String,Object>()), "This is a builder2 template");
	}
}
