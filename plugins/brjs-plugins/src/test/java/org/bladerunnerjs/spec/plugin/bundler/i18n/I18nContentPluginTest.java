package org.bladerunnerjs.spec.plugin.bundler.i18n;

import org.bladerunnerjs.api.App;
import org.bladerunnerjs.api.AppConf;
import org.bladerunnerjs.api.Aspect;
import org.bladerunnerjs.api.Blade;
import org.bladerunnerjs.api.BladerunnerConf;
import org.bladerunnerjs.api.Bladeset;
import org.bladerunnerjs.api.model.exception.NamespaceException;
import org.bladerunnerjs.api.spec.engine.SpecTest;
import org.bladerunnerjs.model.SdkJsLib;
import org.bladerunnerjs.api.BladeWorkbench;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;


public class I18nContentPluginTest extends SpecTest
{

	private App app;
	private AppConf appConf;
	private Aspect aspect;
	private StringBuffer response = new StringBuffer();
	private Bladeset bladeset;
	private Blade blade;
	private BladeWorkbench workbench;
	private BladerunnerConf bladerunnerConf;
	private SdkJsLib sdkLib;
	private Bladeset defaultBladeset;
	private Blade bladeInDefaultBladeset;
	private Aspect defaultAspect;
	
	@Before
	public void initTestObjects() throws Exception
	{
		given(brjs).automaticallyFindsBundlerPlugins()
			.and(brjs).automaticallyFindsMinifierPlugins()
			.and(brjs).hasBeenCreated();
			app = brjs.app("app1");
			appConf = app.appConf();
			aspect = app.aspect("default");
			defaultAspect = app.defaultAspect();
			bladeset = app.bladeset("bs");
			blade = bladeset.blade("b1");
			workbench = blade.workbench();
			bladerunnerConf = brjs.bladerunnerConf();
			sdkLib = brjs.sdkLib("br");
			defaultBladeset = app.defaultBladeset();
			bladeInDefaultBladeset = defaultBladeset.blade("b1");
	}
	
	@Test
	public void theRequestsGeneratedIsTiedToTheLocalesTheAppSupports() throws Exception {
		then(aspect).prodAndDevRequestsForContentPluginsAre("i18n", "i18n/en.js");
	}
	
	@Test
	public void fullLocaleRequestsWillAlsoBeGeneratedIfTheAppConfIsConfiguredForThis() throws Exception {
		given(appConf).supportsLocales("en", "en_GB");
		then(aspect).prodAndDevRequestsForContentPluginsAre("i18n", "i18n/en.js", "i18n/en_GB.js");
	}
	
	@Test
	public void prefixOfResponseChecksBRJSI18nPropertiesExists() throws Exception {
		given(app).hasBeenCreated()
			.and(aspect).hasBeenCreated()
			.and(aspect).containsEmptyFile("index.html");
		when(aspect).requestReceivedInDev("i18n/en_GB.js", response);
		then(response).containsOrderedTextFragments("if (!window.$_brjsI18nProperties) { window.$_brjsI18nProperties = {} };", "window.$_brjsI18nProperties['en_GB'] = {};");
	}
	
	@Test
	public void suffixOfResponseSetsBRJSUseLocale() throws Exception {
		given(app).hasBeenCreated()
			.and(aspect).hasBeenCreated()
			.and(aspect).containsEmptyFile("index.html");
		when(aspect).requestReceivedInDev("i18n/en_GB.js", response);
		then(response).containsOrderedTextFragments("window.$_brjsI18nProperties['en_GB'] = {};", "window.$_brjsI18nUseLocale = 'en_GB';");
	}
	
	@Test
	public void correctLocaleIsRetrieved() throws Exception {
		given(app).hasBeenCreated()
			.and(aspect).hasBeenCreated()
			.and(aspect).containsEmptyFile("index.html");
		when(aspect).requestReceivedInDev("i18n/en_GB.js", response);
		then(response).containsText("window.$_brjsI18nProperties['en_GB'] = ");
	}
	
