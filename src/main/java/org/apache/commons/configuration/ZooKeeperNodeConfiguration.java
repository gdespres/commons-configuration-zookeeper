/**
 *
 */
package org.apache.commons.configuration;

import java.io.InputStream;

import org.apache.curator.framework.recipes.cache.NodeCache;

/**
 *
 */
public interface ZooKeeperNodeConfiguration extends Configuration {

    // ========================================================================
    // CONSTANTS
    // ========================================================================

    public static final int EVENT_RELOAD = 20;

    // ========================================================================
    // METHODS
    // ========================================================================

    NodeCache getNode();

    void load(InputStream in) throws ConfigurationException;
}
