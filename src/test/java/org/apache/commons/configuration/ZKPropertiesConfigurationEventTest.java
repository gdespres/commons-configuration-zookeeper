/**
 *
 */
package org.apache.commons.configuration;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.apache.commons.configuration.event.ConfigurationEvent;
import org.apache.commons.configuration.event.ConfigurationListener;
import org.apache.commons.configuration.reloading.ZKNodeChangeEventReloadingStrategy;
import org.apache.curator.framework.CuratorFramework;
import org.junit.Test;

/**
 *
 */
public class ZKPropertiesConfigurationEventTest extends ZKConfigurationTest {

    @Test
    public void testPropertiesCreatedEvent() throws Exception {

        String path = "/prop1.properties";

        // Init ZooKeeper
        // no-op

        //
        ZKPropertiesConfiguration config = new ZKPropertiesConfiguration(client);
        config.setPath(path);
        config.load();
        config.setReloadingStrategy(new ZKNodeChangeEventReloadingStrategy());
        TestConfigurationListener testConfigListener = new TestConfigurationListener();
        config.addConfigurationListener(testConfigListener);

        // Create Node
        client.create().creatingParentsIfNeeded().forPath(path);
        Thread.sleep(WAIT_NOTIFICATION_MILLIS); // Wait notification

        // Test
        assertThat(testConfigListener.getEventType(), equalTo(ZKPropertiesConfiguration.EVENT_NODE_CREATE));
        assertThat(testConfigListener.getPath(), equalTo(path));
    }

    @Test
    public void testPropertiesChangedEvent() throws Exception {

        String path = "/prop2.properties";

        // Init ZooKeeper
        client.create().creatingParentsIfNeeded().forPath(path, "value".getBytes());

        //
        ZKPropertiesConfiguration config = new ZKPropertiesConfiguration(client);
        config.setPath(path);
        config.load();
        config.setReloadingStrategy(new ZKNodeChangeEventReloadingStrategy());
        TestConfigurationListener testConfigListener = new TestConfigurationListener();
        config.addConfigurationListener(testConfigListener);

        // Update Node
        client.setData().forPath(path, "new value".getBytes());
        Thread.sleep(WAIT_NOTIFICATION_MILLIS); // Wait notification

        // Test
        assertThat(testConfigListener.getEventType(), equalTo(ZKPropertiesConfiguration.EVENT_NODE_UPDATE));
        assertThat(testConfigListener.getPath(), equalTo(path));
    }

    @Test
    public void testPropertiesDeletedEvent() throws Exception {

        String path = "/prop3.properties";
        String namespace = "app";

        // Init ZooKeeper
        CuratorFramework clientWithNamespace = client.usingNamespace(namespace);
        clientWithNamespace.create().creatingParentsIfNeeded().forPath(path, "value".getBytes());

        //
        ZKPropertiesConfiguration config = new ZKPropertiesConfiguration(clientWithNamespace);
        config.setPath(path);
        config.load();
        config.setReloadingStrategy(new ZKNodeChangeEventReloadingStrategy());
        TestConfigurationListener testConfigListener = new TestConfigurationListener();
        config.addConfigurationListener(testConfigListener);

        // Create Node
        clientWithNamespace.delete().forPath(path);
        Thread.sleep(WAIT_NOTIFICATION_MILLIS); // Wait notification

        // Test
        assertThat(testConfigListener.getEventType(), equalTo(ZKPropertiesConfiguration.EVENT_NODE_DELETE));
        assertThat(testConfigListener.getPath(), equalTo("/" + namespace + path));
    }

    private class TestConfigurationListener implements ConfigurationListener {

        private int eventType;

        private String path;

        /**
         * {@inheritDoc}
         */
        @Override
        public void configurationChanged(final ConfigurationEvent pEvent) {

            if (!pEvent.isBeforeUpdate()) {
                eventType = pEvent.getType();
                path = (String) pEvent.getPropertyValue();
                System.out.println(path);
            }
        }

        public int getEventType() {
            return eventType;
        }

        public String getPath() {
            return path;
        }
    }
}
