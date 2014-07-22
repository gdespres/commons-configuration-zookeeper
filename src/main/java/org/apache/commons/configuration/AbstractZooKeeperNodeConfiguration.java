/**
 *
 */
package org.apache.commons.configuration;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.commons.configuration.reloading.IZooKeeperNodeReloadingStrategy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.NodeCache;

/**
 *
 */
public abstract class AbstractZooKeeperNodeConfiguration
        extends AbstractZooKeeperConfiguration
        implements IZooKeeperNodeConfiguration {

    // ========================================================================
    // ATTRIBUTES
    // ========================================================================

    protected NodeCache _node;

    // ========================================================================
    // CONSTRUCTORS
    // ========================================================================

    /**
     *
     * @param client
     */
    public AbstractZooKeeperNodeConfiguration(final CuratorFramework client) {
        super(client);
    }

    /**
     *
     *
     * @param client
     * @param path
     */
    public AbstractZooKeeperNodeConfiguration(final CuratorFramework client, final String path) throws ConfigurationException {
        super(client, path);
    }

    // ========================================================================
    // PUBLIC METHODS
    // ========================================================================

    public IZooKeeperNodeReloadingStrategy getReloadingStrategy() {

        return (IZooKeeperNodeReloadingStrategy) this._strategy;
    }

    public void setReloadingStrategy(final IZooKeeperNodeReloadingStrategy strategy) {

        this._strategy = strategy;
        strategy.setConfiguration(this);
        strategy.init();
    }

    @Override
    public NodeCache getNode() {

        return this._node;
    }

    @Override
    public void load(final String path) throws ConfigurationException {

        NodeCache node = new NodeCache(this._client, path);
        try {
            node.start(true);
            if (node.getCurrentData() != null) {

                this._node = node;
                load(node);
            }
            else {
                throw new ConfigurationException("Unable to load the configuration path " + path);
            }
        } catch (Exception e) {
            throw new ConfigurationException("Unable to start node cache for path " + path, e);
        }
    }

    public void load(final NodeCache node) throws ConfigurationException {

        if (node != null && node.getCurrentData() != null) {
            InputStream in = null;
            try {
                byte[] data = node.getCurrentData().getData();
                in = new ByteArrayInputStream(data);
                load(in);
            } catch (ConfigurationException e) {
                throw e;
            } catch (Exception e) {
                throw new ConfigurationException("Unable to load configuration path " + node.getCurrentData().getPath(), e);
            } finally {
                closeSilent(in);
            }
        }
        else {
            throw new ConfigurationException("Unable to load node : null or empty.");
        }
    }

    // ========================================================================
    // PRIVATE METHODS
    // ========================================================================
}
