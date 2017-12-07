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
package org.jboss.jms.jndi;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.jboss.deployment.DeploymentException;
import org.jboss.system.ServiceMBeanSupport;

/**
 * A JMX service to load a JMSProviderAdapter and register it.
 *
 * @author  <a href="mailto:cojonudo14@hotmail.com">Hiram Chirino</a>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 81030 $
 */
public class JMSProviderLoader extends ServiceMBeanSupport implements JMSProviderLoaderMBean
{
   /** The provider adapter which we are loading. */
   protected JMSProviderAdapter providerAdapter;

   /** The properties */
   protected Properties properties;

   /** The provider name. */
   protected String providerName;

   /** The provider adapter classname. */
   protected String providerAdapterClass;

   /** The factory jndi name. */
   protected String factoryRef;

   /** The queue factory jndi name. */
   protected String queueFactoryRef;

   /** The topic factory jndi name. */
   protected String topicFactoryRef;

   /** The JNDI name to bind the adapter to. */
   protected String jndiName;

   public void setProviderName(String name)
   {
      this.providerName = name;
   }

   public String getProviderName()
   {
      return providerName;
   }

   public void setProviderAdapterClass(String clazz)
   {
      providerAdapterClass = clazz;
   }

   public String getProviderAdapterClass()
   {
      return providerAdapterClass;
   }

   public void setProperties(final Properties properties)
   {
      this.properties = properties;
   }

   public Properties getProperties()
   {
      return properties;
   }

   public void setAdapterJNDIName(final String name)
   {
      this.jndiName = name;
   }

   public String getAdapterJNDIName()
   {
      return jndiName;
   }

   public void setFactoryRef(final String newFactoryRef)
   {
      factoryRef = newFactoryRef;
   }

   public void setQueueFactoryRef(final String newQueueFactoryRef)
   {
      queueFactoryRef = newQueueFactoryRef;
   }

   public void setTopicFactoryRef(final String newTopicFactoryRef)
   {
      topicFactoryRef = newTopicFactoryRef;
   }

   public String getFactoryRef()
   {
      return factoryRef;
   }

   public String getQueueFactoryRef()
   {
      return queueFactoryRef;
   }

   public String getTopicFactoryRef()
   {
      return topicFactoryRef;
   }

   public String getName()
   {
      return providerName;
   }

   protected void startService() throws Exception
   {
      // validate the configuration
      if (queueFactoryRef == null)
         throw new DeploymentException("missing required attribute: QueueFactoryRef");

      if (topicFactoryRef == null)
         throw new DeploymentException("missing required attribute: TopicFactoryRef");

      Class cls = Thread.currentThread().getContextClassLoader().loadClass(providerAdapterClass);
      providerAdapter = (JMSProviderAdapter) cls.newInstance();
      providerAdapter.setName(providerName);
      providerAdapter.setProperties(properties);
      providerAdapter.setFactoryRef(factoryRef);
      providerAdapter.setQueueFactoryRef(queueFactoryRef);
      providerAdapter.setTopicFactoryRef(topicFactoryRef);

      InitialContext context = new InitialContext();
      try
      {
         // Bind in JNDI
         if (jndiName == null)
         {
            String name = providerAdapter.getName();
            jndiName = "java:/" + name;
         }
         bind(context, jndiName, providerAdapter);
         log.debug("Bound adapter to " + jndiName);
      }
      finally
      {
         context.close();
      }
   }

   protected void stopService() throws Exception
   {
      InitialContext context = new InitialContext();

      try
      {
         // Unbind from JNDI
         String name = providerAdapter.getName();
         String jndiname = "java:/" + name;
         context.unbind(jndiname);
         log.debug("unbound adapter " + name + " from " + jndiname);
      }
      finally
      {
         context.close();
      }
   }

   private void bind(Context ctx, String name, Object val) throws NamingException
   {
      log.debug("attempting to bind " + val + " to " + name);

      // Bind val to name in ctx, and make sure that all
      // intermediate contexts exist
      Name n = ctx.getNameParser("").parse(name);
      while (n.size() > 1)
      {
         String ctxName = n.get(0);
         try
         {
            ctx = (Context) ctx.lookup(ctxName);
         }
         catch (NameNotFoundException e)
         {
            ctx = ctx.createSubcontext(ctxName);
         }
         n = n.getSuffix(1);
      }

      ctx.bind(n.get(0), val);
   }
}
