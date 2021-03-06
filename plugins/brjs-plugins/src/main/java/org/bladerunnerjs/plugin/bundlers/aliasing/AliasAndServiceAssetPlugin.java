package org.bladerunnerjs.plugin.bundlers.aliasing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bladerunnerjs.api.Asset;
import org.bladerunnerjs.api.BRJS;
import org.bladerunnerjs.api.memoization.MemoizedFile;
import org.bladerunnerjs.api.plugin.AssetRegistry;
import org.bladerunnerjs.api.plugin.base.AbstractAssetPlugin;
import org.bladerunnerjs.model.AssetContainer;
import org.bladerunnerjs.api.BundlableNode;
import org.bladerunnerjs.plugin.plugins.require.AliasDataSourceModule;
import org.bladerunnerjs.plugin.plugins.require.ServiceDataSourceModule;
import org.bladerunnerjs.plugin.require.AliasCommonJsSourceModule;


public class AliasAndServiceAssetPlugin extends AbstractAssetPlugin
{

	@Override
	public void discoverAssets(AssetContainer assetContainer, MemoizedFile dir, String requirePrefix, List<Asset> implicitDependencies, AssetRegistry assetDiscoveryInitiator)
	{		
		if (assetContainer.dir() == dir) {
			if (assetContainer instanceof BundlableNode) {
				BundlableNode bundlableNode = (BundlableNode) assetContainer;
				createAliasAndServiceDataSourceModules(assetDiscoveryInitiator, bundlableNode);
				addBundlableNodeAliases(implicitDependencies, assetDiscoveryInitiator, bundlableNode);
			}
			
			for (MemoizedFile childDir : getAliasDefinitionsLocations(dir)) {
				addAssetContainerAliases(assetContainer, implicitDependencies, assetDiscoveryInitiator, childDir);
			}
		}
	}

	private void addAssetContainerAliases(AssetContainer assetContainer, List<Asset> implicitDependencies, AssetRegistry assetDiscoveryInitiator, MemoizedFile childDir)
	{
		for (AliasDefinition aliasDefinition : AliasingUtility.aliases(assetContainer, childDir)) {
			if (!assetDiscoveryInitiator.hasRegisteredAsset(AliasCommonJsSourceModule.calculateRequirePath(aliasDefinition))) {
				Asset aliasAsset = new AliasCommonJsSourceModule(assetContainer, aliasDefinition, implicitDependencies);
				assetDiscoveryInitiator.registerAsset(aliasAsset);
			}
		}
	}
	
	private void addBundlableNodeAliases(List<Asset> implicitDependencies, AssetRegistry assetDiscoveryInitiator, BundlableNode bundlableNode)
	{
		for (AliasDefinition aliasDefinition : AliasingUtility.aliases(bundlableNode)) {
			if (!scopeAssetContainersHaveAlias(bundlableNode, aliasDefinition)) {
				Asset aliasAsset = new AliasCommonJsSourceModule(bundlableNode, aliasDefinition, implicitDependencies);
				assetDiscoveryInitiator.registerAsset(aliasAsset);
			}
		}
	}

	private void createAliasAndServiceDataSourceModules(AssetRegistry assetDiscoveryInitiator, BundlableNode bundlableNode)
	{
		List<Asset> dataAssets = Arrays.asList(
			new AliasDataSourceModule(bundlableNode),
			new ServiceDataSourceModule(bundlableNode)
		);
		
		for (Asset asset : dataAssets) {
			if (!assetDiscoveryInitiator.hasRegisteredAsset(asset.getPrimaryRequirePath())) {
				assetDiscoveryInitiator.registerAsset(asset);
			}
		}
	}

	private List<MemoizedFile> getAliasDefinitionsLocations(MemoizedFile dir) {
		List<MemoizedFile> aliasDefinitionsDirs = new ArrayList<>();
		for (MemoizedFile aliasDefintionsRootLocation : Arrays.asList(dir.file("src"), dir.file("resources"))) {
			aliasDefinitionsDirs.add(aliasDefintionsRootLocation);
			aliasDefinitionsDirs.addAll(aliasDefintionsRootLocation.nestedDirs());
		}
		return aliasDefinitionsDirs;
	}

	private boolean scopeAssetContainersHaveAlias(BundlableNode bundlableNode, AliasDefinition aliasDefinition)
	{
		for (AssetContainer scopeAssetContainer : bundlableNode.scopeAssetContainers()) {
			if (scopeAssetContainer == bundlableNode) {
				continue;
			}
			if (scopeAssetContainer.asset(AliasCommonJsSourceModule.calculateRequirePath(aliasDefinition)) != null) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void setBRJS(BRJS brjs)
	{
	}

	
	
}
