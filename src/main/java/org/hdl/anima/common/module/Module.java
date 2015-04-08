package org.hdl.anima.common.module;

import org.hdl.anima.Application;

/**
 
 * A simple lifecycle
 * @author qiuhd
 */
public interface Module {

    /**
     * Returns the name of the module for display in administration interfaces.
     *
     * @return The name of the module.
     */
    String getName();

    /**
     * Initialize the module with the container.
     *
     * @param server the server hosting this module.
     */
    void initialize(Application app);

    /**
     * Start the module (must return quickly). 
     */
    void start();

    /**
     * Stop the module.
     */
    void stop();
    /**
     * Module should free all resources and prepare for deallocation.
     */
    void destroy();
}