	@Test
	public void requestForI18nWithoutAnyAssetsReturnsEmptyResponse() throws Exception 
	{
		given(app).hasBeenCreated()
			.and(aspect).hasBeenCreated()
			.and(aspect).containsEmptyFile("index.html");
		when(aspect).requestReceivedInDev("i18n/en_GB.js", response);
		then(response).containsText("window.$_brjsI18nProperties['en_GB'] = {};");
	}
	
	@Test
	public void i18nFilesForTheGivenLocaleInAspectResourcesAreBundled() throws Exception 
	{
		given(app).hasBeenCreated()
			.and(aspect).hasBeenCreated()
			.and(aspect).containsEmptyFile("index.html")
			.and(aspect).containsResourceFileWithContents("en_GB.properties", "appns.property=property value");
		when(aspect).requestReceivedInDev("i18n/en_GB.js", response);
		then(response).containsText(	
				"window.$_brjsI18nProperties['en_GB'] = {\n"
				+ "  \"appns.property\": \"property value\"\n"
				+ "};");
	}
	
	@Test
	public void i18nFilesForOtherLocalesInAspectResourcesAreIgnored() throws Exception 
	{
		given(app).hasBeenCreated()
			.and(aspect).hasBeenCreated()
			.and(aspect).containsEmptyFile("index.html")
			.and(aspect).containsResourceFileWithContents("en_GB.properties", "appns.property=property value")
			.and(aspect).containsResourceFileWithContents("de_DE.properties", "appns.property=a different value");
		when(aspect).requestReceivedInDev("i18n/en_GB.js", response);
		then(response).containsText(	
				"window.$_brjsI18nProperties['en_GB'] = {\n"
				+ "  \"appns.property\": \"property value\"\n"
				+ "};");
	}
	
	@Test
	public void requestsForALocaleCanContainTheLanguageOnly() throws Exception 
	{
		given(app).hasBeenCreated()
			.and(aspect).hasBeenCreated()
			.and(aspect).containsEmptyFile("index.html")
			.and(aspect).containsResourceFileWithContents("en.properties", "appns.property=property value");
		when(aspect).requestReceivedInDev("i18n/en.js", response);
		then(response).containsText(	
				"window.$_brjsI18nProperties['en'] = {\n"
				+ "  \"appns.property\": \"property value\"\n"
				+ "};");
	}
	
	@Test
	public void requestsForALanguageDoesntIncludeLocationSpecificProperties() throws Exception 
	{
		given(app).hasBeenCreated()
			.and(aspect).hasBeenCreated()
			.and(aspect).containsEmptyFile("index.html")
			.and(aspect).containsResourceFileWithContents("en.properties", "appns.property=property value")
			.and(aspect).containsResourceFileWithContents("en_GB.properties", "appns.property=another value");
		when(aspect).requestReceivedInDev("i18n/en.js", response);
		then(response).containsText(	
				"window.$_brjsI18nProperties['en'] = {\n"
				+ "  \"appns.property\": \"property value\"\n"
				+ "};");
	}
	
	@Test
	public void locationSpecificPropertiesAreAddedToLanguageValues() throws Exception 
	{
		given(app).hasBeenCreated()
			.and(aspect).hasBeenCreated()
			.and(aspect).containsEmptyFile("index.html")
			.and(aspect).containsResourceFileWithContents("en.properties", "appns.some.property=property value")
			.and(aspect).containsResourceFileWithContents("en_GB.properties", "appns.another.property=another value");
		when(aspect).requestReceivedInDev("i18n/en_GB.js", response);
		then(response).containsText(	
				"window.$_brjsI18nProperties['en_GB'] = {\n"
				+ "  \"appns.another.property\": \"another value\",\n"
				+ "  \"appns.some.property\": \"property value\"\n"
				+ "};");
	}

	@Test
	public void locationSpecificPropertiesOverrideLanguageProperties() throws Exception 
	{
		given(app).hasBeenCreated()
			.and(aspect).hasBeenCreated()
			.and(aspect).containsEmptyFile("index.html")
			.and(aspect).containsResourceFileWithContents("en.properties", "appns.property=property value")
			.and(aspect).containsResourceFileWithContents("en_GB.properties", "appns.property=another value");
		when(aspect).requestReceivedInDev("i18n/en_GB.js", response);
		then(response).containsText(	
				"window.$_brjsI18nProperties['en_GB'] = {\n"
				+ "  \"appns.property\": \"another value\"\n"
				+ "};");
	}
	
