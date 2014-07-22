/**
 *
 */
package org.apache.commons.configuration;

import org.apache.curator.framework.CuratorFramework;

/**
 *
 */
public class ZooKeeperPathChildrenConfiguration
        extends AbstractZooKeeperConfiguration
        implements IZooKeeperPathChildrenConfiguration {

    // ========================================================================
    // CONSTRUCTORS
    // ========================================================================

    /**
     * @param pClient
     */
    public ZooKeeperPathChildrenConfiguration(final CuratorFramework pClient) {
        super(pClient);
    }

    /**
     *
     */
    public ZooKeeperPathChildrenConfiguration(final CuratorFramework pClient, final String pPath) throws ConfigurationException {
        super(pClient, pPath);
    }

    // ========================================================================
    // PUBLIC METHODS
    // ========================================================================

    /**
     * {@inheritDoc}
     */
    @Override
    public void load(final String pPath) throws ConfigurationException {
        // TODO Auto-generated method stub

    }

    // ========================================================================
    // PROTECTED METHODS
    // ========================================================================

}
