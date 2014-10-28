package org.bladerunnerjs.model.exception.command;

import org.bladerunnerjs.model.engine.NamedNode;
import org.bladerunnerjs.plugin.CommandPlugin;

/**
 * Class derived from CommandArgumentsException - Exception - Throwable - Object.
 * Thrown when the specified node already exists. 
*/ 


public class NodeAlreadyExistsException extends CommandArgumentsException {
	private static final long serialVersionUID = 1L;
	
	public NodeAlreadyExistsException(NamedNode node, CommandPlugin commandPlugin) {
		super(node.getTypeName() + " '" + node.getName() + "' already exists", commandPlugin);
	}
}