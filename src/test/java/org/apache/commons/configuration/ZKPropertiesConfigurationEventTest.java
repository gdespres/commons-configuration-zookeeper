/**
 *
 */
package org.apache.commons.configuration;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.apache.commons.configuration.event.ConfigurationEvent;
import org.apache.commons.configuration.event.ConfigurationListener;
import org.apache.commons.configuration.reloading.ZKNodeChangeEventReloadingStrategy;
import org.junit.Test;

/**
 *
 */
public class ZKPropertiesConfigurationEventTest extends ZKConfigurationTest {

    @Test
    public void testPropertiesCreatedEvent() throws Exception {

        String path = "/" + System.currentTimeMillis() + ".properties";

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
        Thread.sleep(500); // Wait notification

        // Test
        assertThat(testConfigListener.getEventType(), equalTo(ZKPropertiesConfiguration.EVENT_NODE_CREATE));
    }

    @Test
    public void testPropertiesChangedEvent() throws Exception {

        String path = "/" + System.currentTimeMillis() + ".properties";

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
        Thread.sleep(500); // Wait notification

        // Test
        assertThat(testConfigListener.getEventType(), equalTo(ZKPropertiesConfiguration.EVENT_NODE_UPDATE));
    }

    @Test
    public void testPropertiesDeletedEvent() throws Exception {

        String path = "/" + System.currentTimeMillis() + ".properties";

        // Init ZooKeeper
        client.create().creatingParentsIfNeeded().forPath(path, "value".getBytes());

        //
        ZKPropertiesConfiguration config = new ZKPropertiesConfiguration(client);
        config.setPath(path);
        config.load();
        config.setReloadingStrategy(new ZKNodeChangeEventReloadingStrategy());
        TestConfigurationListener testConfigListener = new TestConfigurationListener();
        config.addConfigurationListener(testConfigListener);

        // Create Node
        client.delete().forPath(path);
        Thread.sleep(500); // Wait notification

        // Test
        assertThat(testConfigListener.getEventType(), equalTo(ZKPropertiesConfiguration.EVENT_NODE_DELETE));
    }

    private class TestConfigurationListener implements ConfigurationListener {

        private int eventType;

        /**
         * {@inheritDoc}
         */
        @Override
        public void configurationChanged(final ConfigurationEvent pEvent) {

            if (!pEvent.isBeforeUpdate()) {
                eventType = pEvent.getType();
            }
        }

        public int getEventType() {
            return eventType;
        }
    }
}
