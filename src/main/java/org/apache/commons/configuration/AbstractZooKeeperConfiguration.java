/**
 *
 */
package org.apache.commons.configuration;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.configuration.reloading.IZooKeeperReloadingStrategy;
import org.apache.commons.configuration.reloading.ZooKeeperInvariantReloadingStrategy;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.LogFactory;
import org.apache.curator.framework.CuratorFramework;

/**
 *
 */
public abstract class AbstractZooKeeperConfiguration
        extends BaseConfiguration
        implements IZooKeeperConfiguration {

    // ========================================================================
    // ATTRIBUTES
    // ========================================================================

    /** Stores the path */
    protected String _path;

    /** Stores the ZooKeeper client **/
    protected final CuratorFramework _client;

    /** A lock object for protecting reload operations */
    protected Object _reloadLock = new Lock("AbstractZooKeeperConfiguration");

    /** Counter that prohibits reloading */
    private int _noReload;

    /** Holds a reference to the reloading strategy */
    protected IZooKeeperReloadingStrategy _strategy;

    // ========================================================================
    // CONSTRUCTORS
    // ========================================================================

    /**
     *
     */
    public AbstractZooKeeperConfiguration(final CuratorFramework client) {

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
    public AbstractZooKeeperConfiguration(final CuratorFramework client, final String path) throws ConfigurationException {

        this(client);

        // store the path
        setPath(path);

        // load the node
        load();
    }

    // ========================================================================
    // PUBLIC METHODS
    // ========================================================================

    public IZooKeeperReloadingStrategy getReloadingStrategy() {

        return this._strategy;
    }

    @Override
    public void setPath(final String path) {

        this._path = StringUtils.startsWith(path, "/") ? path : "/" + path;
        getLogger().debug("Path set to " + path);
    }

    @Override
    public String getPath() {

        return this._path;
    }

    /**
     * Load the configuration from the underlying path.
     * 
     * @throws ConfigurationException
     *             if loading of the configuration fails.
     */
    public void load() throws ConfigurationException {

        load(getPath());
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
                    // if (_strategy.reloadingRequired()) {
                    // if (getLogger().isInfoEnabled()) {
                    // getLogger().info("Reloading configuration for path " +
                    // getPath());
                    // }
                    refresh();
                    //
                    // // notify the strategy
                    // _strategy.reloadingPerformed();
                    // }
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
            // reload();
            return super.getProperty(pKey);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        synchronized (_reloadLock) {
            // reload();
            return super.isEmpty();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsKey(final String pKey) {
        synchronized (_reloadLock) {
            // reload();
            return super.containsKey(pKey);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<String> getKeys() {

        // reload();
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

    /**
     * 
     * @param closeable
     */
    protected final void closeSilent(final Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException e) {
            getLogger().warn(e.getMessage(), e);
        }
    }

    // ========================================================================
    // PRIVATE METHODS
    // ========================================================================

    /**
     * Helper method for initializing the reloading strategy.
     */
    private void initReloadingStrategy() {

        this._strategy = new ZooKeeperInvariantReloadingStrategy();
    }
}
