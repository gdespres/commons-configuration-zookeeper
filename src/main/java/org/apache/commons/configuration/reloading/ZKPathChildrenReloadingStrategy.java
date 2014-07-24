/**
 *
 */
package org.apache.commons.configuration.reloading;

import org.apache.commons.configuration.ZKPathChildrenConfiguration;

/**
 *
 */
public interface ZKPathChildrenReloadingStrategy {

    void init();

    void setConfiguration(final ZKPathChildrenConfiguration pConfiguration);
}
