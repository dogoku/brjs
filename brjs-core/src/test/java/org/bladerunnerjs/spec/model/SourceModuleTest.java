package org.bladerunnerjs.spec.model;

import static org.bladerunnerjs.testing.specutility.SourceModuleDescriptor.*;

import org.bladerunnerjs.model.App;
import org.bladerunnerjs.model.Aspect;
import org.bladerunnerjs.model.JsLib;
import org.bladerunnerjs.testing.specutility.engine.SpecTest;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class SourceModuleTest extends SpecTest {
	private App app;
	private Aspect aspect;
	private JsLib brjsLib, brjsThirdpartyLib, nodeJsLib;
	
	@Before
	public void initTestObjects() throws Exception {
		given(brjs).hasBeenCreated();
			app = brjs.app("app1");
			aspect = app.aspect("default");
			brjsLib = app.jsLib("brjsLib");
			brjsThirdpartyLib = app.jsLib("thirdpartyLib");
			nodeJsLib = app.jsLib("nodeJsLib");
	}
	
	@Ignore
	@Test
	public void aspectSourceModulesAndAssetLocationsAreAsExpected() throws Exception {
		given(aspect).hasClasses("Class1", "Class2", "pkg.Class3")
			.and(aspect).containsFiles("resources/config1.xml", "resources/dir/config2.xml");
		then(aspect).hasSourceModules(sourceModule("app1/Class1", "Class1"), sourceModule("app1/Class2", "Class2"), sourceModule("app1/pkg/Class3", "pkg.Class3"))
			.and(aspect).hasAssetLocations("resources", "src", "src/pkg")
			.and(aspect).assetLocationHasNoDependencies("resources")
			.and(aspect).assetLocationHasDependencies("src", "resources")
			.and(aspect).assetLocationHasDependencies("src/pkg", "src");
	}
	
	@Ignore
	@Test
	public void brjsLibrarySourceModulesAndAssetLocationsAreAsExpected() throws Exception {
		given(brjsLib).hasClasses("Class1", "Class2", "pkg.Class3")
			.and(brjsLib).containsFiles("resources/config1.xml", "resources/dir/config2.xml");
		then(brjsLib).hasSourceModules(sourceModule("brjsLib/Class1", "br.Class1"), sourceModule("brjsLib/Class2", "br.Class2"), sourceModule("brjsLib/pkg/Class3", "br.pkg.Class3"))
			.and(brjsLib).hasAssetLocations("resources", "src", "src/pkg")
			.and(brjsLib).assetLocationHasNoDependencies("resources")
			.and(brjsLib).assetLocationHasDependencies("src", "resources")
			.and(brjsLib).assetLocationHasDependencies("src/pkg", "src");
	}
	
	@Ignore
	@Test
	public void brjsThirdpartyLibrarySourceModulesAndAssetLocationsAreAsExpected() throws Exception {
		given(brjsThirdpartyLib).containsFileWithContents("library.manifest", "js: file1.js, file2.js")
			.and(brjsThirdpartyLib).containsFiles("file1.js", "file2.js");
		then(brjsThirdpartyLib).hasSourceModules(sourceModule("brjsThirdpartyLib", "file1.js", "file2.js"))
			.and(brjsThirdpartyLib).hasAssetLocations(".")
			.and(brjsThirdpartyLib).assetLocationHasNoDependencies(".");
	}
	
	@Ignore
	@Test
	public void nodeJsLibrarySourceModulesAndAssetLocationsAreAsExpected() throws Exception {
		given(nodeJsLib).containsPackageJsonWithMainSourceModule("lib/file1.js")
			.and(nodeJsLib).containsFiles("lib/file1.js", "lib/file2.js");
		then(nodeJsLib).hasSourceModules(sourceModule("nodeJsLib", "lib/file1.js"), sourceModule("nodeJsLib/lib/file1", "lib/file1.js"), sourceModule("nodeJsLib/lib/file2", "lib/file2.js"))
			.and(nodeJsLib).hasAssetLocations(".")
			.and(nodeJsLib).assetLocationHasNoDependencies(".");
	}
}