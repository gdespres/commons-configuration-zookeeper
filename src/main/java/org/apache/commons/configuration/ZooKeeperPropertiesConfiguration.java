package org.apache.commons.configuration;
/**
 *
 */


import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.NodeCache;

import com.google.common.io.Closeables;

/**
 *
 */
public class ZooKeeperPropertiesConfiguration
        extends PropertiesConfiguration {

    // ========================================================================
    // ATTRIBUTES
    // ========================================================================

    private final CuratorFramework rootClient;

    private final CuratorFramework namespacedClient;

    private NodeCache nodeCache;

    private List<NodeCache> includedNodes;

    // ========================================================================
    // CONSTRUCTOR
    // ========================================================================

    /**
     * Constructor with zookeeper client.
     *
     * @param client
     *          ZooKeeper Client.
     */
    public ZooKeeperPropertiesConfiguration(final CuratorFramework client) {

        this(client, null);
    }

    /**
     * Constructor with zookeeper client and namespace.
     *
     * @param client
     *          ZooKeeper Client.
     * @param namespace
     *          Namespace used by ZooKeeper client.
     *
     */
    public ZooKeeperPropertiesConfiguration(final CuratorFramework client, final String namespace) {

        this.rootClient = client;
        if (StringUtils.isBlank(namespace)) {
            this.namespacedClient = null;
        }
        else {
            this.namespacedClient = client.usingNamespace(namespace);
        }
    }

    // ========================================================================
    // PUBLIC METHOD
    // ========================================================================

    /**
     * {@inheritDoc}
     */
    public void setNodePath(final String path) throws ConfigurationException {

        NodeCache node = getNodeCache(path);
        if (node != null) {
            nodeCache = node;
        } else {
            setFileName(path);
        }
    }

    /**
     * {@inheritDoc}
     */
    public NodeCache getNodeCache() {
        return nodeCache;
    }

    /**
     * {@inheritDoc}
     */
    public List<NodeCache> getIncludedNodes() {
        return includedNodes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void load() throws ConfigurationException {

        if (nodeCache != null) {
            load(nodeCache);
        } else {
            super.load();
        }
    }

    public void load(final NodeCache pNodeCache) throws ConfigurationException {

        InputStream in = null;
        Reader reader = null;
        try {
            byte[] data = pNodeCache.getCurrentData().getData();
            in = new ByteArrayInputStream(data);
            reader = new InputStreamReader(in);
            load(reader);
        } catch (ConfigurationException e) {
            throw e;
        } catch (Exception e) {
            throw new ConfigurationException("Unable to load the configuration node " + pNodeCache.getCurrentData().getPath(), e);
        } finally {
            try {
                Closeables.close(in, true);
                Closeables.close(reader, true);
            } catch (Exception e) {
                // NO-OP
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    boolean propertyLoaded(final String key, final String value) throws ConfigurationException {

        boolean result;

        if (StringUtils.isNotEmpty(getInclude()) && key.equalsIgnoreCase(getInclude())) {
            if (isIncludesAllowed()) {
                String[] files;
                if (!isDelimiterParsingDisabled()) {
                    files = StringUtils.split(value, getListDelimiter());
                }
                else {
                    files = new String[] { value };
                }
                for (String f : files) {
                    loadIncludeFile(interpolate(f.trim()));
                }
            }
            result = false;
        }
        else {
            addProperty(key, value);
            result = true;
        }

        return result;
    }

    // ========================================================================
    // PRIVATE METHOD
    // ========================================================================

    /**
     * Helper method for loading an included properties file. This method is
     * called by {@code load()} when an {@code include} property
     * is encountered. It tries to resolve relative file names based on the
     * current base path. If this fails, a resolution based on the location of
     * this properties file is tried.
     *
     * @param fileName the name of the file to load
     * @throws ConfigurationException if loading fails
     */
    private void loadIncludeFile(final String fileName) throws ConfigurationException {

        NodeCache includedNode = getNodeCache(fileName);
        if (includedNode != null) {

            if (includedNodes == null) {
                includedNodes = new ArrayList<NodeCache>();
            }
            includedNodes.add(includedNode);
            load(includedNode);
        }
        else {
            load(fileName);
        }
    }

    private boolean isZooKeeperNodeExists(final CuratorFramework client, final String path) {

        boolean pathExists = false;
        try {
            pathExists = (client.checkExists().forPath(path) != null);
        } catch (Exception e) {
            // no-op
        }
        return pathExists;
    }

    private NodeCache getNodeCache(final String path) throws ConfigurationException {

        NodeCache nodeCache = null;
        if (namespacedClient != null && isZooKeeperNodeExists(namespacedClient, path)) {
            nodeCache = new NodeCache(namespacedClient, path);
        }
        else if (isZooKeeperNodeExists(rootClient, path)) {
            nodeCache = new NodeCache(rootClient, path);
        }

        if (nodeCache != null) {
            try {
                nodeCache.start(true);
            } catch (Exception e) {
                throw new ConfigurationException("Unable to start the configuration node cache for " + fileName);
            }

        }

        return nodeCache;
    }
}
