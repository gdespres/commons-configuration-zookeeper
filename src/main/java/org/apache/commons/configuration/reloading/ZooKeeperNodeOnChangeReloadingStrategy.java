/**
 *
 */
package org.apache.commons.configuration.reloading;

import org.apache.commons.configuration.IZooKeeperNodeConfiguration;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;

/**
 *
 */
public class ZooKeeperNodeOnChangeReloadingStrategy implements IZooKeeperNodeReloadingStrategy {

    // ========================================================================
    // ATTRIBUTES
    // ========================================================================

    private IZooKeeperNodeConfiguration _configuration;

    private boolean _reloadingRequired;

    // ========================================================================
    // PUBLIC METHODS
    // ========================================================================

    @Override
    public void setConfiguration(final IZooKeeperNodeConfiguration configuration) {

        this._configuration = configuration;
    }

    @Override
    public void init() {

        final NodeCache node = _configuration.getNode();
        if (node != null) {
            node.getListenable().addListener(new NodeCacheListener() {
                @Override
                public void nodeChanged() throws Exception {
                    _reloadingRequired = true;
                }
            });
        }
    }

    @Override
    public boolean reloadingRequired() {
        return _reloadingRequired;
    }

    @Override
    public void reloadingPerformed() {
        _reloadingRequired = false;
    }
}
