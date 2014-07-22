/**
 *
 */
package org.apache.commons.configuration.reloading;

import org.apache.commons.configuration.ZooKeeperNodeConfiguration;

/**
 *
 */
public interface ZooKeeperNodeReloadingStrategy {

    void setConfiguration(final ZooKeeperNodeConfiguration pConfiguration);

    void init();

    boolean reloadingRequired();

    void reloadingPerformed();
}
