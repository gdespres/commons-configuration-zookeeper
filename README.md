# commons-configuration-zookeeper

Apache [ZooKeeper][1] extention for [Commons Configuration][2] using [Apache Curator][3]

## Usage

```java
// Init Curator
String connectString = "localhost:2181";
CuratorFramework client = CuratorFrameworkFactory.newClient(connectString, new ExponentialBackoffRetry(1000, 3));
client.start();

try {
    ZKPropertiesConfiguration config = new ZKPropertiesConfiguration(client, "/path/to/file.properties");
    
    // add reloading strategy
    config.setReloadingStrategy(new ZKNodeChangeEventReloadingStrategy()); // properties are reloaded when zookeeper node changes.
    
    // add listener
    config.addConfigurationListener(new ConfigurationListener() {
        public void configurationChanged(final ConfigurationEvent event) {
            if (!event.isBeforeUpdate()) {
                switch(event.getType()) {
                    case ZKPropertiesConfiguration.EVENT_NODE_CREATE : 
                        System.out.println("Path '" + event.getPropertyValue() + "' has been created !");
                        break;
                    case ZKPropertiesConfiguration.EVENT_NODE_UPDATE : 
                        System.out.println("Path '" + event.getPropertyValue() + "' has been updated !");
                        break;
                    case ZKPropertiesConfiguration.EVENT_NODE_DELETE : 
                        System.out.println("Path '" + event.getPropertyValue() + "' has been deleted !");
                        break;
                }
            }
        }
    });
    
    String property = config.getString("property.name");
} catch (ConfigurationException e) {

    // Exception thrown if node format is not valid.
}
```

## Licence

commons-configuration-zookeeper is licensed under the [Apache Software License, Version 2.0][AL2].

[1]: http://zookeeper.apache.org/
[2]: http://commons.apache.org/proper/commons-configuration/
[3]: http://curator.apache.org/
[AL2]: http://www.apache.org/licenses/LICENSE-2.0.txt
