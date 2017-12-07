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
package org.jboss.resource.deployment;

import java.util.Properties;

import javax.management.ObjectName;
import javax.naming.InitialContext;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.resource.metadata.AdminObjectMetaData;
import org.jboss.resource.metadata.ConnectorMetaData;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.util.naming.Util;

/**
 * An admin object deployment
 *
 * @author  <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 76129 $
 */
public class AdminObject extends ServiceMBeanSupport implements AdminObjectMBean
{
   /** The resource adapter name */
   protected ObjectName rarName;

   /** The admin object type */
   protected String type;
   
   /** The properties */
   protected Properties properties;
   
   /** The jndi name */
   protected String jndiName;

   public String getJNDIName()
   {
      return jndiName;
   }

   public void setJNDIName(String jndiName)
   {
      this.jndiName = jndiName;
   }

   public Properties getProperties()
   {
      return properties;
   }

   public void setProperties(Properties properties)
   {
      this.properties = properties;
   }

   public ObjectName getRARName()
   {
      return rarName;
   }

   public void setRARName(ObjectName rarName)
   {
      this.rarName = rarName;
   }

   public String getType()
   {
      return type;
   }

   public void setType(String type)
   {
      this.type = type;
   }
   
   protected void startService() throws Exception
   {
      AdminObjectMetaData aomd = retrieveAdminObjectMetaData();
      if (aomd == null)
         throw new DeploymentException("No admin object metadata type=" + type + " ra=" + rarName);

      Object adminObject = createAdminObject(aomd);
      
      bind(adminObject);
   }
   
   protected void stopService() throws Exception
   {
      unbind();
   }

   /**
    * Retrieve the admin object metadata
    * 
    * @return the admin object metadata
    * @throws DeploymentException for any error
    */
   protected AdminObjectMetaData retrieveAdminObjectMetaData() throws DeploymentException
   {
      try
      {
         ConnectorMetaData cmd = (ConnectorMetaData) server.getAttribute(rarName, "MetaData");
         return cmd.getAdminObject(type);
      }
      catch (Throwable t)
      {
         DeploymentException.rethrowAsDeploymentException("Error retrieving admin object metadata type=" + type + " ra=" + rarName, t);
         return null; // unreachable
      }
   }

   /**
    * Create the admin object
    * 
    * @param aomd the admin object metadata
    * @return the admin object
    * @throws DeploymentException for any error
    */
   protected Object createAdminObject(AdminObjectMetaData aomd) throws DeploymentException
   {
      try
      {
         return AdminObjectFactory.createAdminObject(jndiName, rarName, aomd, properties);
      }
      catch (Throwable t)
      {
         DeploymentException.rethrowAsDeploymentException("Error creating admin object metadata type=" + type + " ra=" + rarName, t);
         return null; // unreachable
      }
   }

   /**
    * Bind the object into jndi
    * 
    * @param object the object to bind
    * @throws Exception for any error
    */
   protected void bind(Object object) throws Exception
   {
      InitialContext ctx = new InitialContext();
      try
      {
         Util.bind(ctx, jndiName, object);
         log.info("Bound admin object '" + object.getClass().getName() + "' at '" + jndiName + "'");
      }
      finally
      {
         ctx.close();
      }
   }

   /**
    * Unbind the object from jndi
    * 
    * @throws Exception for any error
    */
   protected void unbind() throws Exception
   {
      InitialContext ctx = new InitialContext();
      try
      {
         Util.unbind(ctx, jndiName);
         log.info("Unbound admin object at '" + jndiName + "'");
      }
      finally
      {
         ctx.close();
      }
   }
}
