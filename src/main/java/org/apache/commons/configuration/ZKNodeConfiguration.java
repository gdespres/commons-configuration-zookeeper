/**
 *
 */
package org.apache.commons.configuration;

import org.apache.curator.framework.recipes.cache.NodeCache;

/**
 *
 */
public interface ZKNodeConfiguration extends ZKConfiguration {

    // ========================================================================
    // CONSTANTS
    // ========================================================================

    public static final int EVENT_RELOAD = 40;

    public static final int EVENT_NODE_CREATE = 41;

    public static final int EVENT_NODE_UPDATE = 42;

    public static final int EVENT_NODE_DELETE = 43;

    // ========================================================================
    // METHODS
    // ========================================================================

    NodeCache getNode();

    void reload();
}
