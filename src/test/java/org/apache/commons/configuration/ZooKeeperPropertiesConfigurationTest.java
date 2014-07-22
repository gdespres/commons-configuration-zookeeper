/**
 *
 */
package org.apache.commons.configuration;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/**
 *
 */
public class ZooKeeperPropertiesConfigurationTest extends ZooKeeperConfigurationTest {

    // ========================================================================
    //
    // ========================================================================

    @Test
    public void test1() throws ConfigurationException {

        // init zookeeper server
        createOrUpdatePath("test1.properties", loadFileFromClasspath("test1.properties"));

        ZooKeeperPropertiesConfiguration config = new ZooKeeperPropertiesConfiguration(client);
        config.setPath("test1.properties");
        config.load();

        String property = config.getString("prop.helloworld");
        assertThat(property, equalTo("hello world !!!"));

        //
        config.setProperty("prop.hello", "salut");
        property = config.getString("prop.helloworld");
        assertThat(property, equalTo("salut world !!!"));

        // refresh
        config.refresh();
        property = config.getString("prop.helloworld");
        assertThat(property, equalTo("hello world !!!"));
    }
}
