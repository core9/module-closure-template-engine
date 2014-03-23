package io.core9.plugin.template.closure;

import io.core9.core.plugin.Core9Plugin;
import io.core9.plugin.template.TemplateEngine;

import java.io.File;
import java.io.IOException;

import com.google.template.soy.base.SoySyntaxException;

public interface ClosureTemplateEngine extends TemplateEngine<String>, Core9Plugin {
	
	/**
	 * Creates the real tofu object, holding the compiled templates
	 */
	void createCache() throws SoySyntaxException;

	/**
	 * Adds files or folders to the template builder
	 * @throws IOException 
	 */
	void addFile(File file) throws IOException;
	
	/**
	 * Adds a string to the builder
	 * @param identifier
	 * @param template
	 */
	void addString(String identifier, String template);

}
