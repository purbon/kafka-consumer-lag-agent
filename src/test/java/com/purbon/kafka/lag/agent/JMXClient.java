package com.purbon.kafka.lag.agent;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

public class JMXClient {

  private MBeanServerConnection mbsc;
  private JMXConnector jmxc;

  public void connect(String host, long port) throws IOException {

    String urlString = "service:jmx:rmi:///jndi/rmi://" + host + ":" + port + "/jmxrmi";

    JMXServiceURL url = new JMXServiceURL(urlString);
    jmxc = JMXConnectorFactory.connect(url, null);
    mbsc = jmxc.getMBeanServerConnection();
  }

  public void printDomains() throws IOException, MalformedObjectNameException {
    System.out.println("\nDomains:");
    String domains[] = mbsc.getDomains();
    Arrays.sort(domains);
    for (String domain : domains) {
      System.out.println("\tDomain = " + domain);
    }
    String domain = "com.purbon.kafka";

    ObjectName mbeanName = new ObjectName(domain+":*");

    Set<ObjectName> names = mbsc.queryNames(mbeanName, null);
    for(ObjectName name : names) {
      System.out.println(name);
    }

  }

  public Object query(String mbean, Class myMBeanClass)
      throws MalformedObjectNameException {
    ObjectName mbeanName = new ObjectName(mbean);

    Object bean = JMX.newMBeanProxy(mbsc, mbeanName, myMBeanClass, true);
    return bean;
  }

  public void close() {
    try {
      jmxc.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
