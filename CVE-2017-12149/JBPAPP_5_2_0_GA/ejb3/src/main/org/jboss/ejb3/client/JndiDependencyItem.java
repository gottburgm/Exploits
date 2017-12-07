/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.ejb3.client;

import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;

import org.jboss.dependency.plugins.AbstractDependencyItem;
import org.jboss.dependency.spi.Controller;
import org.jboss.dependency.spi.ControllerContext;
import org.jboss.dependency.spi.ControllerState;
import org.jboss.dependency.spi.DependencyItem;
import org.jboss.logging.Logger;
import org.jboss.util.JBossStringBuilder;

/**
 * @author Scott.Stark@jboss.org
 * @version $Revision: 85945 $
 */
public class JndiDependencyItem extends AbstractDependencyItem
   implements DependencyItem
{
private static final Logger log = Logger.getLogger(JndiDependencyItem.class);

   /** The demand jndi name */
   private String jndiName;
   private Properties env;
   private String classLoaderName;

   JndiDependencyItem(String jndiName, Properties env, String classLoaderName)
   {
      this.jndiName = jndiName;
      this.env = env;
      this.classLoaderName = classLoaderName;
   }

   @Override
   public boolean resolve(Controller controller)
   {
      ClassLoader tcl = Thread.currentThread().getContextClassLoader();
      try
      {
         ControllerContext cc = controller.getContext(classLoaderName, ControllerState.INSTALLED);
         ClassLoader loader = (ClassLoader) cc.getTarget();
         if(loader != null)
            Thread.currentThread().setContextClassLoader(loader);
         Properties jndiEnv = env;
         if(jndiEnv == null)
            jndiEnv = System.getProperties();
         InitialContext ctx = new InitialContext(jndiEnv);
         Object depends = ctx.lookup(jndiName);
         super.setIDependOn(depends);
         log.info("Resolved("+jndiName+")"+depends);
         setResolved(true);
         return isResolved();
      }
      catch(NameNotFoundException e)
      {
         log.debug("Jndi lookup failed", e);
      }
      catch(Throwable ignored)
      {
         log.debug("Unexpected error", ignored);
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(tcl);            
      }
      setResolved(false);
      return isResolved();
   }

   @Override
   public void toString(JBossStringBuilder buffer)
   {
      super.toString(buffer);
      buffer.append(" depend=").append(jndiName);
   }
   
   @Override
   public void toShortString(JBossStringBuilder buffer)
   {
      buffer.append(getName()).append(" depend ").append(jndiName);
   }

   @Override
   public String toHumanReadableString()
   {
      StringBuilder builder = new StringBuilder();
      builder.append("JndiDepends: '");
      builder.append(jndiName);
      builder.append("'");
      return builder.toString();
   }
}
