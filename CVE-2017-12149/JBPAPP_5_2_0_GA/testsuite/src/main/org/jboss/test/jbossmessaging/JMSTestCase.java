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

import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import javax.naming.NameClassPair;

import org.objectweb.jtests.jms.admin.Admin;
import org.objectweb.jtests.jms.admin.AdminFactory;

import org.jboss.util.NestedRuntimeException;
import org.jboss.test.JBossTestCase;

/**
 * JMSTestCase. A base test case for all JMS generic test cases.
 * 
 * Warning: If you override setUp() or tearDown(), rememeber to call the
 * superclass versions of these methods within your overriding methods.
 * AbstractTestCase uses setUp() and tearDown() to initialise logging.
 * 
 * @author <a href="richard.achmatowicz@jboss.com">Richard Achmatowicz</a>
 * @version $Revision: 85945 $
 */
public class JMSTestCase extends JBossTestCase
{
   /** JBM provider properties resource name */
   private static String PROP_FILE_NAME = "jbossmessaging/provider.properties";

   private static String PROP_NAME = "jms.provider.resources.dir";

   protected Admin admin;

   /**
    * Constructor for JMSTestCase object
    * 
    * @param name
    *           test case name
    */
   public JMSTestCase(String name)
   {
      super(name);
   }

   /**
    * Create the Admin object to perform all JMS adminsitrative functions in a
    * JMS provider-independent manner
    */
   protected void setUp() throws Exception
   {
      // perform any setUp required in the superclass
      super.setUp();

      try
      {
         log.info("setting up Admin");
         // get the Admin implementation for the current JMS provider
         // specified in provider.properties
         Properties props = getProviderProperties();
         admin = AdminFactory.getAdmin(props);
      }
      catch (Exception e)
      {
         throw new NestedRuntimeException("getAdmin() operation failed", e);
      }
   }

   /**
    * Create a JMS Queue.
    * 
    * The Queue is created dynamically, in a JMS provider-specific manner,
    * according to the instance of the Admin interface currently in use.
    * 
    * @param name
    *           The name of the Queue to be created.
    */
   public void createQueue(String name)
   {
      try
      {
         admin.createQueue(name);
      }
      catch (Exception e)
      {
         throw new NestedRuntimeException("createQueue() operation failed", e);
      }
   }

   /**
    * Delete a JMS Queue.
    * 
    * The Queue is deleted dynamically, in a JMS provider-specific manner,
    * according to the instance of the Admin interface currently in use.
    * 
    * @param name
    *           The name of the Queue to be deleted.
    */
   public void deleteQueue(String name)
   {
      try
      {
         admin.deleteQueue(name);
      }
      catch (Exception e)
      {
         throw new NestedRuntimeException("deleteQueue() operation failed", e);
      }
   }

   /**
    * Create a JMS Topic.
    * 
    * The Topic is created dynamically, in a JMS provider-specific manner,
    * according to the instance of the Admin interface currently in use.
    * 
    * @param name
    *           The name of the Topic to be created.
    */
   public void createTopic(String name)
   {
      try
      {
         admin.createTopic(name);
      }
      catch (Exception e)
      {
         throw new NestedRuntimeException("createTopic() operation failed", e);
      }
   }

   /**
    * Delete a JMS Topic.
    * 
    * The Topic is deleted dynamically, in a JMS provider-specific manner,
    * according to the instance of the Admin interface currently in use.
    * 
    * @param name
    *           The name of the Topic to be deleted.
    */
   public void deleteTopic(String name)
   {
      try
      {
         admin.deleteTopic(name);
      }
      catch (Exception e)
      {
         throw new NestedRuntimeException("deleteTopic() operation failed", e);
      }
   }

   /**
    * Create a JMS ConnectionFactory.
    * 
    * The ConnectionFactory is created dynamically, in a JMS provider-specific
    * manner, according to the instance of the Admin interface currently in use.
    * 
    * @param name
    *           The name of the ConnectionFactory to be created.
    */
   public void createConnectionFactory(String name)
   {
      try
      {
         admin.createConnectionFactory(name);
      }
      catch (Exception e)
      {
         throw new NestedRuntimeException(
               "createConnectionFactory() operation failed", e);
      }
   }

   /**
    * Delete a JMS ConnectionFactory.
    * 
    * The ConnectionFactory is deleted dynamically, in a JMS provider-specific
    * manner, according to the instance of the Admin interface currently in use.
    * 
    * @param name
    *           The name of the ConnectionFactory to be deleted.
    */
   public void deleteConnectionFactory(String name)
   {
      try
      {
         admin.deleteConnectionFactory(name);
      }
      catch (Exception e)
      {
         throw new NestedRuntimeException(
               "deleteConnectionFactory() operation failed", e);
      }
   }

   protected void dumpJNDIContext(String context)
   {
      try
      {
         log.info("Dumping JNDI context:" + context);

         // dump out the context name-value bindings
         InitialContext ic = getInitialContext();
         NamingEnumeration list = ic.list(context);

         while (list.hasMore())
         {
            NameClassPair nc = (NameClassPair) list.next();
            log.info(nc.toString());
         }
         log.info("Dumped JNDI context");
      }
      catch (Exception e)
      {
         throw new NestedRuntimeException("error dumping JNDI context", e);
      }
   }

   public static Properties getProviderProperties()
      throws IOException
   {
      Properties props = new Properties();
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      URL propsURL = loader.getResource(PROP_FILE_NAME);
      System.err.println("using provider.properties: " + propsURL);
      props.load(propsURL.openStream());
      return props;      
   }

   /**
    * Given a resource file name, prepend a directory qualifier to that name,
    * according to the instance of the JMS provider currently in use.
    * 
    * The directory name prepended is determined by the value of the property
    * jms.provider.resources.dir in the provider.properties resources file.
    * 
    * @param name
    *           The name of the resources file.
    */
   public static String getJMSResourceRelativePathname(String name)
   {
      // return the resource name with directory prepended
      return "jbossmessaging/" + name;
   }
}
