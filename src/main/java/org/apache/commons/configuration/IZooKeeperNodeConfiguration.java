/**
 *
 */
package org.apache.commons.configuration;

import java.io.InputStream;

import org.apache.curator.framework.recipes.cache.NodeCache;

/**
 *
 */
public interface IZooKeeperNodeConfiguration extends IZooKeeperConfiguration {

    // ========================================================================
    // METHODS
    // ========================================================================

    NodeCache getNode();

    void load(InputStream in) throws ConfigurationException;

    void reload();
}
