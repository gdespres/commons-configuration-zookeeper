/**
 *
 */
package org.apache.commons.configuration;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.configuration.reloading.ZooKeeperNodeInvariantReloadingStrategy;
import org.apache.commons.configuration.reloading.ZooKeeperNodeReloadingStrategy;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.LogFactory;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.NodeCache;

/**
 *
 */
public abstract class AbstractZooKeeperNodeConfiguration
        extends BaseConfiguration
        implements ZooKeeperNodeConfiguration {

    // ========================================================================
    // CONSTANTS
    // ========================================================================

    public static final int EVENT_RELOAD = 20;

    // ========================================================================
    // ATTRIBUTES
    // ========================================================================

    /** Stores the path */
    protected String _path;

    protected NodeCache _node;

    /** Stores the ZooKeeper client **/
    protected final CuratorFramework _client;

    /** Holds a reference to the reloading strategy */
    protected ZooKeeperNodeReloadingStrategy _strategy;

    /** A lock object for protecting reload operations */
    protected Object _reloadLock = new Lock("AbstractZooKeeperNodeConfiguration");

    /** Counter that prohibits reloading */
    private int _noReload;

    // ========================================================================
    // CONSTRUCTORS
    // ========================================================================

    /**
     *
     * @param client
     */
    public AbstractZooKeeperNodeConfiguration(final CuratorFramework client) {

        initReloadingStrategy();
        setLogger(LogFactory.getLog(getClass()));
        addErrorLogListener();

        this._client = client;
    }

    /**
     *
     *
     * @param client
     * @param path
     */
    public AbstractZooKeeperNodeConfiguration(final CuratorFramework client, final String path) throws ConfigurationException {

        this(client);

        // store the path
        setPath(path);

        // load the node
        load();
    }

    // ========================================================================
    // PUBLIC METHODS
    // ========================================================================

    public ZooKeeperNodeReloadingStrategy getReloadingStrategy() {

        return this._strategy;
    }

    public void setReloadingStrategy(final ZooKeeperNodeReloadingStrategy strategy) {

        this._strategy = strategy;
        strategy.setConfiguration(this);
        strategy.init();
    }

    public void setPath(final String path) {

        this._path = StringUtils.startsWith(path, "/") ? path : "/" + path;
        getLogger().debug("Path set to " + path);
    }

    public String getPath() {

        return this._path;
    }

    @Override
    public NodeCache getNode() {

        return this._node;
    }

    /**
     * Load the configuration from the underlying path.
     *
     * @throws ConfigurationException if loading of the configuration fails.
     */
    public void load() throws ConfigurationException {

        load(getPath());
    }

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

    /**
     * {@inheritDoc}
     */
    @Override
    public void addProperty(final String key, final Object value) {
        synchronized (_reloadLock) {
            super.addProperty(key, value);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setProperty(final String pKey, final Object pValue) {
        synchronized (_reloadLock) {
            super.setProperty(pKey, pValue);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearProperty(final String pKey) {
        synchronized (_reloadLock) {
            super.clearProperty(pKey);
        }
    }

    public void reload() {
        synchronized (_reloadLock) {
            if (_noReload == 0) {
                try {
                    enterNoReload();
                    if (_strategy.reloadingRequired()) {
                        if (getLogger().isInfoEnabled()) {
                            getLogger().info("Reloading configuration for path " + getPath());
                        }
                        refresh();

                        // notify the strategy
                        _strategy.reloadingPerformed();
                    }
                } catch (Exception e) {
                    fireError(EVENT_RELOAD, null, null, e);
                } finally {
                    exitNoReload();
                }
            }
        }
    }

    public void refresh() throws ConfigurationException {

        fireEvent(EVENT_RELOAD, null, getPath(), true);
        setDetailEvents(false);
        try {
            clear();
            load();
        } finally {
            setDetailEvents(true);
        }
        fireEvent(EVENT_RELOAD, null, getPath(), false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getProperty(final String pKey) {

        synchronized (_reloadLock) {
            reload();
            return super.getProperty(pKey);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        synchronized (_reloadLock) {
            reload();
            return super.isEmpty();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsKey(final String pKey) {
        synchronized (_reloadLock) {
            reload();
            return super.containsKey(pKey);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<String> getKeys() {

        reload();
        List<String> keyList = new LinkedList<>();
        enterNoReload();
        try {
            for (Iterator<String> it = super.getKeys(); it.hasNext();) {
                keyList.add(it.next());
            }
            return keyList.iterator();
        } finally {
            exitNoReload();
        }
    }

    // ========================================================================
    // PROTECTED METHODS
    // ========================================================================

    protected void enterNoReload() {
        synchronized (_reloadLock) {
            _noReload++;
        }
    }

    protected void exitNoReload() {
        synchronized (_reloadLock) {
            if (_noReload > 0) {
                _noReload--;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void fireEvent(final int pType, final String pPropName, final Object pPropValue, final boolean pBefore) {

        enterNoReload();
        try {
            super.fireEvent(pType, pPropName, pPropValue, pBefore);
        } finally {
            exitNoReload();
        }
    }

    // ========================================================================
    // PRIVATE METHODS
    // ========================================================================

    /**
     * Helper method for initializing the reloading strategy.
     */
    private void initReloadingStrategy() {

        setReloadingStrategy(new ZooKeeperNodeInvariantReloadingStrategy());
    }

    /**
     *
     * @param closeable
     */
    private void closeSilent(final Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException e) {
            getLogger().warn(e.getMessage(), e);
        }
    }
}
