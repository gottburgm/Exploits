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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.io.InvalidObjectException;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;
import javax.security.auth.Subject;
import org.jboss.logging.Logger;
import org.jboss.mx.remoting.rmi.ClientMBeanServerConnection;
import org.jboss.mx.remoting.rmi.ConnectionNotifier;
import org.jboss.mx.remoting.rmi.ClientNotifier;
import org.jboss.mx.server.ObjectInputStreamWithClassLoader;
import org.jboss.util.Base64;

/**
 * @author <a href="mailto:tom@jboss.org">Tom Elrod</a>
 */
public class RMIConnector implements JMXConnector, Serializable
{
   private static final long serialVersionUID = 817323035842634473l;

   private JMXServiceURL jmxServiceURL = null;
   private RMIServer rmiServer = null;
   private Map environment = Collections.EMPTY_MAP;

   //TODO: -TME -Need to figure out what needs to be transient and add read/writeObject methods

   private transient boolean isConnected = false;
   private transient boolean isClosed = false;

   private transient RMIConnection rmiConnection = null;
   private transient ClassLoader defaultClassLoader = null;
   private transient ConnectionNotifier connectionNotifier = null;
   private transient ClientNotifier clientNotifier = null;

   protected transient static Logger log = Logger.getLogger(RMIConnector.class.getName());

   public RMIConnector(JMXServiceURL url, Map environment)
   {
      if(url == null)
      {
         throw new IllegalArgumentException("JMXServiceURL can not be null.");
      }

      this.jmxServiceURL = url;
      if(environment != null)
      {
         this.environment = Collections.unmodifiableMap(environment);
      }
      init();
   }

   public RMIConnector(RMIServer rmiServer, Map environment)
   {
      if(rmiServer != null)
      {
         this.rmiServer = rmiServer;
      }
      else
      {
         throw new IllegalArgumentException("RMIServer can not be null.");
      }
      if(environment != null)
      {
         this.environment = Collections.unmodifiableMap(environment);
      }
      init();
   }

   private void init()
   {
      if(connectionNotifier == null)
      {
         connectionNotifier = new ConnectionNotifier(this);
      }
   }

   public void connect() throws IOException
   {
      connect(null);
   }

   public void connect(Map env) throws IOException
   {
      synchronized(this)
      {
         if(isClosed)
         {
            throw new IOException("Can not connect RMIConnector because has already been closed.");
         }
         if(isConnected)
         {
            return;
         }

         // map from parameter will override one from constructor
         Map connectEnv = new HashMap(this.environment);
         if(env != null)
         {
            connectEnv.putAll(env);
         }

         // need to set the default classloader
         setDefaultClassLoader(connectEnv);

         // get rmi stub if not already set
         if(rmiServer == null)
         {
            Hashtable lookupEnv = new Hashtable(connectEnv);
            String path = jmxServiceURL.getURLPath();
            if(path.startsWith("/jndi/") || path.startsWith(";jndi/"))
            {
               String lookupPath = path.substring("/jndi/".length());
               Object stub = null;
               InitialContext ctx = null;
               try
               {
                  ctx = new InitialContext(lookupEnv);
                  stub = ctx.lookup(lookupPath);
               }
               catch(NamingException e)
               {
                  log.error("Error looking up rmi server from jndi with url path of " + path, e);
                  throw new IOException("Error looking up rmi server stub within jndi.  " + e.getMessage());
               }
               finally
               {
                  if(ctx != null)
                  {
                     try
                     {
                        ctx.close();
                     }
                     catch(NamingException e)
                     {
                        log.warn("Error closing jndi context.  " + e.getMessage());
                     }
                  }
               }
               //TODO: -TME -Need to add support for IIOP narrowing
               rmiServer = (RMIServer) stub;

            }
            else if(path.startsWith("/stub/") || path.startsWith(";stub/"))
            {
               // get stub path and decode
               String encodedPath = path.substring("/stub/".length());
               byte[] decodedPath = Base64.decode(encodedPath);

               ByteArrayInputStream bin = new ByteArrayInputStream(decodedPath);

               ObjectInputStream ois = new ObjectInputStreamWithClassLoader(bin, defaultClassLoader);
               try
               {
                  Object obj = ois.readObject();
                  rmiServer = (RMIServer) PortableRemoteObject.narrow(obj, RMIServer.class);
               }
               catch(ClassNotFoundException e)
               {
                  log.error("Could not find class " + e.getMessage() + " when loading stub.", e);
                  throw new MalformedURLException("Could not find class " + e.getMessage() + " when loading stub.");
               }
               catch(ClassCastException cce)
               {
                  log.error("Could not convert stub to RMIServer type.", cce);
                  throw new MalformedURLException("Could not convert stub to RMIServer type.");
               }
               finally
               {
                  if(ois != null)
                  {
                     ois.close();
                  }
               }
            }
            else
            {
               //TODO: -TME -Need to add support for iiop lookup
               throw new MalformedURLException("JMXServiceURL does not contain recognizable lookup path.");
            }
         }

         // should now have stub, either via constructor or lookup
         Object credentials = connectEnv.get(JMXConnector.CREDENTIALS);
         rmiConnection = rmiServer.newClient(credentials);

         isConnected = true;

         clientNotifier = new ClientNotifier(rmiConnection);

         //TODO: -TME Start heartbeat agent (JBREM-75)

         connectionNotifier.fireConnectedNotification();
      }
   }

