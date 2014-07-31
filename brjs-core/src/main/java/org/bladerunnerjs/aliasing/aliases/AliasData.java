package org.bladerunnerjs.aliasing.aliases;

import java.io.File;
import java.util.List;

import org.bladerunnerjs.aliasing.AliasOverride;
import org.bladerunnerjs.memoization.MemoizedValue;
import org.bladerunnerjs.model.BRJS;
import org.bladerunnerjs.model.exception.ConfigException;
import org.bladerunnerjs.model.exception.request.ContentFileProcessingException;

public class AliasData {
	private final BRJS brjs;
	private final File aliasesFile;
	private final MemoizedValue<AliasesData> aliasesData;
	
	public AliasData(BRJS brjs, File aliasesFile) {
		this.brjs = brjs;
		this.aliasesFile = aliasesFile;
		aliasesData = new MemoizedValue<>("AliasData.aliasesData", brjs, aliasesFile, brjs.file("conf/brjs.conf"));
	}
	
	public List<AliasOverride> getAliasOverrides() throws ContentFileProcessingException {
		return getAliasesData().aliasOverrides;
	}

	public void setAliasOverrides(List<AliasOverride> aliasOverrides) throws ContentFileProcessingException {
		AliasesData aliasesData = getAliasesData();
		aliasesData.aliasOverrides = aliasOverrides;
		write(aliasesData);
	}
	
	public List<String> getGroupNames() throws ContentFileProcessingException {
		return getAliasesData().groupNames;
	}
	
	public void setGroupNames(List<String> groupNames) throws ContentFileProcessingException {
		AliasesData aliasesData = getAliasesData();
		aliasesData.groupNames = groupNames;
		write(aliasesData);
	}
	
	public String getScenario() throws ContentFileProcessingException {
		return getAliasesData().scenario;
	}
	
	public void setScenario(String scenario) throws ContentFileProcessingException {
		AliasesData aliasesData = getAliasesData();
		aliasesData.scenario = scenario;
		write(aliasesData);
	}
	
	private void write(AliasesData aliasesData) throws ContentFileProcessingException {
		try {
			AliasesWriter.write(aliasesData, aliasesFile, getCharacterEncoding());
		}
		catch(Exception e) {
			throw new ContentFileProcessingException(e, aliasesFile);
		}
	}
	
	private AliasesData getAliasesData() throws ContentFileProcessingException {
		return aliasesData.value(() -> {
			return AliasesReader.read(aliasesFile, getCharacterEncoding());
		});
	}
	
	private String getCharacterEncoding() {
		try {
			return brjs.bladerunnerConf().getDefaultFileCharacterEncoding();
		}
		catch (ConfigException e) {
			throw new RuntimeException(e);
		}
	}
}
