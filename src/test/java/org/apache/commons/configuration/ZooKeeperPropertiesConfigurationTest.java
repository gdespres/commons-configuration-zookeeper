/**
 *
 */
package org.apache.commons.configuration;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.configuration.event.ConfigurationEvent;
import org.apache.commons.configuration.event.ConfigurationListener;
import org.apache.commons.configuration.reloading.ZooKeeperNodeReloadingStategy;
import org.apache.commons.lang.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;
import org.apache.zookeeper.KeeperException.NodeExistsException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.io.ByteStreams;

/**
 *
 */
public class ZooKeeperPropertiesConfigurationTest {

    // ========================================================================
    // ATTRIBUTES
    // ========================================================================

    private static CuratorFramework client;

    private static final String NAMESPACE = "application";

    private static TestingServer server;

    // ========================================================================
    //
    // ========================================================================

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {

        server = new TestingServer();
        client = CuratorFrameworkFactory.newClient(server.getConnectString(), new ExponentialBackoffRetry(1000, 3));
        client.start();
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        client.close();
        server.close();
    }

    // ========================================================================
    //
    // ========================================================================

    @Test
    public void test1() throws ConfigurationException {

        ZooKeeperPropertiesConfiguration config = new ZooKeeperPropertiesConfiguration(client, NAMESPACE);
        config.setNodePath("test1/prop1.properties");
        config.load();

        String propPath1 = config.getString("prop.path1");
        assertThat(propPath1, equalTo("http://www.base-site.com/path1"));
    }

    @Test
    public void test2() throws ConfigurationException {

        // init zookeeper
        createOrUpdatePath("test2/prop1.properties", loadFileFromClasspath("test2/_zookeeper/prop1.properties"));

        // start test
        ZooKeeperPropertiesConfiguration config = new ZooKeeperPropertiesConfiguration(client, NAMESPACE);
        config.setNodePath("test2/prop1.properties");
        config.load();

        String propPath1 = config.getString("prop.path1");
        assertThat(propPath1, equalTo("http://www.base-site.com/path1ZooKeeper"));
    }

    @Test
    public void test3() throws ConfigurationException {

        // init zookeeper
        createOrUpdatePath("prop-included.properties", loadFileFromClasspath("test3/_zookeeper/prop-included.properties"));

        // start test
        ZooKeeperPropertiesConfiguration config = new ZooKeeperPropertiesConfiguration(client, NAMESPACE);
        config.setNodePath("test3/prop1.properties");
        config.load();

        String propPath1 = config.getString("prop.path1");
        assertThat(propPath1, equalTo("http://www.zookeeper-site.com/path1"));
    }

    @Test
    public void test4() throws ConfigurationException {

        // init zookeeper
        createOrUpdatePath("test4/prop1.properties", loadFileFromClasspath("test4/_zookeeper/prop1.properties"));
        createOrUpdatePath("test4/prop-included.properties", loadFileFromClasspath("test4/_zookeeper/prop-included.properties"));

        // start test
        ZooKeeperPropertiesConfiguration config = new ZooKeeperPropertiesConfiguration(client, NAMESPACE);
        config.setNodePath("test4/prop1.properties");
        config.load();

        String propPath1 = config.getString("prop.path1");
        assertThat(propPath1, equalTo("http://www.zookeeper-site.com/path1ZooKeeper"));
    }

    @Test
    public void test5() throws ConfigurationException, InterruptedException {

        // init zookeeper
        createOrUpdatePath("test5/prop1.properties", loadFileFromClasspath("test5/_zookeeper/prop1.properties"));
        createOrUpdatePath("test5/prop-included.properties", loadFileFromClasspath("test5/_zookeeper/prop-included.properties"));

        // start test
        ZooKeeperPropertiesConfiguration config = new ZooKeeperPropertiesConfiguration(client, NAMESPACE);
        config.setNodePath("test5/prop1.properties");
        config.load();
        config.addConfigurationListener(new ConfigurationListener() {

            @Override
            public void configurationChanged(final ConfigurationEvent pEvent) {

                if (pEvent.getType() == AbstractFileConfiguration.EVENT_RELOAD && !pEvent.isBeforeUpdate()) {
                    System.out.println("Configuration has changed !!!");
                }
            }
        });
        config.setReloadingStrategy(new ZooKeeperNodeReloadingStategy());

        String propPath1 = config.getString("prop.path1");
        assertThat(propPath1, equalTo("http://www.zookeeper-site.com/path1ZooKeeper"));

        // update zookeeper
        createOrUpdatePath("test5/prop1.properties", loadFileFromClasspath("test5/_zookeeper/prop1-updated.properties"));
        Thread.sleep(200); // wait for notification propagation.

        propPath1 = config.getString("prop.path1");
        assertThat(propPath1, equalTo("http://www.zookeeper-site.com/path1ZooKeeperReloaded"));

        // update included file

        createOrUpdatePath("test5/prop-included.properties", loadFileFromClasspath("test5/_zookeeper/prop-included-updated.properties"));
        Thread.sleep(200); // wait for notification propagation.

        propPath1 = config.getString("prop.path1");
        assertThat(propPath1, equalTo("http://www.zookeeper-reloaded-site.com/path1ZooKeeperReloaded"));
    }

    // ========================================================================
    // PRIVATE METHODS
    // ========================================================================

    private byte[] loadFileFromClasspath(final String fileName) {

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader != null) {
            InputStream in = classLoader.getResourceAsStream(fileName);
            try {
                return ByteStreams.toByteArray(in);
            } catch (IOException e) {
                return new byte[0];
            }
        }

        return new byte[0];
    }

    private void createOrUpdatePath(final String pPath, final byte[] pData) {

        CuratorFramework namespacedClient = client.usingNamespace(NAMESPACE);
        String path = pPath;
        if (!StringUtils.startsWith(path, "/")) {
            path = "/" + path;
        }
        try {
            try {
                namespacedClient.create().creatingParentsIfNeeded().forPath(path, pData);
            } catch (NodeExistsException nee) {
                namespacedClient.setData().forPath(path, pData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
