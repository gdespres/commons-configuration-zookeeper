/**
 *
 */
package org.apache.commons.configuration.reloading;


/**
 *
 */
public class ZooKeeperInvariantReloadingStrategy implements IZooKeeperReloadingStrategy {

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
