
package com.javalobby.tnt.annotation;


import java.util.HashMap;

/**
 * Close a shell
 */
@attribute("")
public class Close {

  
    /**
     * Construct a new <code>Close</code> task.
     */
    public Close() {
    	
    }

    /**
     * Pass a key/value pair from the shell being closed to the listener 
	 	that prompted the shell to be created in the first place.  If 
	 	the listener has an 'exit' tag with a web address associated, 
	 	this key/value pair will be appened to the web address and 
	 	that web address will then be visited.  This may be replaced in 
	 	the future with a nested 'visitWeb' in the calling listener. 
     * @param x
     */
    @attribute(value = "")	
    public void addXArg(String x) {
 
    }
    
    
    public void setSymbolTable(HashMap symbolTable) {

    }
    /**
     * Sets the exit status returned to the shell
     * @param status
     */
    @attribute(value = "Set the name of the shell to close.",
		 	required = true,
		 	defaultValue = "")	
    public void setShell(String strShell) {

    }

    /**
     * execute the close-shell task
     * @throws BuildException
     */
    public void execute() {

    }
}