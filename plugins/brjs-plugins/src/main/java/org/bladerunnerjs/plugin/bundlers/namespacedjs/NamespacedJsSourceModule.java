package org.bladerunnerjs.plugin.bundlers.namespacedjs;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bladerunnerjs.api.Asset;
import org.bladerunnerjs.api.SourceModule;
import org.bladerunnerjs.api.memoization.MemoizedFile;
import org.bladerunnerjs.api.model.exception.ModelOperationException;
import org.bladerunnerjs.api.model.exception.RequirePathException;
import org.bladerunnerjs.model.AssetContainer;
import org.bladerunnerjs.api.BundlableNode;
import org.bladerunnerjs.model.LinkedFileAsset;
import org.bladerunnerjs.model.SourceModulePatch;
import org.bladerunnerjs.model.TrieBasedDependenciesCalculator;
import org.bladerunnerjs.plugin.bundlers.commonjs.CommonJsSourceModule;

import com.Ostermiller.util.ConcatReader;
import com.google.common.base.Joiner;

public class NamespacedJsSourceModule implements SourceModule {
	
	private AssetContainer assetContainer;
	private MemoizedFile assetFile;
	private LinkedFileAsset linkedFileAsset;
	private List<String> requirePaths = new ArrayList<>();
	private String primaryRequirePath;
	private SourceModulePatch patch;
	private TrieBasedDependenciesCalculator trieBasedUseTimeDependenciesCalculator;
	private TrieBasedDependenciesCalculator trieBasedPreExportDefineTimeDependenciesCalculator;
	private TrieBasedDependenciesCalculator trieBasedPostExportDefineTimeDependenciesCalculator;
	private List<Asset> implicitDependencies;
	public static final String JS_STYLE = "namespaced-js";
	
	public NamespacedJsSourceModule(AssetContainer assetContainer, String requirePrefix, MemoizedFile jsFile, List<Asset> implicitDependencies)
	{
		this.assetContainer = assetContainer;
		this.assetFile = jsFile;
		this.linkedFileAsset =  new LinkedFileAsset(assetFile, assetContainer, requirePrefix, implicitDependencies);
		this.implicitDependencies = implicitDependencies;
		
		primaryRequirePath = calculateRequirePath(requirePrefix, jsFile);
		requirePaths.add(primaryRequirePath);

		patch = SourceModulePatch.getPatchForRequirePath(assetContainer, primaryRequirePath);
	}
	
	@Override
	public void addImplicitDependencies(List<Asset> implicitDependencies) {
		this.implicitDependencies.addAll(implicitDependencies);
	}

	@Override
 	public List<Asset> getDependentAssets(BundlableNode bundlableNode) throws ModelOperationException {
		List<Asset> dependendAssets = new ArrayList<>();
		dependendAssets.addAll( getPreExportDefineTimeDependentAssets(bundlableNode) );
		dependendAssets.addAll( getPostExportDefineTimeDependentAssets(bundlableNode) );
		dependendAssets.addAll( getUseTimeDependentAssets(bundlableNode) );
		dependendAssets.addAll(implicitDependencies);
		return dependendAssets;
	}
	
	public Reader getUnalteredContentReader() throws IOException {
		if (patch.patchAvailable()){
			return new ConcatReader( new Reader[] { linkedFileAsset.getReader(), patch.getReader() });
		} else {
			return linkedFileAsset.getReader();
		}
	}
	
	@Override
	public Reader getReader() throws IOException {
		try {
			List<String> requirePaths = getUseTimeDependencyCalculator().getRequirePaths(SourceModule.class);
			String requireAllInvocation = (requirePaths.size() == 0) ? "" : "\n" + calculateDependenciesRequireDefinition(requirePaths) + "\n";
			List<String> staticRequirePaths = getPreExportDefineTimeDependencyCalculator().getRequirePaths(SourceModule.class);
			String staticRequireAllInvocation = (staticRequirePaths.size() == 0) ? "" : " " + calculateDependenciesRequireDefinition(staticRequirePaths);
			String defineBlockHeader = CommonJsSourceModule.COMMONJS_DEFINE_BLOCK_HEADER + staticRequireAllInvocation;
			
			Reader[] readers = new Reader[] { 
				new StringReader( String.format(defineBlockHeader, getPrimaryRequirePath()) ), 
				getUnalteredContentReader(),
				new StringReader( "\n" ),
				new StringReader( "module.exports = " + getPrimaryRequirePath().replaceAll("/", ".") + ";" ),
				new StringReader( requireAllInvocation ),
				new StringReader(CommonJsSourceModule.COMMONJS_DEFINE_BLOCK_FOOTER), 
			};
			return new ConcatReader( readers );
		}
		catch (ModelOperationException e) {
			throw new IOException("Unable to create the SourceModule reader", e);
		}
	}
	
