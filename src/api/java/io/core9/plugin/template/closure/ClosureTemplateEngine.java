package io.core9.plugin.template.closure;

import io.core9.core.plugin.Core9Plugin;
import io.core9.plugin.server.VirtualHost;
import io.core9.plugin.template.TemplateEngine;

import com.google.template.soy.base.SoySyntaxException;

public interface ClosureTemplateEngine extends TemplateEngine<String>, Core9Plugin {
	
	/**
	 * Creates the real tofu object, holding the compiled templates
	 */
	@Deprecated
	void createCache() throws SoySyntaxException;
	
	/**
	 * Create the cache for a vhost
	 * @param vhost
	 * @throws SoySyntaxException
	 */
	void createCache(VirtualHost vhost) throws SoySyntaxException;
	
	/**
	 * Adds a string to the builder
	 * @param identifier
	 * @param template
	 */
	void addString(String identifier, String template);

	/**
	 * Adds a string to the builder
	 * @param vhost
	 * @param identifier
	 * @param template
	 */
	void addString(VirtualHost vhost, String identifier, String template);
}
