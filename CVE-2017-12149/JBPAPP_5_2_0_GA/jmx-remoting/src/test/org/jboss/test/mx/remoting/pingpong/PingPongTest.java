/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.test.mx.remoting.pingpong;

import org.jboss.remoting.InvokerLocator;
import org.jboss.remoting.detection.multicast.MulticastDetector;
import org.jboss.remoting.ident.Identity;
import org.jboss.remoting.network.NetworkRegistry;
import org.jboss.remoting.transport.Connector;
import org.jboss.remoting.transport.PortUtil;
import org.w3c.dom.Document;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;

/**
 * a simple ping pong test that will send notifications and make remote
 * JMX invocations back and forth to other ping pong mbeans on the network
 *
 * @author <a href="mailto:jhaynie@vocalocity.net">Jeff Haynie</a>
 * @author <a href="mailto:telrod@e2technologies.net">Tom Elrod</a>
 * @version $Revision: 81084 $
 */
public class PingPongTest
{

    public static void main(String[] args)
    {
        try
        {
            //org.apache.log4j.BasicConfigurator.configure();
            System.setProperty("jboss.identity",Identity.createUniqueID());
            MBeanServer server = MBeanServerFactory.createMBeanServer();

            System.out.println("my identity is: "+Identity.get(server));


            NetworkRegistry registry = NetworkRegistry.getInstance();
            server.registerMBean(registry, new ObjectName("remoting:type=NetworkRegistry"));

            int port = PortUtil.findFreePort("localhost");

            Connector connector = new Connector();
            InvokerLocator locator = new InvokerLocator("socket://localhost:" + port);
            StringBuffer buf = new StringBuffer();
            buf.append("<?xml version=\"1.0\"?>\n");
            buf.append("<handlers>\n");
            buf.append("  <handler subsystem=\"JMX\">org.jboss.mx.remoting.JMXSubsystemInvocationHandler</handler>\n");
            buf.append("</handlers>\n");
            Document xml = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(buf.toString().getBytes()));
            connector.setInvokerLocator(locator.getLocatorURI());
            connector.setConfiguration(xml.getDocumentElement());
            ObjectName obj = new ObjectName("jboss.remoting:type=Connector,transport=" + locator.getProtocol());
            server.registerMBean(connector, obj);

            connector.start();

            MulticastDetector detector = new MulticastDetector();
            server.registerMBean(detector, new ObjectName("remoting:type=Detector,transport=multicast"));
            detector.start();

            PingPong pp = new PingPong();
            ObjectName objName = new ObjectName("test:type=PingPong");
            server.registerMBean(pp, objName);
        }
        catch(Throwable e)
        {
            e.printStackTrace();
        }
    }
}
