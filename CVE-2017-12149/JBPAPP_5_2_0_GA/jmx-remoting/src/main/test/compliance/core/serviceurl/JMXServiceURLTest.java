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

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import javax.management.remote.JMXServiceURL;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:tom@jboss.org">Tom Elrod</a>
 */
public class JMXServiceURLTest extends TestCase
{
   private final String protocol = "rmi";
   private final String defaultProtocol = "jmxmp";
   private final String ipv6Host = "1080:0:0:0:8:800:200C:417A";
   private final String longSlashPath = "/jndi/rmi://localhost:1099/jmxconnector";
   private final String longSemicolonPath = ";jndi/rmi://localhost:1099/jmxconnector";
   private final String host = "localhost";
   private final int port = 5400;

   public JMXServiceURLTest(String name)
   {
      super(name);
   }

   public void testValidURLWithIPv6Host()
   {

      JMXServiceURL serviceURL = null;

      try
      {
         serviceURL = new JMXServiceURL("service:jmx:" + protocol + "://[" + ipv6Host + "]" + longSlashPath);
      }
      catch(Throwable ex)
      {
         assertTrue("Got exception " + ex, false);
      }
      assertEquals(protocol, serviceURL.getProtocol());
      assertEquals(ipv6Host, serviceURL.getHost());
      assertEquals(longSlashPath, serviceURL.getURLPath());
      assertEquals(0, serviceURL.getPort());

      try
      {
         serviceURL = new JMXServiceURL("service:jmx:" + protocol + "://[" + ipv6Host + "]:" + port + longSlashPath);
      }
      catch(Throwable ex)
      {
         assertTrue("Got exception " + ex, false);
      }
      assertEquals(protocol, serviceURL.getProtocol());
      assertEquals(ipv6Host, serviceURL.getHost());
      assertEquals(longSlashPath, serviceURL.getURLPath());
      assertEquals(port, serviceURL.getPort());

   }

   public void testToString()
   {
      String url = "service:jmx:" + protocol + "://[" + ipv6Host + "]:" + port + longSlashPath;
      JMXServiceURL serviceURL = null;

      try
      {
         serviceURL = new JMXServiceURL(url);
      }
      catch(Throwable ex)
      {
         assertTrue("Got exception " + ex, false);
      }
      assertEquals(url, serviceURL.toString());
   }

   public void testEquals()
   {
      String url = "service:jmx:" + protocol + "://[" + ipv6Host + "]:" + port + longSlashPath;
      JMXServiceURL serviceURL = null;
      JMXServiceURL serviceURL2 = null;

      try
      {
         serviceURL = new JMXServiceURL(url);
         serviceURL2 = new JMXServiceURL(url);
      }
      catch(Throwable ex)
      {
         assertTrue("Got exception " + ex, false);
      }
      assertEquals(serviceURL, serviceURL2);
   }

   public void testNoHostOrPort()
   {
      JMXServiceURL serviceURL = null;
      try
      {
         serviceURL = new JMXServiceURL("service:jmx:" + protocol + "://" + longSlashPath);
      }
      catch(Throwable ex)
      {
         assertTrue("Got exception " + ex, false);
      }
      assertEquals(protocol, serviceURL.getProtocol());
      try
      {
         assertEquals(InetAddress.getLocalHost().getHostName(), serviceURL.getHost());
      }
      catch(UnknownHostException e)
      {
         e.printStackTrace();
      }
      assertEquals(longSlashPath, serviceURL.getURLPath());
      assertEquals(0, serviceURL.getPort());

   }

   public void testValidURLWithSlashPathSeperator()
   {
      JMXServiceURL serviceURL = null;
      try
      {
         serviceURL = new JMXServiceURL("service:jmx:" + protocol + "://" + host + longSlashPath);
      }
      catch(Throwable ex)
      {
         assertTrue("Got exception " + ex, false);
      }
      assertEquals(protocol, serviceURL.getProtocol());
      assertEquals(host, serviceURL.getHost());
      assertEquals(longSlashPath, serviceURL.getURLPath());
      assertEquals(0, serviceURL.getPort());

      try
      {
         serviceURL = new JMXServiceURL("service:jmx:" + protocol + "://" + host + ":" + port + longSlashPath);
      }
      catch(Throwable ex)
      {
         assertTrue("Got exception " + ex, false);
      }
      assertEquals(protocol, serviceURL.getProtocol());
      assertEquals(host, serviceURL.getHost());
      assertEquals(longSlashPath, serviceURL.getURLPath());
      assertEquals(port, serviceURL.getPort());
   }

