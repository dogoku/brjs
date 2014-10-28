package org.bladerunnerjs.model.exception.command;

import org.bladerunnerjs.plugin.CommandPlugin;

/**
 * Class derived from CommandArgumentsException - Exception - Throwable - Object.
 * Thrown when the inputted number of arguments does not match the required number of arguments. 
*/ 

public class IncorrectNumberOfArgumentsException extends CommandArgumentsException {
	private static final long serialVersionUID = 1L;
	
	public IncorrectNumberOfArgumentsException(CommandPlugin commandPlugin) {
		super("Incorrect number of arguments", commandPlugin);
	}
}