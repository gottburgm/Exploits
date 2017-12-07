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
package org.jboss.naming;

import javax.naming.InitialContext;

import org.jboss.deployment.DeploymentException;
import org.jboss.logging.Logger;
import org.jboss.system.ServiceMBeanSupport;

/**
 * An mbean used to construct a link ref pair
 * 
 * @author Adrian Brock (adrian@jboss.com)
 * @version $Revision: 62323 $
 */
public class LinkRefPairService extends ServiceMBeanSupport
   implements LinkRefPairServiceMBean
{
   /** This is a hack to make sure this class doesn't get garbage collected */
   protected static final Class HACK = LinkRefPairObjectFactory.class;
   
   static
   {
      // Make sure the LinkRefPairObjectFactory class is initialized
      Logger.getLogger(LinkRefPairService.class).debug("LinkRefPair guid=" + LinkRefPairObjectFactory.guid);
   }
   
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------

   /** The jndi binding */
   private String jndiName;

   /** The remote jndi binding */
   private String remoteJndiName;

   /** The local jndi binding */
   private String localJndiName;
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------
   
   // LinkRefPairServiceMBean implementation ------------------------

   public String getJndiName()
   {
      return jndiName;
   }

   public String getLocalJndiName()
   {
      return localJndiName;
   }

   public String getRemoteJndiName()
   {
      return remoteJndiName;
   }

   public void setJndiName(String jndiName)
   {
      this.jndiName = jndiName;
   }

   public void setLocalJndiName(String jndiName)
   {
      this.localJndiName = jndiName;
   }
   
   public void setRemoteJndiName(String jndiName)
   {
      this.remoteJndiName = jndiName;
   }
   
   // ServiceMBeanSupport overrides ---------------------------------
   
   protected void startService() throws Exception
   {
      if (jndiName == null)
         throw new DeploymentException("The jndiName is null for LinkRefPair " + getServiceName());
      if (remoteJndiName == null)
         throw new DeploymentException("The remoteJndiName is null for LinkRefPair " + getServiceName());
      if (localJndiName == null)
         throw new DeploymentException("The localJndiName is null for LinkRefPair " + getServiceName());

      LinkRefPair pair = new LinkRefPair(remoteJndiName, localJndiName);
      InitialContext ctx = new InitialContext();
      try
      {
         Util.bind(ctx, jndiName, pair);
      }
      finally
      {
         ctx.close();
      }
   }
   
   protected void stopService() throws Exception
   {
      LinkRefPair pair = new LinkRefPair(remoteJndiName, localJndiName);
      InitialContext ctx = new InitialContext();
      try
      {
         Util.unbind(ctx, jndiName);
      }
      finally
      {
         ctx.close();
      }
   }
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
}