	@Test
	public void locationSpecificRequestWillUseLanguageOnlyProperties() throws Exception 
	{
		given(app).hasBeenCreated()
			.and(aspect).hasBeenCreated()
			.and(aspect).containsEmptyFile("index.html")
			.and(aspect).containsResourceFileWithContents("en.properties", "appns.property=property value");
		when(aspect).requestReceivedInDev("i18n/en_GB.js", response);
		then(response).containsText(	
				"window.$_brjsI18nProperties['en_GB'] = {\n"
				+ "  \"appns.property\": \"property value\"\n"
				+ "};");
	}
	
	@Test
	public void propertiesCanBeInASubfolderOfResources() throws Exception 
	{
		given(app).hasBeenCreated()
			.and(aspect).hasBeenCreated()
			.and(aspect).containsEmptyFile("index.html")
			.and(aspect).containsResourceFileWithContents("i18n/en.properties", "appns.property=property value");
		when(aspect).requestReceivedInDev("i18n/en_GB.js", response);
		then(response).containsText(	
				"window.$_brjsI18nProperties['en_GB'] = {\n"
				+ "  \"appns.property\": \"property value\"\n"
				+ "};");
	}
	
	@Test
	public void propertiesCanBeInASrcDirectoryWithTheAssociatedClass() throws Exception 
	{
		given(app).hasBeenCreated()
			.and(aspect).hasBeenCreated()
			.and(aspect).indexPageRefersTo("appns.Class")
			.and(aspect).hasClass("appns/Class")
			.and(aspect).containsFileWithContents("src/appns/en.properties", "appns.property=property value");
		when(aspect).requestReceivedInDev("i18n/en_GB.js", response);
		then(response).containsText(	
				"window.$_brjsI18nProperties['en_GB'] = {\n"
				+ "  \"appns.property\": \"property value\"\n"
				+ "};");
	}
	
	
	@Test
	public void bladePropertiesAreOverriddenByAspectProperties() throws Exception 
	{
		given(app).hasBeenCreated()
			.and(aspect).indexPageRefersTo("appns.bs.b1.Class")
			.and(blade).hasClass("appns/bs/b1/Class")
			.and(blade).containsResourceFileWithContents("en.properties", "appns.bs.b1.property=blade value")
			.and(aspect).containsResourceFileWithContents("en.properties", "appns.bs.b1.property=aspect value");
		when(aspect).requestReceivedInDev("i18n/en_GB.js", response);
		then(response).containsText(	
				"window.$_brjsI18nProperties['en_GB'] = {\n"
				+ "  \"appns.bs.b1.property\": \"aspect value\"\n"
				+ "};");
	}
	
	@Test
	public void bladePropertiesAreOverriddenByWorkbenchProperties() throws Exception 
	{
		given(app).hasBeenCreated()
		.and(workbench).indexPageRequires("appns/bs/b1/Class")
		.and(blade).hasClass("appns/bs/b1/Class")
		.and(blade).containsResourceFileWithContents("en.properties", "appns.bs.b1.property=blade value")
		.and(workbench).containsResourceFileWithContents("en.properties", "appns.bs.b1.property=workbench value");
		when(workbench).requestReceivedInDev("i18n/en_GB.js", response);
		then(response).containsText(	
				"window.$_brjsI18nProperties['en_GB'] = {\n"
				+ "  \"appns.bs.b1.property\": \"workbench value\"\n"
				+ "};");
	}
	
	@Test
	public void i18nPropertyKeysMustMatchTheAspectNamespace() throws Exception 
	{
		given(blade).hasClass("appns/bs/b1/Class")
			.and(blade).containsResourceFileWithContents("en_GB.properties", "some.property=property value")
			.and(aspect).indexPageRefersTo("appns.bs.b1.Class");
		when(aspect).requestReceivedInDev("i18n/en_GB.js", response);
		then(exceptions).verifyException(NamespaceException.class, "some.property", "appns.bs.b1.*");
	}
	
