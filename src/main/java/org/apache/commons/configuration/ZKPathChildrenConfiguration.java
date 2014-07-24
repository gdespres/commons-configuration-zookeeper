/**
 *
 */
package org.apache.commons.configuration;

import org.apache.curator.framework.recipes.cache.PathChildrenCache;

/**
 *
 */
public interface ZKPathChildrenConfiguration extends ZKConfiguration {

    PathChildrenCache getPathChildren();
}
