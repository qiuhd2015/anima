package org.hdl.anima.common.module;

import org.hdl.anima.Application;

/**
 * A default Module implementation that basically avoids subclasses having to implement the whole
 * Module interface.
 *
 * @author qiuhd
 */
public class BasicModule implements Module {

    /**
     * The name of the module
     */
    private String name;
    /**
     * The application of the module
     */
    protected Application application;
    
    /**
     * Create a basic module with the given name.
     *
     * @param moduleName The name for the module or null to use the default
     */
    public BasicModule(String moduleName) {
        if (moduleName == null) {
            this.name = "No name assigned";
        }
        else {
            this.name = moduleName;
        }
    }

    /**
     * Obtain the name of the module.
     *
     * @return The name of the module
     */
    public String getName() {
        return name;
    }

    /**
     * Initializes the basic module.
     * @param application 
     */
    public void initialize(Application application) {
    	this.application = application;
    }

    /**
     * Starts the basic module.
     * 
     * @throws IllegalStateException 
     */
    public void start() throws IllegalStateException {
    }

    /**
     * Stops the basic module.
     */
    public void stop() {
    }

    /**
     * Destroys the module.
     * 
     */
    public void destroy() {
    }
}