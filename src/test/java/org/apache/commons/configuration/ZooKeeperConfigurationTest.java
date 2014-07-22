/**
 *
 */
package org.apache.commons.configuration;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;
import org.apache.zookeeper.KeeperException.NodeExistsException;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.google.common.io.ByteStreams;

/**
 *
 */
public class ZooKeeperConfigurationTest {

    // ========================================================================
    // ATTRIBUTES
    // ========================================================================

    protected static CuratorFramework client;

    protected static TestingServer server;

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
    // TOOLS
    // ========================================================================

    protected byte[] loadFileFromClasspath(final String fileName) {

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

    protected void createOrUpdatePath(final String pPath, final byte[] pData) {

        //CuratorFramework namespacedClient = client.usingNamespace(NAMESPACE);
        String path = pPath;
        if (!StringUtils.startsWith(path, "/")) {
            path = "/" + path;
        }
        try {
            try {
                client.create().creatingParentsIfNeeded().forPath(path, pData);
            } catch (NodeExistsException nee) {
                client.setData().forPath(path, pData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