	@Test
	public void i18nPropertyKeysDefinedWithTheAspectDoNotNeedToBeNamespace() throws Exception 
	{
		given(aspect).containsEmptyFile("index.html")
			.and(aspect).containsResourceFileWithContents("en_GB.properties", "some.property=property value");
		when(aspect).requestReceivedInDev("i18n/en_GB.js", response);
		then(exceptions).verifyNoOutstandingExceptions();
	}
	
	@Test
	public void propertiesAreOrderedAlphabetically() throws Exception 
	{
		given(app).hasBeenCreated()
			.and(aspect).hasBeenCreated()
			.and(aspect).containsEmptyFile("index.html")
			.and(aspect).containsResourceFileWithContents("en.properties", "appns.p3=v3\nappns.p1=v1\nappns.p2=v2\n");
		when(aspect).requestReceivedInDev("i18n/en_GB.js", response);
		then(response).containsText(	
				"window.$_brjsI18nProperties['en_GB'] = {\n"
				+ "  \"appns.p1\": \"v1\",\n"
				+ "  \"appns.p2\": \"v2\",\n"
				+ "  \"appns.p3\": \"v3\"\n"
				+ "};");
	}
	
	@Test
	public void newLinesWithinPropertiesArePreserved() throws Exception 
	{
		given(app).hasBeenCreated()
			.and(aspect).hasBeenCreated()
			.and(aspect).containsEmptyFile("index.html")
			.and(aspect).containsResourceFileWithContents("en.properties", "appns.p1=v\\n1");
		when(aspect).requestReceivedInDev("i18n/en_GB.js", response);
		then(response).containsText(	
				"window.$_brjsI18nProperties['en_GB'] = {\n"
				+ "  \"appns.p1\": \"v\\n1\"\n"
				+ "};");
	}
	
	@Test
	public void quotesWithinPropertiessAreProperlyEscaped() throws Exception 
	{
		given(app).hasBeenCreated()
			.and(aspect).hasBeenCreated()
			.and(aspect).containsEmptyFile("index.html")
			.and(aspect).containsResourceFileWithContents("en.properties", "appns.p1=\"quoted\"");
		when(aspect).requestReceivedInDev("i18n/en_GB.js", response);
		then(response).containsText(	
				"window.$_brjsI18nProperties['en_GB'] = {\n"
				+ "  \"appns.p1\": \"\\\"quoted\\\"\"\n"
				+ "};");
	}
	
	@Test
	public void weCanUseUTF8() throws Exception {
		given(bladerunnerConf).defaultFileCharacterEncodingIs("UTF-8")
			.and().activeEncodingIs("UTF-8")
			.and(aspect).hasBeenCreated()
			.and(aspect).containsEmptyFile("index.html")
			.and(aspect).containsResourceFileWithContents("en.properties", "appns.p1=\"$£€\"");
		when(aspect).requestReceivedInDev("i18n/en_GB.js", response);
		then(response).containsText(	
				"window.$_brjsI18nProperties['en_GB'] = {\n"
				+ "  \"appns.p1\": \"\\\"$£€\\\"\"\n"
				+ "};");
	}
	
	@Test
	public void weCanUseLatin1() throws Exception {
		given(bladerunnerConf).defaultFileCharacterEncodingIs("ISO-8859-1")
    		.and().activeEncodingIs("ISO-8859-1")
    		.and(aspect).hasBeenCreated()
    		.and(aspect).containsEmptyFile("index.html")
    		.and(aspect).containsResourceFileWithContents("en.properties", "appns.p1=\"$£\"");
    	when(aspect).requestReceivedInDev("i18n/en_GB.js", response);
    	then(response).containsText(	
    			"window.$_brjsI18nProperties['en_GB'] = {\n"
    			+ "  \"appns.p1\": \"\\\"$£\\\"\"\n"
    			+ "};");
	}
	
