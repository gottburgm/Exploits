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
package org.jboss.test.jbossmessaging.test.joram;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.logging.Logger;
import org.jboss.test.jms.JMSTestAdmin;
import org.jboss.util.NestedRuntimeException;
import org.objectweb.jtests.jms.admin.Admin;

/**
 * JBossMessagingAdmin.
 * 
 * @author <a href="richard.achmatowicz@jboss.com">Richard Achmatowicz</a>
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision$
 */
public class JoramTestAdminDelegate implements Admin
{
   private Logger log = Logger.getLogger(JoramTestAdminDelegate.class);

   private InitialContext initialContext;
   private MBeanServerConnection server;
   
   protected static final ObjectName namingService;
   
   static
   {
      try
      {
         namingService = new ObjectName("jboss:service=Naming");
      }
      catch (Exception e)
      {
         throw new NestedRuntimeException(e);
      }
   }

   public JoramTestAdminDelegate(Class<?> clazz) throws Exception
   {
      this();
   }

   public JoramTestAdminDelegate() throws Exception
   {
      try
      {
         log.info("Initializing...");

         // set up the initial naming service context
         initialContext = new InitialContext();

         // set up the MBean server connection
         String adaptorName = System.getProperty("jbosstest.server.name", "jmx/invoker/RMIAdaptor");
         server = (MBeanServerConnection) initialContext.lookup(adaptorName);

      }
      catch (Exception e)
      {
         throw new NestedRuntimeException(e);
      }
   }

   public String getName()
   {
      return this.getClass().getName();
   }

   private MBeanServerConnection getServer()
   {
      return server;
   }

   public InitialContext createInitialContext() throws NamingException
   {
      return initialContext;
   }

   public void createQueue(String name)
   {
      try
      {
         JMSTestAdmin.getAdmin().createQueue(name, new String[]{name});
      }
      catch (Exception e)
      {
         throw new NestedRuntimeException(e);
      }
   }

   public void deleteQueue(String name)
   {
      try
      {
         JMSTestAdmin.getAdmin().deleteQueue(name);
      }
      catch (Exception e)
      {
         throw new NestedRuntimeException(e);
      }
   }

   public void createTopic(String name)
   {
      try
      {
         JMSTestAdmin.getAdmin().createTopic(name, new String[]{name});
      }
      catch (Exception e)
      {
         e.printStackTrace();
         throw new NestedRuntimeException(e);
      }
   }

   public void deleteTopic(String name)
   {
      try
      {
         JMSTestAdmin.getAdmin().deleteTopic(name);
      }
      catch (Exception e)
      {
         throw new NestedRuntimeException(e);
      }
   }

   public void createConnectionFactory(String name)
   {
      try
      {
         MBeanServerConnection server = getServer();
         server.invoke(namingService, "createAlias", new Object[] { name, "ConnectionFactory" }, new String[] { String.class.getName(), String.class.getName() });
      }
      catch (Exception e)
      {
    	  log.warn("naming = " + namingService);
    	  log.warn(e.getMessage(), e);

         throw new NestedRuntimeException(e);
      }
   }

   public void deleteConnectionFactory(String name)
   {
      try
      {
         MBeanServerConnection server = getServer();
         server.invoke(namingService, "removeAlias", new Object[] { name }, new String[] { String.class.getName() });
      }
      catch (Exception e)
      {
         throw new NestedRuntimeException(e);
      }
   }

   public void createQueueConnectionFactory(String name)
   {
      createConnectionFactory(name);
   }

   public void deleteQueueConnectionFactory(String name)
   {
      deleteConnectionFactory(name);
   }

   public void createTopicConnectionFactory(String name)
   {
      createConnectionFactory(name);
   }

   public void deleteTopicConnectionFactory(String name)
   {
      deleteConnectionFactory(name);
   }

}