/**
 *
 */
package org.apache.commons.configuration;

import org.apache.commons.configuration.reloading.ZKPathChildrenReloadingStrategy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCache.StartMode;

/**
 *
 */
public class ZKPathChildrenBaseConfiguration
        extends AbstractZKConfiguration
        implements ZKPathChildrenConfiguration {

    // ========================================================================
    // ATTRIBUTES
    // ========================================================================

    protected PathChildrenCache _pathChildren;

    // ========================================================================
    // CONSTRUCTORS
    // ========================================================================

    /**
     * @param pClient
     */
    public ZKPathChildrenBaseConfiguration(final CuratorFramework pClient) {
        super(pClient);
    }

    /**
     *
     */
    public ZKPathChildrenBaseConfiguration(final CuratorFramework pClient, final String pPath) throws ConfigurationException {
        super(pClient, pPath);
    }

    // ========================================================================
    // PUBLIC METHODS
    // ========================================================================

    public void setReloadingStrategy(final ZKPathChildrenReloadingStrategy strategy) {

        strategy.setConfiguration(this);
        strategy.init();
    }

    @Override
    public PathChildrenCache getPathChildren() {

        return this._pathChildren;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void load() throws ConfigurationException {

        if (_pathChildren != null) {
            load(_pathChildren);
        }
        else {
            load(getPath());
        }
    }

    protected void load(final String path) throws ConfigurationException {

        PathChildrenCache pathChildren = new PathChildrenCache(_client, path, true);
        try {
            pathChildren.start(StartMode.BUILD_INITIAL_CACHE);
            this._pathChildren = pathChildren;
            if (pathChildren.getCurrentData() != null) {
                load(pathChildren);
            }
        } catch (Exception e) {
            throw new ConfigurationException("Unable to start path children cache for path " + path, e);
        }
    }

    protected void load(final PathChildrenCache pathChildren) throws ConfigurationException {

        if (pathChildren != null && pathChildren.getCurrentData() != null) {
            for (ChildData child : pathChildren.getCurrentData()) {
                String path = child.getPath();
                String key = path.replace(getPath() + "/", "");
                String value = new String(child.getData());
                addProperty(key, value);
            }
        }
        else {
            throw new ConfigurationException("Unable to load path children : null or empty.");
        }
    }

    // ========================================================================
    // PROTECTED METHODS
    // ========================================================================

}
