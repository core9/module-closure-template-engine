package io.core9.plugin.template;

import java.util.Map;

public interface TemplateEngine<T> {
	
	/**
	 * Renders the page
	 */
	T render(String template, Map<String, Object> context);

}