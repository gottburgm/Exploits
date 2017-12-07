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
package test.compliance.core.serviceurl;

import java.io.IOException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:tom@jboss.org">Tom Elrod</a>
 */
public class JMXConnectorFactoryTest extends TestCase
{
   private final String protocol = "rmi";


   public JMXConnectorFactoryTest(String name)
   {
      super(name);
   }


   public void testConnect()
   {
      String serviceURL = "service:jmx:" + protocol + "://localhost:5900";
      try
      {
         JMXServiceURL jmxServiceURL = new JMXServiceURL(serviceURL);
         JMXConnector connector = JMXConnectorFactory.connect(jmxServiceURL);
         assertNotNull(connector);
      }
      catch(IOException e)
      {
         assertTrue(e.getMessage(), false);
      }
   }

   //TODO: -TME Need to add more complicated unit tests.  The only one now just checks for creating connector.
}