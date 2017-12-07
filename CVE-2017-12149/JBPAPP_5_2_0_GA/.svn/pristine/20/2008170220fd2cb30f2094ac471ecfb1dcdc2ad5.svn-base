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
package javax.management.remote.rmi;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.jboss.logging.Logger;
import org.jboss.util.Base64;

/**
 * @author <a href="mailto:tom@jboss.org">Tom Elrod</a>
 */
public class RMIConnectorServer extends JMXConnectorServer
{
   public static final String JNDI_REBIND_ATTRIBUTE = "jmx.remote.jndi.rebind";
   public static final String RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE = "jmx.remote.rmi.client.socket.factory";
   public static final String RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE = "jmx.remote.rmi.server.socket.factory";

   private JMXServiceURL serviceURL = null;
   private Map environment = Collections.EMPTY_MAP;
   private RMIServerImpl serverImpl = null;

   private boolean isActive = false;
   private boolean isStopped = false;

   protected static Logger log = Logger.getLogger(RMIConnectorServer.class.getName());

   public RMIConnectorServer(JMXServiceURL url, Map environment) throws IOException
   {
      this(url, environment, null);
   }

   public RMIConnectorServer(JMXServiceURL url, Map environment, MBeanServer mbeanServer) throws IOException
   {
      this(url, environment, null, mbeanServer);
   }

   public RMIConnectorServer(JMXServiceURL url, Map environment, RMIServerImpl rmiServerImpl, MBeanServer mbeanServer)
         throws IOException
   {
      super(mbeanServer);

      validateServiceURL(url);
      this.serviceURL = url;
      if(environment != null)
      {
         this.environment = Collections.unmodifiableMap(environment);
      }

      this.serverImpl = rmiServerImpl;

   }

   private void validateServiceURL(JMXServiceURL url) throws MalformedURLException
   {
      if(url != null)
      {
         String protocol = url.getProtocol();
         if(protocol == null || !(protocol.equalsIgnoreCase("rmi") || protocol.equalsIgnoreCase("iiop")))
         {
            throw new MalformedURLException("Protocol for JMXServiceURL is " + protocol + ".  Must be either rmi or iiop.");
         }
      }
      else
      {
         throw new IllegalArgumentException("JMXServiceURL can not be null");
      }
   }

   public JMXConnector toJMXConnector(Map env) throws IOException
   {
      return null;
   }

   public void start() throws IOException
   {
      if(isActive())
      {
         return; // already started, so nothing to do
      }

      if(isStopped)
      {
         // yes, this is per spec.
         throw new IOException("RMIConnectorServer can not be started once stopped.");
      }

      if(serverImpl == null)
      {
         // create new instances of server impl
         serverImpl = createServer(getAddress(), getAttributes());
      }

      // get the classloader to use for the server impl
      ClassLoader classLoader = findClassLoader(getAttributes(), getMBeanServer());

      serverImpl.setRMIConnectorServer(this);
      serverImpl.setMBeanServer(getMBeanServer());
      serverImpl.setDefaultClassLoader(classLoader);

      serverImpl.export();

      // now need to bind server
      RMIServer stub = (RMIServer) serverImpl.toStub();

      // check to see if is for jndi binding
      JMXServiceURL url = getAddress();
      final String jndiPathKey = "/jndi/";
      final String jndiPathKey2 = ";jndi/";
      if(url != null && url.getURLPath() != null &&
         (url.getURLPath().startsWith(jndiPathKey) || url.getURLPath().startsWith(jndiPathKey2)))
      {
         // get the jndi path
         String jndiPath = url.getURLPath().substring(jndiPathKey.length());

         boolean rebind = false; // by default is false
         // now have to see if environment varible set for this
         String srebind = (String) environment.get(JNDI_REBIND_ATTRIBUTE);
         if(Boolean.getBoolean(srebind))
         {
            rebind = true;
         }

         //TODO: -TME Ignoring IIOP binding for now (few extra steps required)

         // now do actual jndi binding
         InitialContext ctx = null;
         try
         {
            ctx = new InitialContext(new Hashtable(environment));
            if(rebind)
            {
               ctx.rebind(jndiPath, stub);
            }
            else
            {
               ctx.bind(jndiPath, stub);
            }
            log.debug("Bound rmi connector stub (" + stub + ") into jndi with path: " + jndiPath);
         }
         catch(NamingException ne)
         {
            log.error("Error binding rmi connector stub", ne);
            throw new IOException(ne.getMessage());
         }
         finally
         {
            try
            {
               if(ctx != null)
               {
                  ctx.close();
               }
            }
            catch(NamingException ne)
            {
               log.debug("Error closing InitialContext.", ne);
            }
         }
      }
      else // not jndi, so need to convrt service url
      {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         ObjectOutputStream oos = new ObjectOutputStream(baos);
         oos.writeObject(stub);
         oos.flush();
         oos.close();
         byte[] bytes = baos.toByteArray();
         String encoded = Base64.encodeBytes(bytes);
         String path = "/stub/" + encoded;

         this.serviceURL = new JMXServiceURL(url.getProtocol(), url.getHost(), url.getPort(), path);

      }

      isActive(true);

      log.debug("RMIConnectorServer started at service url: " + getAddress());
   }

