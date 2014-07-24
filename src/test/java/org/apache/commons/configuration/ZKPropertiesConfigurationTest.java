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
public class ZKPropertiesConfigurationTest extends ZKConfigurationTest {

    // ========================================================================
    //
    // ========================================================================

    @Test
    public void test1() throws ConfigurationException {

        // init zookeeper server
        createOrUpdatePath("test1.properties", loadFileFromClasspath("test1.properties"));

        ZKPropertiesConfiguration config = new ZKPropertiesConfiguration(client);
        config.setPath("test1.properties");
        config.load();

        String property = config.getString("prop.helloworld");
        assertThat(property, equalTo("hello world !!!"));

        //
        config.setProperty("prop.hello", "salut");
        property = config.getString("prop.helloworld");
        assertThat(property, equalTo("salut world !!!"));

        // refresh
        config.reload();
        property = config.getString("prop.helloworld");
        assertThat(property, equalTo("hello world !!!"));
    }
}