	@Test
	public void theCorrectRequirePrefixIsUsedForNamespaceEnforcement() throws Exception {
		given(aspect).hasClass("appns/AspectClass")
			.and(sdkLib).containsFileWithContents("br-lib.conf", "requirePrefix: foo/bar")
			.and(sdkLib).hasClass("foo/bar/SdkClass")
			.and(aspect).indexPageRefersTo("appns.AspectClass")
			.and(aspect).classRequires("appns/AspectClass", "foo/bar/SdkClass")
			.and(sdkLib).containsResourceFileWithContents("en_GB.properties", "foo.bar.property=property value");
		when(aspect).requestReceivedInDev("i18n/en_GB.js", response);
		then(response).containsText(	
				"window.$_brjsI18nProperties['en_GB'] = {\n"
				+ "  \"foo.bar.property\": \"property value\"\n"
				+ "};");
	}
	
	@Test
	public void bladeI18nPropertiesInDefaultBladesetCanBeBundled() throws Exception {
		given(bladeInDefaultBladeset).hasClass("appns/b1/BladeClass")
			.and(bladeInDefaultBladeset).containsFileWithContents("resources/en_GB.properties", "appns.b1.property=property value")
			.and(aspect).indexPageRequires("appns/b1/BladeClass");
		when(aspect).requestReceivedInDev("i18n/en_GB.js", response);
		then(response).containsText("\"appns.b1.property\": \"property value\"");
	}
	
	@Test
	public void defaultAspectI18nPropertiesCanBeBundled() throws Exception {
		given(defaultAspect).hasClass("appns/AspectClass")
			.and(defaultAspect).containsFileWithContents("resources/en_GB.properties", "appns.property=property value")
			.and(defaultAspect).indexPageRequires("appns/AspectClass");
		when(defaultAspect).requestReceivedInDev("i18n/en_GB.js", response);
		then(response).containsText("\"appns.property\": \"property value\"");
	}
	
	
	/* ** Dependencies from HTML files ** */
	
	@Test
	public void i18nPropertiesInBladesetResourcesReferencedFromAnAspectHtmlTemplateAreBundled() throws Exception {
		given(defaultAspect).containsResourceFileWithContents("view.html", "<div>@{appns.bs.key@}</div>")
    		.and(bladeset).containsFileWithContents("resources/en_GB.properties", "appns.bs.key=translation");
    	when(defaultAspect).requestReceivedInDev("i18n/en_GB.js", response);
    	then(response).containsText("\"appns.bs.key\": \"translation\"");
	}
	
	@Test
	public void i18nPropertiesInBladesetSrcReferencedFromAnAspectHtmlTemplateAreBundled() throws Exception {
		given(defaultAspect).containsResourceFileWithContents("view.html", "<div>@{appns.bs.key@}</div>")
    		.and(bladeset).containsFileWithContents("src/en_GB.properties", "appns.bs.key=translation");
    	when(defaultAspect).requestReceivedInDev("i18n/en_GB.js", response);
    	then(response).containsText("\"appns.bs.key\": \"translation\"");
	}
	
	@Test @Ignore
	public void i18nPropertiesInBladesetSrcPackageDirReferencedFromAnAspectHtmlTemplateAreBundled() throws Exception {
		given(defaultAspect).containsResourceFileWithContents("view.html", "<div>@{appns.bs.pkg.key@}</div>")
    		.and(bladeset).containsFileWithContents("src/pkg/en_GB.properties", "appns.bs.pkg.key=translation");
    	when(defaultAspect).requestReceivedInDev("i18n/en_GB.js", response);
    	then(response).containsText("\"appns.bs.pkg.key\": \"translation\"");
	}
	
	@Test
	public void i18nPropertiesInBladeResourcesReferencedFromAnAspectHtmlTemplateAreBundled() throws Exception {
		given(defaultAspect).containsResourceFileWithContents("view.html", "<div>@{appns.bs.b1.key@}</div>")
			.and(blade).containsFileWithContents("resources/en_GB.properties", "appns.bs.b1.key=translation");
		when(defaultAspect).requestReceivedInDev("i18n/en_GB.js", response);
		then(response).containsText("\"appns.bs.b1.key\": \"translation\"");
	}
	
	@Test
	public void i18nPropertiesInBladeSrcReferencedFromAnAspectHtmlTemplateAreBundled() throws Exception {
		given(defaultAspect).containsResourceFileWithContents("view.html", "<div>@{appns.bs.b1.key@}</div>")
			.and(blade).containsFileWithContents("src/en_GB.properties", "appns.bs.b1.key=translation");
		when(defaultAspect).requestReceivedInDev("i18n/en_GB.js", response);
		then(response).containsText("\"appns.bs.b1.key\": \"translation\"");
	}
	
