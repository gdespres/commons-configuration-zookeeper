package org.apache.commons.configuration;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.apache.commons.configuration.reloading.ZKPathChildrenChangeEventReloadingStrategy;
import org.junit.Test;

public class ZKPathChildrenConfigurationTest extends ZKConfigurationTest {

    @Test
    public void test() throws Exception {

        // init
        client.create().creatingParentsIfNeeded().forPath("/pathChildren/prop1", "value1".getBytes());
        client.create().creatingParentsIfNeeded().forPath("/pathChildren/prop2", "value2".getBytes());

        //
        ZKPathChildrenBaseConfiguration config = new ZKPathChildrenBaseConfiguration(client, "/pathChildren");
        config.setReloadingStrategy(new ZKPathChildrenChangeEventReloadingStrategy());

        assertThat(config.getString("prop1"), equalTo("value1"));
        assertThat(config.getString("prop2"), equalTo("value2"));
        assertThat(config.getString("prop3"), nullValue());

        client.setData().forPath("/pathChildren/prop1", "value1-updated".getBytes());
        client.delete().forPath("/pathChildren/prop2");
        client.create().creatingParentsIfNeeded().forPath("/pathChildren/prop3", "value3".getBytes());
        Thread.sleep(500);

        assertThat(config.getString("prop1"), equalTo("value1-updated"));
        assertThat(config.getString("prop2"), nullValue());
        assertThat(config.getString("prop3"), equalTo("value3"));
    }
}
