/**
 *
 */
package org.apache.commons.configuration;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.commons.configuration.reloading.ZKNodeReloadingStrategy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.zookeeper.KeeperException.NoNodeException;

/**
 *
 */
public abstract class AbstractZKNodeConfiguration
        extends AbstractZKConfiguration
        implements ZKNodeConfiguration {

    // ========================================================================
    // ATTRIBUTES
    // ========================================================================

    protected NodeCache _node;

    /** Stores the previous existance of node, to detect creation or deletion. */
    private boolean _previousNodeExists;

    // ========================================================================
    // CONSTRUCTORS
    // ========================================================================

    /**
     *
     * @param client
     */
    public AbstractZKNodeConfiguration(final CuratorFramework client) {
        super(client);
    }

    /**
     *
     *
     * @param client
     * @param path
     */
    public AbstractZKNodeConfiguration(final CuratorFramework client, final String path) throws ConfigurationException {
        super(client, path);
    }

    // ========================================================================
    // PUBLIC METHODS
    // ========================================================================

    @Override
    public NodeCache getNode() {

        return this._node;
    }

    public void setReloadingStrategy(final ZKNodeReloadingStrategy strategy) {

        strategy.setConfiguration(this);
        strategy.init();
    }

    /**
     * Load the configuration from the underlying path.
     *
     * @throws ConfigurationException
     *             if loading of the configuration fails.
     */
    @Override
    public void load() throws ConfigurationException {

        if (_node != null) {
            load(_node);
        } else {
            load(getPath());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reload() {
        synchronized (_reloadLock) {
            if (_noReload == 0) {
                try {
                    enterNoReload();

                    if (!_previousNodeExists && isNodeExists()) { // Creation

                        _previousNodeExists = true;
                        fireEvent(EVENT_NODE_CREATE, null, getCompletePath(), true);
                        setDetailEvents(false);
                        try {
                            load();
                        } finally {
                            setDetailEvents(true);
                        }
                        fireEvent(EVENT_NODE_CREATE, null, getCompletePath(), false);

                    } else if (_previousNodeExists && !isNodeExists()) { // Deletion

                        _previousNodeExists = false;
                        fireEvent(EVENT_NODE_DELETE, null, getCompletePath(), true);
                        setDetailEvents(false);
                        try {
                            clear();
                        } finally {
                            setDetailEvents(true);
                        }
                        fireEvent(EVENT_NODE_DELETE, null, getCompletePath(), false);

                    } else { // Update

                        fireEvent(EVENT_NODE_UPDATE, null, getCompletePath(), true);
                        setDetailEvents(false);
                        try {
                            clear();
                            load();
                        } finally {
                            setDetailEvents(true);
                        }
                        fireEvent(EVENT_NODE_UPDATE, null, getCompletePath(), false);
                    }

                } catch (Exception e) {
                    fireError(EVENT_RELOAD, null, null, e);
                } finally {
                    exitNoReload();
                }
            }
        }
    }

    // ========================================================================
    // PROTECTED
    // ========================================================================

    protected void load(final String path) throws ConfigurationException {

        NodeCache node = new NodeCache(this._client, path);
        try {
            node.start(true);
            this._node = node;
            if (node.getCurrentData() != null) {
                load(node);
            }
        } catch (NoNodeException e) {
            throw new ConfigurationException("Unable to find parent node : " + e.getMessage(), e);
        } catch (Exception e) {
            throw new ConfigurationException("Unable to start node cache for path " + path, e);
        }
    }

    protected void load(final NodeCache node) throws ConfigurationException {

        if (node != null && node.getCurrentData() != null) {
            _previousNodeExists = true;
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

    abstract protected void load(InputStream in) throws ConfigurationException;

    // ========================================================================
    // PRIVATE METHODS
    // ========================================================================

    private boolean isNodeExists() {

        return (_node != null && _node.getCurrentData() != null);
    }
}
