package io.core9.plugin.template;

import io.core9.plugin.server.VirtualHost;

import java.util.Map;

public interface TemplateEngine<T> {
	
	/**
	 * Renders the page
	 */
	@Deprecated
	T render(String template, Map<String, Object> context);

	/**
	 * Renders a page
	 * @param vhost
	 * @param template
	 * @param context
	 * @return
	 */
	T render(VirtualHost vhost, String template, Map<String,Object> context);
}