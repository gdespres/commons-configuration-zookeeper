package org.apache.commons.configuration.reloading;
/**
 *
 */


import java.util.List;

import org.apache.commons.configuration.FileConfiguration;
import org.apache.commons.configuration.ZooKeeperPropertiesConfiguration;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;

/**
 *
 */
public class ZooKeeperNodeReloadingStategy implements ReloadingStrategy {

    private ZooKeeperPropertiesConfiguration configuration;

    private boolean reloadingRequired;

    /**
     * {@inheritDoc}
     */
    public void setConfiguration(final FileConfiguration pConfiguration) {

        if (ZooKeeperPropertiesConfiguration.class.isAssignableFrom(pConfiguration.getClass())) {

            configuration = (ZooKeeperPropertiesConfiguration) pConfiguration;
        } else {

            throw new IllegalStateException(getClass().getName() + " class can only be used with " + ZooKeeperPropertiesConfiguration.class.getName() + " configuration class.");
        }
    }

    /**
     * {@inheritDoc}
     */
    public void init() {

        final NodeCache nodeCache = configuration.getNodeCache();
        nodeCache.getListenable().addListener(new NodeCacheListener() {
            public void nodeChanged() throws Exception {
                reloadingRequired = true;
            }
        });

        List<NodeCache> includedNodes = configuration.getIncludedNodes();
        if (includedNodes != null) {
            for (final NodeCache includedNode : includedNodes) {
                includedNode.getListenable().addListener(new NodeCacheListener() {
                    public void nodeChanged() throws Exception {
                        reloadingRequired = true;
                    }
                });
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean reloadingRequired() {
        return reloadingRequired;
    }

    /**
     * {@inheritDoc}
     */
    public void reloadingPerformed() {
        reloadingRequired = false;
    }
}
