/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.resource.adapter.jms;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Set;

import javax.jms.ConnectionMetaData;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.security.auth.Subject;

import org.jboss.jms.jndi.JMSProviderAdapter;
import org.jboss.logging.Logger;

/**
 * Jms ManagedConectionFactory
 * 
 * @author <a href="mailto:peter.antman@tim.se">Peter Antman </a>.
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 76316 $
 */
public class JmsManagedConnectionFactory implements ManagedConnectionFactory
{
   private static final long serialVersionUID = -923483284031773011L;

   private static final Logger log = Logger.getLogger(JmsManagedConnection.class);

   /** Settable attributes in ra.xml */
   private JmsMCFProperties mcfProperties = new JmsMCFProperties();

   /** Whether we are strict */
   private boolean strict = true;

   /** For local access. */
   private JMSProviderAdapter adapter;
   
   /** The try lock */
   private int useTryLock = 60;

   public JmsManagedConnectionFactory()
   {
      // empty
   }

   /**
    * Create a "non managed" connection factory. No appserver involved
    */
   public Object createConnectionFactory() throws ResourceException
   {
      return createConnectionFactory(null);
   }

   /**
    * Create a ConnectionFactory with appserver hook
    */
   public Object createConnectionFactory(ConnectionManager cxManager) throws ResourceException
   {
      Object cf = new JmsConnectionFactoryImpl(this, cxManager);

      if (log.isTraceEnabled())
      {
         log.trace("Created connection factory: " + cf + ", using connection manager: " + cxManager);
      }

      return cf;
   }

   /**
    * Create a new connection to manage in pool
    */
   public ManagedConnection createManagedConnection(Subject subject, ConnectionRequestInfo info)
         throws ResourceException
   {
      boolean trace = log.isTraceEnabled();

      info = getInfo(info);
      if (trace)
         log.trace("connection request info: " + info);

      JmsCred cred = JmsCred.getJmsCred(this, subject, info);
      if (trace)
         log.trace("jms credentials: " + cred);

      // OK we got autentication stuff
      JmsManagedConnection mc = new JmsManagedConnection(this, info, cred.name, cred.pwd);

      if (trace)
         log.trace("created new managed connection: " + mc);

      return mc;
   }

   /**
    * Match a set of connections from the pool
    */
   public ManagedConnection matchManagedConnections(Set connectionSet, Subject subject, ConnectionRequestInfo info)
         throws ResourceException
   {
      boolean trace = log.isTraceEnabled();

      // Get cred
      info = getInfo(info);
      JmsCred cred = JmsCred.getJmsCred(this, subject, info);

      if (trace)
         log.trace("Looking for connection matching credentials: " + cred);

      // Traverse the pooled connections and look for a match, return first
      // found
      Iterator connections = connectionSet.iterator();

      while (connections.hasNext())
      {
         Object obj = connections.next();

         // We only care for connections of our own type
         if (obj instanceof JmsManagedConnection)
         {
            // This is one from the pool
            JmsManagedConnection mc = (JmsManagedConnection) obj;

            // Check if we even created this on
            ManagedConnectionFactory mcf = mc.getManagedConnectionFactory();

            // Only admit a connection if it has the same username as our
            // asked for creds

            // FIXME, Here we have a problem, jms connection
            // may be anonymous, have a user name

            if ((mc.getUserName() == null || (mc.getUserName() != null && mc.getUserName().equals(cred.name)))
                  && mcf.equals(this))
            {
               // Now check if ConnectionInfo equals
               if (info.equals(mc.getInfo()))
               {

                  if (trace)
                     log.trace("Found matching connection: " + mc);

                  return mc;
               }
            }
         }
      }

      if (trace)
         log.trace("No matching connection was found");

      return null;
   }

   public void setLogWriter(PrintWriter out) throws ResourceException
   {
      // 
      // jason: screw the logWriter stuff for now it sucks ass
      //
   }

   public PrintWriter getLogWriter() throws ResourceException
   {
      // 
      // jason: screw the logWriter stuff for now it sucks ass
      //

      return null;
   }

   /**
    * Checks for equality ower the configured properties.
    */
   public boolean equals(Object obj)
   {
      if (obj == null)
         return false;
      if (obj instanceof JmsManagedConnectionFactory)
      {
         return mcfProperties.equals(((JmsManagedConnectionFactory) obj).getProperties());
      }
      else
      {
         return false;
      }
   }

   public int hashCode()
   {
      return mcfProperties.hashCode();
   }

   // --- Connfiguration API ---

   public void setJmsProviderAdapterJNDI(String jndi)
   {
      mcfProperties.setProviderJNDI(jndi);
   }

   public String getJmsProviderAdapterJNDI()
   {
      return mcfProperties.getProviderJNDI();
   }

   /**
    * Set userName, null by default.
    */
   public void setUserName(String userName)
   {
      mcfProperties.setUserName(userName);
   }

   /**
    * Get userName, may be null.
    */
   public String getUserName()
   {
      return mcfProperties.getUserName();
   }

   /**
    * Set password, null by default.
    */
   public void setPassword(String password)
   {
      mcfProperties.setPassword(password);
   }

   /**
    * Get password, may be null.
    */
   public String getPassword()
   {
      return mcfProperties.getPassword();
   }

   /**
    * Get client id, may be null.
    */
   public String getClientID()
   {
      return mcfProperties.getClientID();
   }

   /**
    * Set client id, null by default.
    */
   public void setClientID(final String clientID)
   {
      mcfProperties.setClientID(clientID);
   }

   public boolean isStrict()
   {
      return strict;
   }

   public void setStrict(boolean strict)
   {
      this.strict = strict;
   }

   public void setStrict(Boolean strict)
   {
      this.strict = strict.booleanValue();
   }

   /**
    * Set the default session typ
    * 
    * @param type either javax.jms.Topic or javax.jms.Queue
    * 
    * @exception ResourceException if type was not a valid type.
    */
   public void setSessionDefaultType(String type) throws ResourceException
   {
      mcfProperties.setSessionDefaultType(type);
   }

   public String getSessionDefaultType()
   {
      return mcfProperties.getSessionDefaultType();
   }

   /**
    * For local access
    */
   public void setJmsProviderAdapter(final JMSProviderAdapter adapter)
   {
      this.adapter = adapter;
   }

   public JMSProviderAdapter getJmsProviderAdapter()
   {
      return adapter;
   }
   
   /**
    * Get the useTryLock.
    * 
    * @return the useTryLock.
    */
   public int getUseTryLock()
   {
      return useTryLock;
   }

   /**
    * Set the useTryLock.
    * 
    * @param useTryLock the useTryLock.
    */
   public void setUseTryLock(int useTryLock)
   {
      this.useTryLock = useTryLock;
   }

   private ConnectionRequestInfo getInfo(ConnectionRequestInfo info)
   {
      if (info == null)
      {
         // Create a default one
         return new JmsConnectionRequestInfo(mcfProperties);
      }
      else
      {
         // Fill the one with any defaults
         ((JmsConnectionRequestInfo) info).setDefaults(mcfProperties);
         return info;
      }
   }

   public ConnectionMetaData getMetaData()
   {
      return new JmsConnectionMetaData();
   }

   //---- MCF to MCF API

   protected JmsMCFProperties getProperties()
   {
      return mcfProperties;
   }
}