   public void testValidURLWithSemicolonPathSeperator()
   {
      JMXServiceURL serviceURL = null;

      try
      {
         serviceURL = new JMXServiceURL("service:jmx:" + protocol + "://" + host + longSemicolonPath);
      }
      catch(Throwable ex)
      {
         assertTrue("Got exception " + ex, false);
      }
      assertEquals(protocol, serviceURL.getProtocol());
      assertEquals(host, serviceURL.getHost());
      assertEquals(longSemicolonPath, serviceURL.getURLPath());
      assertEquals(0, serviceURL.getPort());

      try
      {
         serviceURL = new JMXServiceURL("service:jmx:" + protocol + "://" + host + ":" + port + longSemicolonPath);
      }
      catch(Throwable ex)
      {
         assertTrue("Got exception " + ex, false);
      }
      assertEquals(protocol, serviceURL.getProtocol());
      assertEquals(host, serviceURL.getHost());
      assertEquals(longSemicolonPath, serviceURL.getURLPath());
      assertEquals(port, serviceURL.getPort());

   }

   public void testNoProtocolWithSuffix()
   {
      JMXServiceURL serviceURL = null;

      try
      {
         serviceURL = new JMXServiceURL("service:jmx:://" + host + ":" + port);
      }
      catch(Throwable ex)
      {
         assertTrue("Got exception " + ex, false);
      }
      assertEquals(defaultProtocol, serviceURL.getProtocol());
      assertEquals(host, serviceURL.getHost());
      assertEquals(port, serviceURL.getPort());
   }

   public void testParamConstructWithIPv6Host()
   {
      JMXServiceURL serviceURL = null;

      try
      {
         serviceURL = new JMXServiceURL(protocol, "[" + ipv6Host + "]", port);
      }
      catch(Throwable ex)
      {
         assertTrue("Got exception " + ex, false);
      }
      assertEquals(protocol, serviceURL.getProtocol());
      assertEquals(ipv6Host, serviceURL.getHost());
      assertEquals(port, serviceURL.getPort());
   }

   public void testNullURL()
   {
      try
      {
         JMXServiceURL serviceURL = new JMXServiceURL(null);
      }
      catch(MalformedURLException e)
      {
         assertTrue("Got MalformedULRException and expected NullPointerException.", false);
      }
      catch(NullPointerException npex)
      {
         assertTrue(true);
         return;
      }
      catch(Throwable ex)
      {
         assertTrue("Expected NullPointerException, but got " + ex, false);
      }
      assertTrue("Did not throw NullPointerException as expected.", false);
   }

   public void testNoPrefix()
   {
      try
      {
         JMXServiceURL serviceURL = new JMXServiceURL("rmi://myhost:0");
      }
      catch(MalformedURLException e)
      {
         assertTrue(true);
         return;
      }
      catch(Throwable ex)
      {
         assertTrue("Expected MalformedURLException, but got " + ex, false);
      }
      assertTrue("Did not throw MalformedURLException as expected.", false);
   }

   public void testPortWithoutHost()
   {
      try
      {
         JMXServiceURL serviceURL = new JMXServiceURL("service:jmx:://:1234");
      }
      catch(MalformedURLException e)
      {
         assertTrue(true);
         return;
      }
      catch(Throwable ex)
      {
         assertTrue("Expected MalformedURLException, but got " + ex, false);
      }
      assertTrue("Did not throw MalformedURLException as expected.", false);
   }

   public void testNoProtocolWithoutSuffix()
   {
      try
      {
         JMXServiceURL serviceURL = new JMXServiceURL("service:jmx:myhost:0");
      }
      catch(MalformedURLException e)
      {
         assertTrue(true);
         return;
      }
      catch(Throwable ex)
      {
         assertTrue("Expected MalformedURLException, but got " + ex, false);
      }
      assertTrue("Did not throw MalformedURLException as expected.", false);
   }

   public void testInvalidProtocolCharacter()
   {
      try
      {
         JMXServiceURL serviceURL = new JMXServiceURL("service:jmx:rm$i://myhost:0");
      }
      catch(MalformedURLException e)
      {
         assertTrue(true);
         return;
      }
      catch(Throwable ex)
      {
         assertTrue("Expected MalformedURLException, but got " + ex, false);
      }
      assertTrue("Did not throw MalformedURLException as expected.", false);
   }

}
