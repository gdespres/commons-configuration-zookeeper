/**
 *
 */
package org.apache.commons.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.curator.framework.CuratorFramework;

/**
 *
 */
public class ZooKeeperXMLPropertiesConfiguration extends ZooKeeperPropertiesConfiguration {

    // ========================================================================
    // CONSTRUCTORS
    // ========================================================================

    /**
     * @throws ConfigurationException
     */
    public ZooKeeperXMLPropertiesConfiguration(final CuratorFramework client) throws ConfigurationException {
        super(client);
    }

    /**
     * @throws ConfigurationException
     *
     */
    public ZooKeeperXMLPropertiesConfiguration(final CuratorFramework client, final String path) throws ConfigurationException {
        super(client, path);
    }

    // ========================================================================
    // PUBLIC METHODS
    // ========================================================================

    /**
     * {@inheritDoc}
     */
    @Override
    public void load(final InputStream in) throws ConfigurationException {

        Properties properties = new Properties();
        try {
            properties.loadFromXML(in);
            for (String key : properties.stringPropertyNames()) {
                String value = properties.getProperty(key);
                addProperty(key, value);
            }
        } catch (IOException e) {
            throw new ConfigurationException("Unable to load properties for path " + getPath(), e);
        }
    }

    // ========================================================================
    // PROTECTED METHODS
    // ========================================================================
}
