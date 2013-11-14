package org.bladerunnerjs.core.plugin.bundler;

import java.util.List;

import org.bladerunnerjs.core.plugin.bundlesource.FileSetFactory;
import org.bladerunnerjs.model.BundleSet;
import org.bladerunnerjs.model.TagHandlerPlugin;
import org.bladerunnerjs.model.exception.request.BundlerProcessingException;


public interface BundlerPlugin extends TagHandlerPlugin, ServletPlugin {
	FileSetFactory getFileSetFactory();
	List<String> generateRequiredDevRequestPaths(BundleSet bundleSet, String locale) throws BundlerProcessingException;
	List<String> generateRequiredProdRequestPaths(BundleSet bundleSet, String locale) throws BundlerProcessingException;
}