   //spec section 2.11.2
   private ClassLoader findClassLoader(Map environment, MBeanServer server) throws IllegalArgumentException
   {
      ClassLoader classLoader = null;

      if(environment != null)
      {
         // look for the classloader instance to be in map
         Object loaderInstance = environment.get(JMXConnectorServerFactory.DEFAULT_CLASS_LOADER);

         if(loaderInstance != null && loaderInstance instanceof ClassLoader)
         {
            classLoader = (ClassLoader) loaderInstance;
         }

         // now try to find classloader by object name specified in map
         Object loaderObjectName = environment.get(JMXConnectorServerFactory.DEFAULT_CLASS_LOADER_NAME);
         if(loaderObjectName != null)
         {
            if(classLoader != null) //this is problem because can not have value for both
            {
               throw new IllegalArgumentException("Can not specify class loader using both the " +
                                                  JMXConnectorServerFactory.DEFAULT_CLASS_LOADER + " and " +
                                                  JMXConnectorServerFactory.DEFAULT_CLASS_LOADER_NAME + " keys.  " +
                                                  "Only one of the two can be supplied.");
            }
            else if(!(loaderObjectName instanceof ObjectName))
            {
               throw new IllegalArgumentException("The value specified for " + JMXConnectorServerFactory.DEFAULT_CLASS_LOADER_NAME +
                                                  " is not of type " + ObjectName.class.getName());
            }

            // now to get get clasloader from mbean server
            ObjectName objectName = (ObjectName) loaderObjectName;
            try
            {
               classLoader = server.getClassLoader(objectName);
            }
            catch(InstanceNotFoundException e)
            {
               throw new IllegalArgumentException("The ObjectName specified by " + JMXConnectorServerFactory.DEFAULT_CLASS_LOADER_NAME +
                                                  " was not found within the MBeanServer.");
            }
         }
      }

      // if still equals null, just use thread context
      if(classLoader == null)
      {
         classLoader = Thread.currentThread().getContextClassLoader();
      }

      return classLoader;
   }


   private RMIServerImpl createServer(JMXServiceURL address, Map environment) throws IOException
   {
      RMIServerImpl impl = null;

      String protocol = address.getProtocol();

      if("iiop".equalsIgnoreCase(protocol))
      {
         // create new RMIIIOPServerImpl
         impl = new RMIIIOPServerImpl(environment);
      }
      else if(protocol == null || protocol.length() == 0 || "rmi".equalsIgnoreCase("rmi"))
      {
         int port = address.getPort();
         RMIClientSocketFactory clientSocketFactory = (RMIClientSocketFactory) environment.get(RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE);
         RMIServerSocketFactory serverSocketFactory = (RMIServerSocketFactory) environment.get(RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE);
         impl = new RMIJRMPServerImpl(port, clientSocketFactory, serverSocketFactory, environment);
      }
      else
      {
         throw new MalformedURLException("Can not create connector server.  Protocol must be either 'rmi', 'iiop', or null.");
      }
      return impl;
   }

   public void stop() throws IOException
   {
      if(isStopped)
      {
         return;
      }

      isActive = false;
      isStopped = true;

      if(serverImpl != null)
      {
         serverImpl.close();
      }

      // check to see if is for jndi binding
      JMXServiceURL url = getAddress();
      final String jndiPathKey = "/jndi/";
      final String jndiPathKey2 = ";jndi/";
      if(url != null && url.getURLPath() != null &&
         (url.getURLPath().startsWith(jndiPathKey) || url.getURLPath().startsWith(jndiPathKey2)))
      {
         // get the jndi path
         String jndiPath = url.getURLPath().substring(jndiPathKey.length());

         InitialContext ctx = null;
         try
         {
            ctx = new InitialContext(new Hashtable(environment));
            ctx.unbind(jndiPath);
         }
         catch(NamingException ne)
         {
            log.error("Error unbinding rmi connector stub", ne);
         }
         finally
         {
            try
            {
               if(ctx != null)
               {
                  ctx.close();
               }
            }
            catch(NamingException ne)
            {
               log.debug("Error closing InitialContext.", ne);
            }
         }
      }

   }

   public boolean isActive()
   {
      return isActive;
   }

   private void isActive(boolean active)
   {
      this.isActive = active;
   }

   public JMXServiceURL getAddress()
   {
      return serviceURL;
   }

   public Map getAttributes()
   {
      return environment;
   }

   protected void connectionOpened(String connectionId, String message, Object userData)
   {
      super.connectionOpened(connectionId, message, userData);
   }

   protected void connectionClosed(String connectionId, String message, Object userData)
   {
      super.connectionClosed(connectionId, message, userData);
   }

   protected void connectionFailed(String connectionId, String message, Object userData)
   {
      //TODO: -TME -Implement
   }
}