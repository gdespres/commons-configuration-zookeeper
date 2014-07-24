/**
 *
 */
package org.apache.commons.configuration.reloading;

import org.apache.commons.configuration.ZKNodeConfiguration;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;

/**
 *
 */
public class ZKNodeChangeEventReloadingStrategy implements ZKNodeReloadingStrategy {

    // ========================================================================
    // ATTRIBUTES
    // ========================================================================

    private ZKNodeConfiguration _configuration;

    // ========================================================================
    // PUBLIC METHODS
    // ========================================================================

    @Override
    public void setConfiguration(final ZKNodeConfiguration configuration) {

        this._configuration = configuration;
    }

    @Override
    public void init() {

        final NodeCache node = _configuration.getNode();
        if (node != null) {
            node.getListenable().addListener(new NodeCacheListener() {
                @Override
                public void nodeChanged() throws Exception {
                    _configuration.reload();
                }
            });
        }
    }
}
