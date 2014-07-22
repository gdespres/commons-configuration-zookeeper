/**
 *
 */
package org.apache.commons.configuration.reloading;

import org.apache.commons.configuration.ZooKeeperNodeConfiguration;

/**
 *
 */
public class ZooKeeperNodeInvariantReloadingStrategy implements ZooKeeperNodeReloadingStrategy {

    /**
     * {@inheritDoc}
     */
    @Override
    public void setConfiguration(final ZooKeeperNodeConfiguration pConfiguration) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean reloadingRequired() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reloadingPerformed() {
    }

}
