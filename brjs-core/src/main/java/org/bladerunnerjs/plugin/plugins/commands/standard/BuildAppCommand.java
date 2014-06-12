package org.bladerunnerjs.plugin.plugins.commands.standard;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.bladerunnerjs.console.ConsoleWriter;
import org.bladerunnerjs.model.App;
import org.bladerunnerjs.model.BRJS;
import org.bladerunnerjs.model.exception.ModelOperationException;
import org.bladerunnerjs.model.exception.command.CommandArgumentsException;
import org.bladerunnerjs.model.exception.command.CommandOperationException;
import org.bladerunnerjs.model.exception.command.DirectoryAlreadyExistsCommandException;
import org.bladerunnerjs.model.exception.command.DirectoryDoesNotExistCommandException;
import org.bladerunnerjs.model.exception.command.NodeDoesNotExistException;
import org.bladerunnerjs.plugin.utility.command.ArgsParsingCommandPlugin;

import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Switch;
import com.martiansoftware.jsap.UnflaggedOption;

public class BuildAppCommand extends ArgsParsingCommandPlugin {

	public class Messages {
		public static final String APP_BUILT_CONSOLE_MSG = "Built app '%s' available at '%s'";
	}
	
	private BRJS brjs;
	private ConsoleWriter out;
	
	private class Parameters {
		public static final String APP_NAME = "app-name";
		public static final String TARGET_DIR = "target-dir";
	}
	
	@Override
	protected void configureArgsParser(JSAP argsParser) throws JSAPException {
		argsParser.registerParameter(new UnflaggedOption(Parameters.APP_NAME).setRequired(true).setHelp("the application within which the new blade will be created"));
		argsParser.registerParameter(new UnflaggedOption(Parameters.TARGET_DIR).setHelp("the directory within which the exported app will be built"));
		argsParser.registerParameter(new Switch("war").setShortFlag('w').setLongFlag("war").setDefault("false").setHelp("whether the exported files should be placed into a war zip."));
	}
	
	@Override
	public void setBRJS(BRJS brjs) {
		this.brjs = brjs;
		out = brjs.getConsoleWriter();
	}
	
	@Override
	public String getCommandName() {
		return "build-app";
	}
	
	@Override
	public String getCommandDescription() {
		return "Build an application so that it can be deployed on web server.";
	}
	
	@Override
	protected int doCommand(JSAPResult parsedArgs) throws CommandArgumentsException, CommandOperationException {
		
		String appName = parsedArgs.getString(Parameters.APP_NAME);
		String targetDirPath = parsedArgs.getString(Parameters.TARGET_DIR);
		boolean warExport = parsedArgs.getBoolean("war");
		
		App app = brjs.app(appName);
		File targetDir;
		if (targetDirPath == null) 
		{
			targetDir = brjs.storageDir("built-apps");
			File appExportDir = new File(targetDir, appName);
			File warExportFile = new File(targetDir, appName+".war");
			if (warExport) {
				FileUtils.deleteQuietly(warExportFile);
			} else {
				FileUtils.deleteQuietly(appExportDir);			
			}
			targetDir.mkdirs();
		} 
		else {
			targetDir = new File(targetDirPath);
			if (!targetDir.isDirectory()) 
			{
				targetDir = brjs.file("sdk/" + targetDirPath);
			}
		}
		
		File appExportDir = new File(targetDir, appName);
		File warExportFile = new File(targetDir, appName+".war");
		
		if(!app.dirExists()) throw new NodeDoesNotExistException(app, this);
		if(!targetDir.isDirectory()) throw new DirectoryDoesNotExistCommandException(targetDirPath, this);
		
		if (warExport) {
			if(warExportFile.exists()) throw new DirectoryAlreadyExistsCommandException(appExportDir.getPath(), this);
		} else {
			if(appExportDir.exists()) throw new DirectoryAlreadyExistsCommandException(appExportDir.getPath(), this);			
		}
		
		try {
			app.build(targetDir, warExport);
			
			if (warExport) {
				out.println(Messages.APP_BUILT_CONSOLE_MSG, appName, warExportFile.getCanonicalPath());
			} else {
				out.println(Messages.APP_BUILT_CONSOLE_MSG, appName, appExportDir.getCanonicalPath());			
			}
		}
		catch (ModelOperationException | IOException e) {
			throw new CommandOperationException(e);
		}
		
		return 0;
	}
}
