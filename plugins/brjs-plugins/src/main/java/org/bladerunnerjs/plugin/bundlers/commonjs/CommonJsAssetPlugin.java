package org.bladerunnerjs.plugin.bundlers.commonjs;

import java.io.FileFilter;
import java.util.List;

import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.bladerunnerjs.api.Asset;
import org.bladerunnerjs.api.BRJS;
import org.bladerunnerjs.api.memoization.MemoizedFile;
import org.bladerunnerjs.api.plugin.AssetDiscoveryInitiator;
import org.bladerunnerjs.api.plugin.base.AbstractAssetPlugin;
import org.bladerunnerjs.model.AssetContainer;


public class CommonJsAssetPlugin extends AbstractAssetPlugin
{
	
	@Override
	public void setBRJS(BRJS brjs)
	{
	}

	@Override
	public void discoverAssets(AssetContainer assetContainer, MemoizedFile dir, String requirePrefix, List<Asset> implicitDependencies, AssetDiscoveryInitiator assetDiscoveryInitiator)
	{
		if (assetContainer.dir() == dir || !dir.jsStyle().equals(CommonJsSourceModule.JS_STYLE)) {
			return;
		}
		
		FileFilter jsFileFilter = new SuffixFileFilter(".js");
		for (MemoizedFile jsFile : dir.listFiles(jsFileFilter)) {
			DefaultCommonJsSourceModule asset = new DefaultCommonJsSourceModule(assetContainer, requirePrefix, jsFile);
			if (!assetDiscoveryInitiator.hasRegisteredAsset(asset.getPrimaryRequirePath())) {
				if (jsFile.isChildOf(assetContainer.file("tests"))) {
					assetDiscoveryInitiator.registerSeedAsset( asset );
				} else {
					assetDiscoveryInitiator.registerAsset( asset );					
				}
			}
		}
	}

}