	@Test @Ignore
	public void i18nPropertiesInBladeSrcPackageDirReferencedFromAnAspectHtmlTemplateAreBundled() throws Exception {
		given(defaultAspect).containsResourceFileWithContents("view.html", "<div>@{appns.bs.b1.pkg.key@}</div>")
			.and(blade).containsFileWithContents("src/pkg/en_GB.properties", "appns.bs.b1.pkg.key=translation");
		when(defaultAspect).requestReceivedInDev("i18n/en_GB.js", response);
		then(response).containsText("\"appns.bs.b1.pkg.key\": \"translation\"");
	}
	
	@Test @Ignore
	public void i18nPropertiesInLibResourcesReferencedFromAnAspectHtmlTemplateAreBundled() throws Exception {
		given(defaultAspect).containsResourceFileWithContents("view.html", "<div>@{lib.key@}</div>")
			.and(sdkLib).containsFileWithContents("resources/en_GB.properties", "lib.key=translation")
			.and(sdkLib).containsFileWithContents("br-lib.conf", "requirePrefix: lib");
		when(defaultAspect).requestReceivedInDev("i18n/en_GB.js", response);
		then(response).containsText("\"lib.key\": \"translation\"");
	}
	
	@Test @Ignore
	public void i18nPropertiesInLibSrcReferencedFromAnAspectHtmlTemplateAreBundled() throws Exception {
		given(defaultAspect).containsResourceFileWithContents("view.html", "<div>@{lib.key@}</div>")
			.and(sdkLib).containsFileWithContents("src/en_GB.properties", "lib.key=translation")
			.and(sdkLib).containsFileWithContents("br-lib.conf", "requirePrefix: appns");
		when(defaultAspect).requestReceivedInDev("i18n/en_GB.js", response);
		then(response).containsText("\"lib.key\": \"translation\"");
	}
	
	@Test @Ignore
	public void i18nPropertiesInLibSrcPackageDirReferencedFromAnAspectHtmlTemplateAreBundled() throws Exception {
		given(defaultAspect).containsResourceFileWithContents("view.html", "<div>@{lib.pkg.key@}</div>")
			.and(sdkLib).containsFileWithContents("src/pkg/en_GB.properties", "lib.pkg.key=translation")
			.and(sdkLib).containsFileWithContents("br-lib.conf", "requirePrefix: appns");
		when(defaultAspect).requestReceivedInDev("i18n/en_GB.js", response);
		then(response).containsText("\"lib.key\": \"translation\"");
	}
	
	@Test @Ignore
	public void i18nPropertiesInLibResourcesReferencedFromAnotherLibTestHtmlTemplateAreBundled() throws Exception {
		SdkJsLib sdkLib2 = brjs.sdkLib("test");
		given(sdkLib2).containsResourceFileWithContents("view.html", "<div>@{lib.key@}</div>")
			.and(sdkLib).containsFileWithContents("resources/en_GB.properties", "lib.key=translation")
			.and(sdkLib).containsFileWithContents("br-lib.conf", "requirePrefix: appns");
		when(sdkLib2.testType("ut").defaultTestTech()).requestReceivedInDev("i18n/en_GB.js", response);
		then(response).containsText("\"lib.key\": \"translation\"");
	}
	
	@Test @Ignore
	public void i18nPropertiesInLibSrcReferencedFromAotherLibTestHtmlTemplateAreBundled() throws Exception {
		SdkJsLib sdkLib2 = brjs.sdkLib("test");
		given(sdkLib2).containsResourceFileWithContents("view.html", "<div>@{lib.key@}</div>")
			.and(sdkLib).containsFileWithContents("src/en_GB.properties", "lib.key=translation")
			.and(sdkLib).containsFileWithContents("br-lib.conf", "requirePrefix: appns");
		when(sdkLib2.testType("ut").defaultTestTech()).requestReceivedInDev("i18n/en_GB.js", response);
		then(response).containsText("\"lib.key\": \"translation\"");
	}
	
