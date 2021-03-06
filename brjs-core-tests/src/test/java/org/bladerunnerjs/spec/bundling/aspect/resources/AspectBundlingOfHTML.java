package org.bladerunnerjs.spec.bundling.aspect.resources;

import org.bladerunnerjs.api.App;
import org.bladerunnerjs.api.Aspect;
import org.bladerunnerjs.api.Blade;
import org.bladerunnerjs.api.Bladeset;
import org.bladerunnerjs.api.JsLib;
import org.bladerunnerjs.api.spec.engine.SpecTest;
import org.junit.Before;
import org.junit.Test;

public class AspectBundlingOfHTML extends SpecTest {
	private App app;
	private Aspect aspect;
	private Aspect rootDefaultAspect;
	private Bladeset bladeset;
	private Blade blade;
	private JsLib sdkLib, userLib;
	private StringBuffer response = new StringBuffer();
	
	@Before
	public void initTestObjects() throws Exception
	{
		given(brjs).automaticallyFindsBundlerPlugins()
			.and(brjs).automaticallyFindsMinifierPlugins()
			.and(brjs).hasBeenCreated();
		
			app = brjs.app("app1");
			aspect = app.aspect("default");
			rootDefaultAspect = app.defaultAspect();
			bladeset = app.bladeset("bs");
			blade = bladeset.blade("b1");
			sdkLib = brjs.sdkLib("br");
			userLib = app.jsLib("userLib");
	}
	
	// Aspect
	@Test
	public void aspectClassesReferredToInAspectHTMlFilesAreBundled() throws Exception {
		given(aspect).hasClasses("appns/Class1")
			.and(aspect).resourceFileRefersTo("html/view.html", "appns.Class1");
		when(aspect).requestReceivedInDev("js/dev/combined/bundle.js", response);
		then(response).containsCommonJsClasses("appns.Class1");
	}
	
	@Test
	public void rootAspectClassesReferredToInAspectHTMlFilesAreBundled() throws Exception {
		given(rootDefaultAspect).hasClasses("appns/Class1")
			.and(rootDefaultAspect).resourceFileRefersTo("html/view.html", "appns.Class1");
		when(rootDefaultAspect).requestReceivedInDev("js/dev/combined/bundle.js", response);
		then(response).containsCommonJsClasses("appns.Class1");
	}

	// Bladeset
	@Test
	public void bladesetClassesReferredToInAspectHTMlFilesAreBundled() throws Exception {
		given(bladeset).hasClasses("appns/bs/Class1", "appns/bs/Class2")
			.and(aspect).resourceFileRefersTo("html/view.html", "appns.bs.Class1");
		when(aspect).requestReceivedInDev("js/dev/combined/bundle.js", response);
		then(response).containsCommonJsClasses("appns.bs.Class1");
	}
	
	// Blade
	@Test
	public void bladeClassesReferredToInAspectHTMlFilesAreBundled() throws Exception {
		given(blade).hasClasses("appns/bs/b1/Class1", "appns/bs/b1/Class2")
			.and(aspect).resourceFileRefersTo("html/view.html", "appns.bs.b1.Class1");
		when(aspect).requestReceivedInDev("js/dev/combined/bundle.js", response);
		then(response).containsCommonJsClasses("appns.bs.b1.Class1");
	}
	
	// SDK BRJS Lib
	@Test
	public void aspectCanBundleSdkLibHTMLResources() throws Exception {
		given(sdkLib).hasBeenCreated()
			.and(sdkLib).hasNamespacedJsPackageStyle()
			.and(sdkLib).containsResourceFileWithContents("html/workbench.html", "<div id='br.workbench-view'></div>")
			.and(sdkLib).hasClass("br.workbench.ui.Workbench")
			.and(aspect).containsResourceFileWithContents("aspect.html", "<div id='appns.aspect-view'></div>")
			.and(aspect).indexPageRefersTo("br.workbench.ui.Workbench");
		when(aspect).requestReceivedInDev("html/bundle.html", response);
		then(response).containsOrderedTextFragments(
				"<!-- workbench.html -->",
				"<div id='br.workbench-view'></div>",
				"<!-- aspect.html -->",
				"<div id='appns.aspect-view'></div>");
	}
	
	
	// User library (specific to an app)
	@Test
	public void aspectCanBundleUserLibHTMLRresources() throws Exception {
		given(userLib).hasBeenCreated()
			.and(userLib).hasNamespacedJsPackageStyle()
			.and(userLib).containsResourceFileWithContents("html/userLib.html", "<div id='userLib.my-view'></div>")
			.and(userLib).hasClass("userLib.Class1")
			.and(aspect).containsResourceFileWithContents("aspect.html", "<div id='appns.aspect-view'></div>")
			.and(aspect).indexPageRefersTo("userLib.Class1");
		when(aspect).requestReceivedInDev("html/bundle.html", response);
		then(response).containsOrderedTextFragments(
				"<!-- userLib.html -->",
				"<div id='userLib.my-view'></div>",
				"<!-- aspect.html -->",
				"<div id='appns.aspect-view'></div>" );
	}
	
	@Test
	public void bladesetResourcesAreLoadedEvenIfTheBladesetHasNoSource() throws Exception {
		given(bladeset).containsResourceFileWithContents("file.xml", "<some-xml/>")
			.and(blade).hasClass("appns/bs/b1/Class1")
    		.and(aspect).indexPageRequires("appns/bs/b1/Class1");
    	when(aspect).requestReceivedInDev("xml/bundle.xml", response);
    	then(response).containsText("<some-xml/>");
	}
	
	@Test
	public void bladeResourcesAreLoadedEvenIfTheBladesetHasNoSource() throws Exception {
		given(blade).containsResourceFileWithContents("file.xml", "<some-xml id='appns.bs.b1.someId'></some-xml>")
			.and(aspect).indexPageHasContent("appns.bs.b1.someId");
		when(aspect).requestReceivedInDev("xml/bundle.xml", response);
		then(response).containsText("<some-xml id='appns.bs.b1.someId'></some-xml>");
	}
	
	@Test
	public void bladeResourcesAreLoadedEvenIfTheBladesetHasNoSourceAndSecondaryIdIsUsed() throws Exception {
		given(blade).containsResourceFileWithContents("file.xml", "<some-xml id='appns.bs.b1.someId'></some-xml><some-xml id='appns.bs.b1.anotherId'></some-xml>")
			.and(aspect).indexPageHasContent("appns.bs.b1.anotherId");
		when(aspect).requestReceivedInDev("xml/bundle.xml", response);
		then(response).containsText("<some-xml id='appns.bs.b1.someId'></some-xml><some-xml id='appns.bs.b1.anotherId'></some-xml>");
	}
	
}
