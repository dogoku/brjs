package org.bladerunnerjs.core.plugin.minifier;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.bladerunnerjs.core.plugin.VirtualProxyPlugin;

public class VirtualProxyMinifierPlugin extends VirtualProxyPlugin implements MinifierPlugin {
	private MinifierPlugin minifierPlugin;
	
	public VirtualProxyMinifierPlugin(MinifierPlugin minifierPlugin) {
		super(minifierPlugin);
		this.minifierPlugin = minifierPlugin;
	}
	
	@Override
	public List<String> getSettingNames() {
		return minifierPlugin.getSettingNames();
	}
	
	@Override
	public void minify(String settingName, List<InputSource> inputSources, Writer writer) throws IOException {
		initializePlugin();
		minifierPlugin.minify(settingName, inputSources, writer);
	}
	
	@Override
	public void generateSourceMap(String minifierLevel, List<InputSource> inputSources, Writer writer) throws IOException {
		initializePlugin();
		minifierPlugin.generateSourceMap(minifierLevel, inputSources, writer);
	}
}