	@Test @Ignore
	public void i18nPropertiesInLibSrcPackageDirReferencedFromAotherLibTestHtmlTemplateAreBundled() throws Exception {
		SdkJsLib sdkLib2 = brjs.sdkLib("test");
		given(sdkLib2).containsResourceFileWithContents("view.html", "<div>@{lib.pkg.key@}</div>")
			.and(sdkLib).containsFileWithContents("src/pkg/en_GB.properties", "lib.pkg.key=translation")
			.and(sdkLib).containsFileWithContents("br-lib.conf", "requirePrefix: appns");
		when(sdkLib2.testType("ut").defaultTestTech()).requestReceivedInDev("i18n/en_GB.js", response);
		then(response).containsText("\"lib.pkg.key\": \"translation\"");
	}
	
	
	/* ** Dependencies from XML files ** */
	
	@Test
	public void i18nPropertiesInBladesetResourcesReferencedFromAnAspectXmlFileAreBundled() throws Exception {
		given(defaultAspect).containsResourceFileWithContents("config.xml", "<div>@{appns.bs.key@}</div>")
    		.and(bladeset).containsFileWithContents("resources/en_GB.properties", "appns.bs.key=translation");
    	when(defaultAspect).requestReceivedInDev("i18n/en_GB.js", response);
    	then(response).containsText("\"appns.bs.key\": \"translation\"");
	}
	
	@Test
	public void i18nPropertiesInBladesetSrcReferencedFromAnAspectXmlFileAreBundled() throws Exception {
		given(defaultAspect).containsResourceFileWithContents("config.xml", "<div>@{appns.bs.key@}</div>")
    		.and(bladeset).containsFileWithContents("src/en_GB.properties", "appns.bs.key=translation");
    	when(defaultAspect).requestReceivedInDev("i18n/en_GB.js", response);
    	then(response).containsText("\"appns.bs.key\": \"translation\"");
	}
	
	@Test @Ignore
	public void i18nPropertiesInBladesetSrcPackageDirReferencedFromAnAspectXmlFileAreBundled() throws Exception {
		given(defaultAspect).containsResourceFileWithContents("config.xml", "<div>@{appns.bs.pkg.key@}</div>")
    		.and(bladeset).containsFileWithContents("src/pkg/en_GB.properties", "appns.bs.pkg.key=translation");
    	when(defaultAspect).requestReceivedInDev("i18n/en_GB.js", response);
    	then(response).containsText("\"appns.bs.pkg.key\": \"translation\"");
	}
	
	@Test
	public void i18nPropertiesInBladeResourcesReferencedFromAnAspectXmlFileAreBundled() throws Exception {
		given(defaultAspect).containsResourceFileWithContents("config.xml", "<div>@{appns.bs.b1.key@}</div>")
			.and(blade).containsFileWithContents("resources/en_GB.properties", "appns.bs.b1.key=translation");
		when(defaultAspect).requestReceivedInDev("i18n/en_GB.js", response);
		then(response).containsText("\"appns.bs.b1.key\": \"translation\"");
	}
	
	@Test
	public void i18nPropertiesInBladeSrcReferencedFromAnAspectXmlFileAreBundled() throws Exception {
		given(defaultAspect).containsResourceFileWithContents("config.xml", "<div>@{appns.bs.b1.key@}</div>")
			.and(blade).containsFileWithContents("src/en_GB.properties", "appns.bs.b1.key=translation");
		when(defaultAspect).requestReceivedInDev("i18n/en_GB.js", response);
		then(response).containsText("\"appns.bs.b1.key\": \"translation\"");
	}
	
	@Test @Ignore
	public void i18nPropertiesInBladeSrcPackageDirReferencedFromAnAspectXmlFileAreBundled() throws Exception {
		given(defaultAspect).containsResourceFileWithContents("config.xml", "<div>@{appns.bs.b1.pkg.key@}</div>")
			.and(blade).containsFileWithContents("src/pkg/en_GB.properties", "appns.bs.b1.pkg.key=translation");
		when(defaultAspect).requestReceivedInDev("i18n/en_GB.js", response);
		then(response).containsText("\"appns.bs.b1.pkg.key\": \"translation\"");
	}
	
