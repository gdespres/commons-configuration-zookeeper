package org.apache.commons.configuration;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class ZooKeeperXMLPropertiesConfigurationTest extends ZooKeeperConfigurationTest {

    // ========================================================================
    //
    // ========================================================================

    @Test
    public void testXMLProperties() throws ConfigurationException {

        // init zookeeper server
        createOrUpdatePath("/propertiesAsXml.xml", loadFileFromClasspath("propertiesAsXml.xml"));

        ZooKeeperXMLPropertiesConfiguration config = new ZooKeeperXMLPropertiesConfiguration(client, "propertiesAsXml.xml");
        assertThat(config.getString("application.fullname"), equalTo("name-version"));
    }
}
