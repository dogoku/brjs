package org.bladerunnerjs.api.spec.utility;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.bladerunnerjs.api.App;
import org.bladerunnerjs.api.BRJS;
import org.bladerunnerjs.api.spec.engine.NodeVerifier;
import org.bladerunnerjs.api.spec.engine.SpecTest;
import org.bladerunnerjs.api.spec.engine.VerifierChainer;
import org.bladerunnerjs.model.engine.Node;

import static org.junit.Assert.*;


public class BRJSVerifier extends NodeVerifier<BRJS> {
	private BRJS brjs;

	public BRJSVerifier(SpecTest modelTest, BRJS brjs) {
		super(modelTest, brjs);
		this.brjs = brjs;
	}

	public VerifierChainer hasApps(String... apps)
	{
		List<String> existingAppNames = new ArrayList<>();
		for (App app : brjs.apps()) {
			existingAppNames.add( app.getName() );
		}
		assertEquals( StringUtils.join(apps, ", "), StringUtils.join(existingAppNames, ", "));
		
		return verifierChainer;
	}

	public VerifierChainer ancestorNodeCanBeFound(File file, Class<? extends Node> nodeClass)
	{
		Node ancestorNodeOfClass = brjs.locateAncestorNodeOfClass(file, nodeClass);
		assertNotNull( ancestorNodeOfClass );
		assertTrue( nodeClass.isAssignableFrom(ancestorNodeOfClass.getClass()) );
		
		return verifierChainer;
	}
	
	public VerifierChainer ancestorNodeCannotBeFound(File file, Class<? extends Node> nodeClass)
	{
		assertNull( brjs.locateAncestorNodeOfClass(file, nodeClass) );
		
		return verifierChainer;
	}

	public VerifierChainer hasDirectoryWithFormat(String base, String dirPattern, StringBuffer filePath)
	{
		File baseDir = brjs.file(base);
		File[] children = baseDir.listFiles();
		if (children == null || children.length == 0) {
			throw new RuntimeException(baseDir + " doesn't contain any files or doesnt exist");
		}
		for (File childDir : children) {
			if (childDir.getName().matches(dirPattern)) {
				filePath.append(childDir.getAbsolutePath());
				return verifierChainer;
			}
		}
		throw new RuntimeException("Unable to find a directory that matches " + dirPattern);
	}
}