	@Test @Ignore
	public void i18nPropertiesInLibResourcesReferencedFromAnAspectXmlFileAreBundled() throws Exception {
		given(defaultAspect).containsResourceFileWithContents("config.xml", "<div>@{lib.key@}</div>")
			.and(sdkLib).containsFileWithContents("resources/en_GB.properties", "lib.key=translation")
			.and(sdkLib).containsFileWithContents("br-lib.conf", "requirePrefix: lib");
		when(defaultAspect).requestReceivedInDev("i18n/en_GB.js", response);
		then(response).containsText("\"lib.key\": \"translation\"");
	}
	
	@Test @Ignore
	public void i18nPropertiesInLibSrcReferencedFromAnAspectXmlFileAreBundled() throws Exception {
		given(defaultAspect).containsResourceFileWithContents("config.xml", "<div>@{lib.key@}</div>")
			.and(sdkLib).containsFileWithContents("src/en_GB.properties", "lib.key=translation")
			.and(sdkLib).containsFileWithContents("br-lib.conf", "requirePrefix: appns");
		when(defaultAspect).requestReceivedInDev("i18n/en_GB.js", response);
		then(response).containsText("\"lib.key\": \"translation\"");
	}
	
	@Test @Ignore
	public void i18nPropertiesInLibSrcPackageDirReferencedFromAnAspectXmlFileAreBundled() throws Exception {
		given(defaultAspect).containsResourceFileWithContents("config.xml", "<div>@{lib.pkg.key@}</div>")
			.and(sdkLib).containsFileWithContents("src/pkg/en_GB.properties", "lib.pkg.key=translation")
			.and(sdkLib).containsFileWithContents("br-lib.conf", "requirePrefix: appns");
		when(defaultAspect).requestReceivedInDev("i18n/en_GB.js", response);
		then(response).containsText("\"lib.pkg.key\": \"translation\"");
	}
	
	@Test @Ignore
	public void i18nPropertiesInLibResourcesReferencedFromAnotherLibTestXmlFileAreBundled() throws Exception {
		SdkJsLib sdkLib2 = brjs.sdkLib("test");
		given(sdkLib2).containsResourceFileWithContents("config.xml", "<div>@{lib.key@}</div>")
			.and(sdkLib).containsFileWithContents("resources/en_GB.properties", "lib.key=translation")
			.and(sdkLib).containsFileWithContents("br-lib.conf", "requirePrefix: appns");
		when(sdkLib2.testType("ut").defaultTestTech()).requestReceivedInDev("i18n/en_GB.js", response);
		then(response).containsText("\"lib.key\": \"translation\"");
	}
	
	@Test @Ignore
	public void i18nPropertiesInLibSrcReferencedFromAotherLibTestXmlFileAreBundled() throws Exception {
		SdkJsLib sdkLib2 = brjs.sdkLib("test");
		given(sdkLib2).containsResourceFileWithContents("config.xml", "<div>@{lib.key@}</div>")
			.and(sdkLib).containsFileWithContents("src/en_GB.properties", "lib.key=translation")
			.and(sdkLib).containsFileWithContents("br-lib.conf", "requirePrefix: appns");
		when(sdkLib2.testType("ut").defaultTestTech()).requestReceivedInDev("i18n/en_GB.js", response);
		then(response).containsText("\"lib.key\": \"translation\"");
	}
	
	@Test @Ignore
	public void i18nPropertiesInLibSrcPackageDirReferencedFromAotherLibTestXmlFileAreBundled() throws Exception {
		SdkJsLib sdkLib2 = brjs.sdkLib("test");
		given(sdkLib2).containsResourceFileWithContents("config.xml", "<div>@{lib.pkg.key@}</div>")
			.and(sdkLib).containsFileWithContents("src/pkg/en_GB.properties", "lib.pkg.key=translation")
			.and(sdkLib).containsFileWithContents("br-lib.conf", "requirePrefix: appns");
		when(sdkLib2.testType("ut").defaultTestTech()).requestReceivedInDev("i18n/en_GB.js", response);
		then(response).containsText("\"lib.pkg.key\": \"translation\"");
	}
	
}
