/**
 *
 */
package org.apache.commons.configuration;

import org.apache.commons.logging.Log;

/**
 *
 */
public interface ZKConfiguration extends Configuration {

    // ========================================================================
    // CONSTANTS
    // ========================================================================

    //public static final int EVENT_RELOAD = 20;

    // ========================================================================
    // PUBLIC METHODS
    // ========================================================================

    void setPath(String path);

    String getPath();

    void load() throws ConfigurationException;

    //void load(String path) throws ConfigurationException;

    Log getLogger();
}
