/**
 *
 */
package org.apache.commons.configuration.reloading;

import org.apache.commons.configuration.IZooKeeperNodeConfiguration;

/**
 *
 */
public interface IZooKeeperNodeReloadingStrategy extends IZooKeeperReloadingStrategy {

    void setConfiguration(final IZooKeeperNodeConfiguration pConfiguration);
}
