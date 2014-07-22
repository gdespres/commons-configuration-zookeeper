/**
 *
 */
package org.apache.commons.configuration;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.apache.commons.configuration.event.ConfigurationEvent;
import org.apache.commons.configuration.event.ConfigurationListener;
import org.apache.commons.configuration.reloading.ZooKeeperNodeOnChangeReloadingStrategy;
import org.junit.Test;

/**
 *
 */
public class CompositeConfigurationTest extends ZooKeeperConfigurationTest {

    @Test
    public void testCompositeConfiguration() throws Exception {

        final String applicationName = "api-geo-2";
        final String fileName = "application.properties";

        final String zkRootPropertiesPath = "/" + fileName;
        final String zkApplicationPropertiesPath = "/" + applicationName + "/" + fileName;

        // 1) INIT
        createOrUpdatePath(zkRootPropertiesPath, loadFileFromClasspath("_zk_root_application.properties"));
        createOrUpdatePath(zkApplicationPropertiesPath, loadFileFromClasspath("_zk_api-geo-2_application.properties"));

        // 2) TEST

        CompositeConfiguration config = new CompositeConfiguration();

        // ZooKeeper Application Properties
        try {
            ZooKeeperPropertiesConfiguration zkApplicationPropertiesConfig = new ZooKeeperPropertiesConfiguration(client.usingNamespace(applicationName), fileName);
            zkApplicationPropertiesConfig.setReloadingStrategy(new ZooKeeperNodeOnChangeReloadingStrategy());
            zkApplicationPropertiesConfig.addConfigurationListener(new ConfigurationListener() {

                @Override
                public void configurationChanged(final ConfigurationEvent pEvent) {
                    if (!pEvent.isBeforeUpdate() && pEvent.getType() == ZooKeeperNodeConfiguration.EVENT_RELOAD) {
                        System.out.println("Path '" + zkApplicationPropertiesPath + "' has changed !");
                    }
                }
            });
            config.addConfiguration(zkApplicationPropertiesConfig);
        } catch (ConfigurationException e) {
            fail("ZooKeeper Path ");
        }

        // ZooKeeper Root Properties
        try {
            ZooKeeperPropertiesConfiguration zkRootPropertiesConfig = new ZooKeeperPropertiesConfiguration(client, fileName);
            zkRootPropertiesConfig.setReloadingStrategy(new ZooKeeperNodeOnChangeReloadingStrategy());
            zkRootPropertiesConfig.addConfigurationListener(new ConfigurationListener() {

                @Override
                public void configurationChanged(final ConfigurationEvent pEvent) {
                    if (!pEvent.isBeforeUpdate() && pEvent.getType() == ZooKeeperNodeConfiguration.EVENT_RELOAD) {
                        System.out.println("Path '" + zkRootPropertiesPath + "' has changed !");
                    }
                }
            });
            config.addConfiguration(zkRootPropertiesConfig);
        } catch (ConfigurationException e) {

        }

        // Local Properties
        try {
            PropertiesConfiguration propertiesConfig = new PropertiesConfiguration(fileName);
            config.addConfiguration(propertiesConfig);
        } catch (ConfigurationException e) {

        }

        assertThat(config.getString("application.fullname"), equalTo("zk-application-2"));
        assertThat(config.getString("property.only.local"), equalTo("local"));
        assertThat(config.getString("property.only.zk.root"), equalTo("zk-root"));
        assertThat(config.getString("property.only.zk.application"), equalTo("zk-application"));

        // UPDATE ZooKeeper Application Properties
        createOrUpdatePath("/" + applicationName + "/" + fileName, loadFileFromClasspath("_zk_api-geo-2_application-updated.properties"));
        Thread.sleep(500); // Wait for notification

        assertThat(config.getString("application.fullname"), equalTo("zk-application-reloaded-3"));
    }
}