	@Override
	public String getPrimaryRequirePath() {
		return primaryRequirePath;
	}
	
	@Override
	public boolean isEncapsulatedModule() {
		return true;
	}
	
	@Override
	public boolean isGlobalisedModule() {
		return true;
	}
	
	@Override
	public List<Asset> getPreExportDefineTimeDependentAssets(BundlableNode bundlableNode) throws ModelOperationException {
		try {
			 return bundlableNode.assets(this, getPreExportDefineTimeDependencyCalculator().getRequirePaths());
		}
		catch (RequirePathException e) {
			throw new ModelOperationException(e);
		}
	}
	
	@Override
	public List<Asset> getPostExportDefineTimeDependentAssets(BundlableNode bundlableNode) throws ModelOperationException {
		try {
			List<Asset> assets = bundlableNode.assets(this, getPostExportDefineTimeDependencyCalculator().getRequirePaths());
			assets.addAll(bundlableNode.assets(this, getUseTimeDependencyCalculator().getRequirePaths()));
			
			return assets;
		}
		catch (RequirePathException e) {
			throw new ModelOperationException(e);
		}
	}
	
	@Override
	public List<Asset> getUseTimeDependentAssets(BundlableNode bundlableNode) throws ModelOperationException {
		// Note: use-time NamespacedJs dependencies are promoted to post-export define-time since this makes our transpiler much easier to write,
		// and since this also enables our CommonJs singleton-pattern to correctly require all of the dependencies needed for any singletons.
		return Collections.emptyList();
	}
	
	private String calculateDependenciesRequireDefinition(List<String> requirePaths) throws ModelOperationException {
		List<String> requireAllRequirePaths = new ArrayList<>();
		for (String requirePath : requirePaths) {
			if (!requirePath.contains("!")) {
				requireAllRequirePaths.add(requirePath);
			}
		}
		return (requireAllRequirePaths.isEmpty()) ? "" : "requireAll(require, ['" + Joiner.on("','").join(requireAllRequirePaths) + "']);\n";
	}
	
	@Override
	public MemoizedFile file()
	{
		return linkedFileAsset.file();
	}
	
	@Override
	public String getAssetName() {
		return linkedFileAsset.getAssetName();
	}
	
	@Override
	public String getAssetPath() {
		return linkedFileAsset.getAssetPath();
	}
	
	private TrieBasedDependenciesCalculator getPreExportDefineTimeDependencyCalculator() {
		if (trieBasedPreExportDefineTimeDependenciesCalculator == null) {
			trieBasedPreExportDefineTimeDependenciesCalculator = new TrieBasedDependenciesCalculator(assetContainer, this, new NamespacedJsPreExportDefineTimeDependenciesReader.Factory(this), assetFile, patch.getPatchFile());
		}
		return trieBasedPreExportDefineTimeDependenciesCalculator;
	}
	
	private TrieBasedDependenciesCalculator getPostExportDefineTimeDependencyCalculator() {
		if (trieBasedPostExportDefineTimeDependenciesCalculator == null) {
			trieBasedPostExportDefineTimeDependenciesCalculator = new TrieBasedDependenciesCalculator(assetContainer, this, new NamespacedJsPostExportDefineTimeDependenciesReader.Factory(this), assetFile, patch.getPatchFile());
		}
		return trieBasedPostExportDefineTimeDependenciesCalculator;
	}
	
	private TrieBasedDependenciesCalculator getUseTimeDependencyCalculator() {
		if (trieBasedUseTimeDependenciesCalculator == null) {
			trieBasedUseTimeDependenciesCalculator = new TrieBasedDependenciesCalculator(assetContainer, this, new NamespacedJsUseTimeDependenciesReader.Factory(this), assetFile, patch.getPatchFile());
		}
		return trieBasedUseTimeDependenciesCalculator;
	}
	
	@Override
	public List<String> getRequirePaths() {
		return requirePaths;
	}

	@Override
	public AssetContainer assetContainer()
	{
		return assetContainer;
	}
	
	@Override
	public boolean isScopeEnforced() {
		return true;
	}
	
	@Override
	public boolean isRequirable()
	{
		return true;
	}
	
	public static String calculateRequirePath(String requirePrefix, MemoizedFile file) {
		return requirePrefix+"/"+file.requirePathName();
	}
	
}
