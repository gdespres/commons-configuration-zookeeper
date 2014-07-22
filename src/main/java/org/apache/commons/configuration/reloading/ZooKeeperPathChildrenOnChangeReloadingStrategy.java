/**
 *
 */
package org.apache.commons.configuration.reloading;

import org.apache.commons.configuration.IZooKeeperPathChildrenConfiguration;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;

/**
 *
 */
public class ZooKeeperPathChildrenOnChangeReloadingStrategy implements IZooKeeperPathChildrenReloadingStrategy {

    // ========================================================================
    // ATTRIBUTES
    // ========================================================================

    private IZooKeeperPathChildrenConfiguration _configuration;

    // ========================================================================
    // PUBLIC METHODS
    // ========================================================================

    @Override
    public void setConfiguration(final IZooKeeperPathChildrenConfiguration configuration) {

        this._configuration = configuration;
    }

    @Override
    public void init() {

        final PathChildrenCache pathChildren = _configuration.getPathChildren();
        if (pathChildren != null) {
            pathChildren.getListenable().addListener(new PathChildrenCacheListener() {

                @Override
                public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {

                    ChildData data = event.getData();
                    String path = null;
                    if (data != null) {
                        path = data.getPath();
                        String key = path.replace(_configuration.getPath() + "/", "");
                        String value = new String(data.getData());

                        switch (event.getType()) {
                        case CHILD_ADDED:
                            _configuration.addProperty(key, value);
                            break;
                        case CHILD_UPDATED:
                            _configuration.setProperty(key, value);
                            break;
                        case CHILD_REMOVED:
                            _configuration.clearProperty(key);
                            break;
                        default:
                            break;
                        }
                    }
                }
            });
        }
    }
}
