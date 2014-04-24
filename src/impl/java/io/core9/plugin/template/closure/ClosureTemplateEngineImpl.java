package io.core9.plugin.template.closure;

import io.core9.plugin.server.VirtualHost;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import net.xeoh.plugins.base.annotations.PluginImplementation;

import com.google.common.io.Resources;
import com.google.template.soy.SoyFileSet;
import com.google.template.soy.base.SoySyntaxException;
import com.google.template.soy.msgs.SoyMsgBundle;
import com.google.template.soy.msgs.SoyMsgBundleHandler;
import com.google.template.soy.msgs.SoyMsgException;
import com.google.template.soy.tofu.SoyTofu;
import com.google.template.soy.tofu.SoyTofu.Renderer;
import com.google.template.soy.xliffmsgplugin.XliffMsgPlugin;

@PluginImplementation
public class ClosureTemplateEngineImpl implements ClosureTemplateEngine {
	// TODO, just look at it...
	private final Map<String, Renderer> CACHE = new HashMap<String, Renderer>();
	private final Map<VirtualHost, Map<String,String>> VHOST_TEMPLATES = new HashMap<VirtualHost, Map<String,String>>();
	private final Map<VirtualHost, Map<String,Renderer>> VHOST_CACHE = new HashMap<VirtualHost, Map<String,Renderer>>();
	private final Map<VirtualHost, SoyTofu> VHOST_TOFUS = new HashMap<VirtualHost, SoyTofu>();
	private final Map<String, String> TEMPLATE_STRING_COLLECTION = new HashMap<String, String>();
	@Deprecated
	private SoyTofu tofu;
	
	

	@Override
	@Deprecated
	public String render(String templatename, Map<String, Object> context) {
		context.remove("_id");
		return compile(templatename, context);
	};
	
	@Override
	public String render(VirtualHost vhost, String template, Map<String, Object> context) {
		context.remove("_id");
		return compile(vhost, template, context);
	}

	/**
	 * Gets a rendered template Adds the renderer to the cache if not already
	 * available.
	 * 
	 * @param template
	 * @return the renderer
	 */
	@Deprecated
	public String compile(String template, Map<String, Object> context) {
		String result = "";

		Renderer renderer = CACHE.get(template);
		//FIXME need a good way to deal with debug mode and caching on and of
		if (renderer != null) {
			result = renderer.setData(context).render();
		} else {
			result = tofu.newRenderer(template).setMsgBundle(getStandardMsgBundle()).setData(context).render();
			CACHE.put(template, renderer);
		}
		return result;
	}
	
	
	public String compile(VirtualHost vhost, String template, Map<String, Object> context) {
		String result = "";
		
		Renderer renderer = null;
		Map<String,Renderer> vhostCache = VHOST_CACHE.get(vhost);
		
		// Get renderer if in cache, create new cache otherwise
		if(vhostCache != null) {
			renderer = vhostCache.get(template);
		} else {
			vhostCache = new HashMap<String, SoyTofu.Renderer>();
			VHOST_CACHE.put(vhost, vhostCache);
		}
		
		// Check if renderer is available, else put a new one in cache
		if(renderer != null) {
			result = renderer.setData(context).render();
		} else {
			SoyTofu tofu = VHOST_TOFUS.get(vhost);
			// TODO Quickfix
			if(tofu == null) {
				tofu = this.tofu;
			}
			renderer = tofu.newRenderer(template).setMsgBundle(getStandardMsgBundle());
			vhostCache.put(template, renderer);
			result = renderer.setData(context).render();
		}
		return result;
	}

	@Override
	@Deprecated
	public void createCache() throws SoySyntaxException {
		SoyFileSet.Builder buildCollection = new SoyFileSet.Builder();

		for (Map.Entry<String,String> template : TEMPLATE_STRING_COLLECTION.entrySet()) {
			if(template.getValue() != null && validateTemplate(template)){
				
				System.out.println("Adding to Soy templates build collection : " + template.getKey());
				buildCollection.add(template.getValue(), template.getKey());
			}
		}
		//FIXME quickfix to keep app from crashing
		SoyTofu orgTofo = tofu;
		try {
			tofu = buildCollection.build().compileToTofu();
			this.CACHE.clear();
		} catch (SoySyntaxException e) {
			tofu = orgTofo;
			e.printStackTrace();
			throw e;
		}
	}
	

	@Override
	public void createCache(VirtualHost vhost) throws SoySyntaxException {
		SoyFileSet.Builder buildCollection = new SoyFileSet.Builder();
		for(Map.Entry<String, String> template : TEMPLATE_STRING_COLLECTION.entrySet()) {
			System.out.println("Adding to Soy templates build collection : " + template.getKey());
			buildCollection.add(template.getValue(), template.getKey());
		}
		for(Map.Entry<String, String> template : VHOST_TEMPLATES.get(vhost).entrySet()) {
			System.out.println("Adding to Soy templates build collection : " + template.getKey());
			buildCollection.add(template.getValue(), template.getKey());
		}
		SoyTofu orgTofo = VHOST_TOFUS.get(vhost);
		try {
			this.VHOST_TOFUS.put(vhost, buildCollection.build().compileToTofu());
			if(this.VHOST_CACHE.get(vhost) != null) {
				this.VHOST_CACHE.get(vhost).clear();
			}
		} catch (SoySyntaxException e) {
			this.VHOST_TOFUS.put(vhost, orgTofo);
			e.printStackTrace();
			throw e;
		}
	}

	private boolean validateTemplate(Map.Entry<String, String> template) {
		String keys = template.getKey();
		String[] space = keys.split("\\.");
		for (String key : space) {
			if(key.equals("null")){
				System.out.println("Not adding : " + keys);
				return false;
			}
		}
		return true;
	}

	@Override
	public void addString(String identifier, String template) {
		TEMPLATE_STRING_COLLECTION.put(identifier, template);
	}
	
	@Override
	public void addString(VirtualHost vhost, String identifier, String template) {
		Map<String,String> templates = VHOST_TEMPLATES.get(vhost);
		if(templates == null) {
			templates = new HashMap<String,String>();
			VHOST_TEMPLATES.put(vhost, templates);
		}
		templates.put(identifier, template);
	}

	private SoyMsgBundle getStandardMsgBundle() {
		SoyMsgBundleHandler msgBundleHandler = new SoyMsgBundleHandler(
				new XliffMsgPlugin());
		URL xliffResource = Resources.getResource("nl-nl.xlf");
		SoyMsgBundle msgBundle = null;
		try {
			msgBundle = msgBundleHandler.createFromResource(xliffResource);
		} catch (SoyMsgException | IOException e) {
			e.printStackTrace();
		}
		if (msgBundle.getLocaleString() == null) {
			System.out.println("Error reading message resource \"nl-nl.xlf\".");
		}
		return msgBundle;
	}
}
