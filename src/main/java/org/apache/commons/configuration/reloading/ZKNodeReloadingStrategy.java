/**
 *
 */
package org.apache.commons.configuration.reloading;

import org.apache.commons.configuration.ZKNodeConfiguration;

/**
 *
 */
public interface ZKNodeReloadingStrategy {

    void init();

    void setConfiguration(final ZKNodeConfiguration pConfiguration);
}
