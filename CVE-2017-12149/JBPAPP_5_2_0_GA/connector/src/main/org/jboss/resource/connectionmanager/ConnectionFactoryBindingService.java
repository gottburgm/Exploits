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
package org.jboss.resource.connectionmanager;

import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.resource.ResourceException;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.logging.Logger;
import org.jboss.managed.api.annotation.ManagementObjectID;
import org.jboss.managed.api.annotation.ManagementProperty;
import org.jboss.managed.api.annotation.ViewUse;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.util.naming.NonSerializableFactory;

/**
 * Handles the binding of the connection factory into jndi
 *
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @author <a href="mailto:weston.price@jboss.com">Weston Price</a>
 * @author Scott.Stark@jboss.org
 * @version $Revision: 76129 $
 */
public class ConnectionFactoryBindingService extends ServiceMBeanSupport 
   implements ConnectionFactoryBindingServiceMBean
{
   /** The logger */
   private static final Logger log = Logger.getLogger(ConnectionFactoryBindingService.class);
   
   /** The connection manager */
   protected ObjectName cm;
   
   /** The jndi name */
   protected String jndiName;
   
   /** The bind name */
   protected String bindName;
   
   /** Whether to use the java naming context */
   protected boolean useJavaContext = true;
   
   /** The connection factory */
   protected Object cf;
   
   protected void startService() throws Exception
   {
      determineBindName();
      createConnectionFactory();
      bindConnectionFactory();
   }
   
   protected void stopService() throws Exception
   {
      unbindConnectionFactory();
   }

   @ManagementProperty(use={ViewUse.RUNTIME})
   public ObjectName getConnectionManager()
   {
      return cm;
   }

   public void setConnectionManager(ObjectName cm)
   {
      this.cm = cm;
   }

   @ManagementProperty(use={ViewUse.RUNTIME})
   public String getBindName()
   {
      return bindName;
   }

   /**
    * The global JNDI name the factory is bound under. This is the ManagedObject
    * id that ties the mbean to the root ManagedConnectionFactoryDeploymentMetaData
    * based ManagedObject.
    */
   @ManagementObjectID(type="DataSource")
   public String getJndiName()
   {
      return jndiName;
   }

   public void setJndiName(String jndiName)
   {
      this.jndiName = jndiName;
   }

   public boolean isUseJavaContext()
   {
      return useJavaContext;
   }

   public void setUseJavaContext(boolean useJavaContext)
   {
      this.useJavaContext = useJavaContext;
   }

   /**
    * Determine the bind name
    */
   protected void determineBindName() throws Exception
   {
      bindName = jndiName;
      if( useJavaContext && jndiName.startsWith("java:") == false )
         bindName = "java:" + jndiName;
   }

   /**
    * Create the connection factory 
    */
   protected void createConnectionFactory() throws Exception
   {
      try
      {
         BaseConnectionManager2 bcm = (BaseConnectionManager2) server.getAttribute(cm, "Instance");
         BaseConnectionManager2.ConnectionManagerProxy cmProxy = new BaseConnectionManager2.ConnectionManagerProxy(bcm, cm);
         cf = bcm.getPoolingStrategy().getManagedConnectionFactory().createConnectionFactory(cmProxy);
      }
      catch (ResourceException re)
      {
         throw new DeploymentException("Could not create ConnectionFactory for adapter: " + cm, re);
      }
   }
   
   /**
    * Bind the connection factory into jndi
    */
   protected void bindConnectionFactory() throws Exception
   {
      InitialContext ctx = new InitialContext();
      try
      {
         log.debug("Binding object '" + cf + "' into JNDI at '" + bindName + "'");
         Name name = ctx.getNameParser("").parse(bindName);
         NonSerializableFactory.rebind(name, cf, true);
         log.info("Bound ConnectionManager '" + serviceName + "' to JNDI name '" + bindName + "'");
      }
      catch (NamingException ne)
      {
         throw new DeploymentException("Could not bind ConnectionFactory into jndi: " + bindName, ne);
      }
      finally
      {
         ctx.close();
      }
   }
   
   /**
    * Unbind the connection factory into jndi
    */
   protected void unbindConnectionFactory() throws Exception
   {
      InitialContext ctx = new InitialContext();
      try
      {
         ctx.unbind(bindName);
         NonSerializableFactory.unbind(bindName);
         log.info("Unbound ConnectionManager '" + serviceName + "' from JNDI name '" + bindName + "'");
      }
      catch (NamingException ne)
      {
         log.error("Could not unbind managedConnectionFactory from jndi: " + bindName, ne);
      }
      finally
      {
         ctx.close();
      }
   }
}