   /**
    * Will set the default classloader by either taking the jmx.remote.default.class.loader value from
    * either the map passed during connect or if not present, the one from when this object was created,
    * or if that is not present, then from the current thread.
    *
    * @param lookupEnv
    */
   private void setDefaultClassLoader(Map lookupEnv)
   {
      // get classloader to use
      defaultClassLoader = Thread.currentThread().getContextClassLoader();
      Object loaderInstance = lookupEnv.get(JMXConnectorServerFactory.DEFAULT_CLASS_LOADER);

      if(loaderInstance != null && loaderInstance instanceof ClassLoader)
      {
         defaultClassLoader = (ClassLoader) loaderInstance;
      }
   }

   public MBeanServerConnection getMBeanServerConnection() throws IOException
   {
      return getMBeanServerConnection(null);
   }

   public MBeanServerConnection getMBeanServerConnection(Subject delegationSubject) throws IOException
   {
      if(!isConnected || isClosed)
      {
         throw new IOException("Connector is not connected.");
      }

      //TODO: -TME: -Can probably store the clientConnection in a map per subject so that
      // can just return the same client connection if asked for by the same client instead
      // of having to create a new one each time.
      MBeanServerConnection clientConnection = new ClientMBeanServerConnection(rmiConnection,
                                                                               clientNotifier,
                                                                               defaultClassLoader,
                                                                               delegationSubject);
      return clientConnection;
   }

   public void close() throws IOException
   {
      synchronized(this)
      {
         if(isClosed)
         {
            return;
         }
         isClosed = true;
         isConnected = false;

         //TODO: -TME Will need to clean up heartbeat (JBREM-75) that will be added later on

         clientNotifier.close();

         if(rmiConnection != null)
         {
            rmiConnection.close();
         }

         rmiConnection = null;
         rmiServer = null;

         connectionNotifier.fireClosedNotification();
      }
   }

   public String getConnectionId() throws IOException
   {
      if(rmiConnection != null)
      {
         return rmiConnection.getConnectionId();
      }
      else
      {
         return null;
      }
   }

   public void addConnectionNotificationListener(NotificationListener listener,
                                                 NotificationFilter filter,
                                                 Object handback)
   {
      connectionNotifier.addNotificationListener(listener, filter, handback);
   }

   public void removeConnectionNotificationListener(NotificationListener listener) throws ListenerNotFoundException
   {
     connectionNotifier.removeNotificationListener(listener);
   }

   public void removeConnectionNotificationListener(NotificationListener listener,
                                                    NotificationFilter filter,
                                                    Object handback)
         throws ListenerNotFoundException
   {
      connectionNotifier.removeNotificationListener(listener, filter, handback);
   }

   public String toString()
   {
      StringBuffer buffer = new StringBuffer(getClass().getName() + " for ");
      buffer.append("JMXServiceURL: " + jmxServiceURL.toString());
      return buffer.toString();
   }

   // the following were added since need to initialize the connection notifier if passed over wire since
   // the connection notifier just extends the notification broadcaster support, which is not itself serializable.
   private void readObject(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException
   {
      objectInputStream.defaultReadObject();
      init();
   }

   private void writeObject(ObjectOutputStream objectOutputStream) throws IOException
   {
      objectOutputStream.defaultWriteObject();
   }

}