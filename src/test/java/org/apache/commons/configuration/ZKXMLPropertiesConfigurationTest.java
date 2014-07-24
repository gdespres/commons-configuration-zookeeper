package org.apache.commons.configuration;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class ZKXMLPropertiesConfigurationTest extends ZKConfigurationTest {

    // ========================================================================
    //
    // ========================================================================

    @Test
    public void testXMLProperties() throws ConfigurationException {

        // init zookeeper server
        createOrUpdatePath("/propertiesAsXml.xml", loadFileFromClasspath("propertiesAsXml.xml"));

        ZKXMLPropertiesConfiguration config = new ZKXMLPropertiesConfiguration(client, "propertiesAsXml.xml");
        assertThat(config.getString("application.fullname"), equalTo("name-version"));
    }
}
