package io.core9.plugin.template.closure;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
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
	private final Map<String, Renderer> cache = new HashMap<String, Renderer>();
	private SoyTofu tofu;
	private Map<String, File> templateFileCollection = new HashMap<>();
	private Map<String, String> templateStringCollection = new HashMap<>();

	// FIXME blocking, use handler
	public String render(String templatename, Map<String, Object> context) {
		context.remove("_id");
		return compile(templatename, context);
	};

	/**
	 * Gets a rendered template Adds the renderer to the cache if not already
	 * available.
	 * 
	 * @param template
	 * @return the renderer
	 */
	public String compile(String template, Map<String, Object> context) {
		String result = "";

		Renderer renderer = cache.get(template);
		//FIXME need a good way to deal with debug mode and caching on and of
		if (renderer != null) {
			result = renderer.setData(context).render();
		} else {
			result = tofu.newRenderer(template).setMsgBundle(getStandardMsgBundle()).setData(context).render();
			cache.put(template, renderer);
		}
		return result;
	}

	@Override
	public void createCache() throws SoySyntaxException {
		SoyFileSet.Builder buildCollection = new SoyFileSet.Builder();

		for (Map.Entry<String, File> template : templateFileCollection.entrySet()) {
			for (File file : traverseDirectory(template.getValue())) {
				buildCollection.add(file);
			}
		}

		for (Map.Entry<String,String> template : templateStringCollection.entrySet()) {
			if(template.getValue() != null && validateTemplate(template)){
				
				System.out.println("Adding to Soy templates build collection : " + template.getKey());
				buildCollection.add(template.getValue(), template.getKey());
			}
		}
		//FIXME quickfix to keep app from crashing
		SoyTofu orgTofo = tofu;
		try {
			tofu = buildCollection.build().compileToTofu();
			this.cache.clear();
		} catch (SoySyntaxException e) {
			tofu = orgTofo;
			e.printStackTrace();
			//throw e;
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
	public void addFile(File file) throws IOException {
		templateFileCollection.put(file.getCanonicalPath(), file);
	}

	@Override
	public void addString(String identifier, String template) {
		templateStringCollection.put(identifier, template);
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

	/**
	 * Traverses a directory and adds all files to the result
	 * 
	 * @param folder
	 * @return
	 */
	public static ArrayList<File> traverseDirectory(File file) {
		ArrayList<File> result = new ArrayList<File>();
		if (file.isDirectory()) {
			for (File child : file.listFiles()) {
				result.addAll(traverseDirectory(child));
			}
		} else {
			if (file.getPath().endsWith(".soy")) {
				result.add(file);
			}
		}
		return result;
	}
}
