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

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.security.Principal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.management.MBeanServer;
import javax.management.remote.JMXAuthenticator;
import javax.management.remote.JMXConnectorServer;
import javax.security.auth.Subject;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:tom@jboss.org">Tom Elrod</a>
 */
public abstract class RMIServerImpl implements RMIServer
{
   private MBeanServer mbeanServer = null;
   private Map environment = null;
   private ClassLoader defaultClassLoader = null;
   private RMIConnectorServer connectorServer = null;
   private Map clientConnections = new HashMap();

   private static int connectionIdNumber = 0;

   private boolean isRunning = true;

   protected static Logger log = Logger.getLogger(RMIServerImpl.class.getName());

   public RMIServerImpl(Map env)
   {
      this.environment = env;
   }

   protected abstract void export() throws IOException;

   public abstract Remote toStub() throws IOException;

   protected void setRMIConnectorServer(RMIConnectorServer connectorServer)
   {
      this.connectorServer = connectorServer;
   }

   public void setDefaultClassLoader(ClassLoader cl)
   {
      this.defaultClassLoader = cl;
   }

   public ClassLoader getDefaultClassLoader()
   {
      return defaultClassLoader;
   }

   public void setMBeanServer(MBeanServer mbs)
   {
      this.mbeanServer = mbs;
   }

   public MBeanServer getMBeanServer()
   {
      return mbeanServer;
   }

   public String getVersion() throws RemoteException
   {
      return "1.0 JBoss_JMX_Remoting_1_0_1";
   }

   public RMIConnection newClient(Object credentials) throws IOException, SecurityException
   {
      if(!isRunning)
      {
         throw new IOException("RMIServer has been closed.");
      }

      RMIConnection client = null;

      try
      {
         // authenticate caller based on credentials
         Subject subject = authenticateCaller(environment, credentials);

         String connectionId = gernerateConnectionId(subject);

         client = makeClient(connectionId, subject);

         WeakReference clientRef = new WeakReference(client);
         synchronized(clientConnections)
         {
            clientConnections.put(connectionId, clientRef);
         }

         connectorServer.connectionOpened(connectionId, "Connection opened for client: " + client, null);
      }
      catch(Exception e)
      {
         if(e instanceof IOException)
         {
            throw (IOException) e;
         }
         else if(e instanceof RuntimeException)
         {
            throw (RuntimeException) e;
         }
         else
         {
            log.error("Error creating new client (RMIConnection).", e);
            throw new IOException("Error creating new RMIConnection.  " + e.getMessage());
         }
      }

      return client;
   }

   private Subject authenticateCaller(Map environment, Object credentials)
   {
      JMXAuthenticator authenticator = (JMXAuthenticator) environment.get(JMXConnectorServer.AUTHENTICATOR);

      Subject subject = null;
      if(authenticator != null)
      {
         subject = authenticator.authenticate(credentials);
      }

      return subject;
   }

   private String gernerateConnectionId(Subject subject)
   {
      // see javax.management.remote package doc for how to form the connection id.
      String protocol = getProtocol();

      StringBuffer connectionId = new StringBuffer(protocol + ":");
      try
      {
         String clientHost = RemoteServer.getClientHost();
         if(clientHost != null && clientHost.length() > 0)
         {
            // now check if is ipv6 address
            if(clientHost.indexOf(':') != -1)
            {
               connectionId.append("//[" + clientHost + "]");
            }
            else
            {
               connectionId.append("//" + clientHost);
            }
            // can not get port, so can not add it
         }
      }
      catch(ServerNotActiveException e)
      {
         log.warn("Can not get client host for connection id.  " + e.getMessage());
      }

      connectionId.append(" ");

      // per spec, "The ClientId is the identity of the client entity,
      // typically a string returned by JMXPrincipal.getName().
      // This string must not contain spaces."
      if(subject != null)
      {
         Set principals = subject.getPrincipals();
         Iterator itr = principals.iterator();
         String firstName = itr.hasNext() ? ((Principal) itr.next()).getName().replace(' ', '_').replace(';', ':').trim() : null;
         if(firstName != null)
         {
            connectionId.append(firstName);
         }
         while(itr.hasNext())
         {
            connectionId.append(";");
            Principal principal = (Principal) itr.next();
            String name = principal.getName().replace(' ', '_').replace(';', ':').trim();
            connectionId.append(name);
         }
         connectionId.append(" ");
      }

      connectionId.append(getNextConnectionIdNumber());

      return connectionId.toString();

   }

   private static synchronized int getNextConnectionIdNumber()
   {
      return connectionIdNumber++;
   }

   protected abstract RMIConnection makeClient(String connectionId, Subject subject) throws IOException;

   protected abstract void closeClient(RMIConnection client) throws IOException;

   protected abstract String getProtocol();

   protected void clientClosed(RMIConnection client) throws IOException
   {
      if(client != null)
      {
         String connectionID = client.getConnectionId();
         RMIConnection rmiConnection = null;
         synchronized(clientConnections)
         {
            WeakReference clientRef = (WeakReference) clientConnections.remove(connectionID);
            if(clientRef != null)
            {
               rmiConnection = (RMIConnection) clientRef.get();
            }
         }
         closeClient(client);
         connectorServer.connectionClosed(client.getConnectionId(), "Connection closed for client: " + client, null);
      }

   }

   public void close() throws IOException
   {
      isRunning = false;
      try
      {
         closeServer();
      }
      finally
      {
         closeClientConnections();
      }
   }

   private void closeClientConnections()
   {
      synchronized(clientConnections)
      {
         Set clients = clientConnections.entrySet();
         Iterator itr = clients.iterator();
         while(itr.hasNext())
         {
            WeakReference clientRef = (WeakReference) itr.next();
            if(clientRef != null)
            {
               RMIConnection rmiConnection = (RMIConnection) clientRef.get();
               if(rmiConnection != null)
               {
                  try
                  {
                     rmiConnection.close();
                  }
                  catch(IOException e)
                  {
                     e.printStackTrace();
                  }
               }
            }
         }
      }
   }

   protected abstract void closeServer() throws IOException;


}