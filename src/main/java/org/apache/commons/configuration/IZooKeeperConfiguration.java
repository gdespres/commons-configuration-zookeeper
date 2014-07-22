/**
 *
 */
package org.apache.commons.configuration;

/**
 *
 */
public interface IZooKeeperConfiguration extends Configuration {

    // ========================================================================
    // CONSTANTS
    // ========================================================================

    public static final int EVENT_RELOAD = 20;

    // ========================================================================
    // PUBLIC METHODS
    // ========================================================================

    void setPath(String path);

    String getPath();

    void load(String path) throws ConfigurationException;
}
