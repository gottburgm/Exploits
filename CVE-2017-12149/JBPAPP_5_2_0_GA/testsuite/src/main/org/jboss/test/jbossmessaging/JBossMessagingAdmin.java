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
package org.jboss.test.jbossmessaging;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.logging.Logger;
import org.jboss.util.NestedRuntimeException;

import org.objectweb.jtests.jms.admin.Admin;

/**
 * JBossMessagingAdmin.
 * 
 * @author <a href="richard.achmatowicz@jboss.com">Richard Achmatowicz</a>
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public class JBossMessagingAdmin implements Admin
{
   private Logger log = Logger.getLogger(JBossMessagingAdmin.class);

   private InitialContext initialContext ;
   private MBeanServerConnection server ;

   protected static final String name ;
   protected static final ObjectName serverPeer;
   protected static final ObjectName namingService;
   
   static
   {
      try
      {
	 name =  JBossMessagingAdmin.class.getName() ;
         serverPeer = new ObjectName("jboss.messaging:service=ServerPeer");
         namingService = new ObjectName("jboss:service=Naming");
      }
      catch (Exception e)
      {
         throw new NestedRuntimeException(e);
      }
   }
   
   public JBossMessagingAdmin(Class clazz) throws Exception
   {
	   this();
   }
   public JBossMessagingAdmin() throws Exception
   {
      try {
	  log.info("Initializing...") ;

          // set up the initial naming service context
          initialContext = new InitialContext() ;

          // set up the MBean server connection
          String adaptorName = System.getProperty("jbosstest.server.name","jmx/invoker/RMIAdaptor") ;
          server = (MBeanServerConnection) initialContext.lookup(adaptorName) ;
	   
      } catch (Exception e) {
         throw new NestedRuntimeException(e);
      }
   }

   public String getName() {
      return name ;
   }

   private MBeanServerConnection getServer() {
       return server ;
   }

   public InitialContext createInitialContext() throws NamingException {
      return initialContext ;
   }

   public void createQueue(String name)
   {
      try
      {
         MBeanServerConnection server = getServer();
         try
         {
            server.invoke(serverPeer, "deployQueue", new Object[] { name, name },  new String[] { String.class.getName(), String.class.getName() } );
         }
         catch (Exception ignored)
         {
            log.trace("Ignored", ignored);
         }
         ObjectName queueName = new ObjectName("jboss.messaging.destination:service=Queue,name=" + name);
         server.invoke(queueName, "removeAllMessages", null, null);
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
         MBeanServerConnection server = getServer();
         ObjectName queueName = new ObjectName("jboss.messaging.destination:service=Queue,name=" + name);
         server.invoke(queueName, "removeAllMessages", null, null);
         server.invoke(serverPeer, "destroyQueue", new Object[] { name },  new String[] { String.class.getName() } );
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
         MBeanServerConnection server = getServer();
         try
         {
            server.invoke(serverPeer, "deployTopic", new Object[] { name, name },  new String[] { String.class.getName(), String.class.getName() } );
         }
         catch (Exception ignored)
         {
        	 ignored.printStackTrace();
         }
         ObjectName topicName = new ObjectName("jboss.messaging.destination:service=Topic,name=" + name);
         server.invoke(topicName, "removeAllMessages", null, null);
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
         MBeanServerConnection server = getServer();
         ObjectName topicName = new ObjectName("jboss.messaging.destination:service=Topic,name=" + name);
         server.invoke(topicName, "removeAllMessages", null, null);
         server.invoke(serverPeer, "destroyTopic", new Object[] { name },  new String[] { String.class.getName() } );
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
         server.invoke(namingService, "createAlias", new Object[] { name, "ConnectionFactory" },  new String[] { String.class.getName(), String.class.getName() } );
      }
      catch (Exception e)
      {
         throw new NestedRuntimeException(e);
      }
   }

   public void deleteConnectionFactory(String name)
   {
      try
      {
         MBeanServerConnection server = getServer();
         server.invoke(namingService, "removeAlias", new Object[] { name },  new String[] { String.class.getName() } );
      }
      catch (Exception e)
      {
         throw new NestedRuntimeException(e);
      }
   }

   public void createQueueConnectionFactory(String name)
   {
       createConnectionFactory(name) ;
   }

   public void deleteQueueConnectionFactory(String name)
   {
       deleteConnectionFactory(name) ;
   }

   public void createTopicConnectionFactory(String name)
   {
       createConnectionFactory(name) ;
   }

   public void deleteTopicConnectionFactory(String name)
   {
       deleteConnectionFactory(name) ;
   }

}
