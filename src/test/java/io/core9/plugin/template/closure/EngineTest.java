package io.core9.plugin.template.closure;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import org.junit.Test;

public class EngineTest {
	private ClosureTemplateEngineImpl engine = new ClosureTemplateEngineImpl();
	
	@Test
	public void testStandardTemplateString() {
		String template = "{namespace io.core9}\n\n/**\n *\n */\n{template .string}\nString\n{/template}";
		engine.addString("string.soy", template);
		engine.createCache();
		assertEquals("String", engine.render("io.core9.string", new HashMap<String,Object>())); 
	}

}
