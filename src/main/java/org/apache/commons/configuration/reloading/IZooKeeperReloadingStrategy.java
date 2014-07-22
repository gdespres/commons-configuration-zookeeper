/**
 *
 */
package org.apache.commons.configuration.reloading;

/**
 *
 */
public interface IZooKeeperReloadingStrategy {

    void init();

    boolean reloadingRequired();

    void reloadingPerformed();
}
