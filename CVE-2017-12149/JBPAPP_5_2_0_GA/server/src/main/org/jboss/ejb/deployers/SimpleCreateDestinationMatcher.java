/*
* JBoss, Home of Professional Open Source
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
package org.jboss.ejb.deployers;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.metadata.ejb.jboss.JBossMessageDrivenBeanMetaData;

/**
 * SimpleCreateDestinationMatcher.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public class SimpleCreateDestinationMatcher implements CreateDestinationMatcher
{
   /** The message listener class */
   private String messageListener;

   /** The resource adapter name */
   private String rarName;

   /** Whether we are the default matcher */
   private boolean defaultMatcher;
   
   /**
    * Get the messageListener.
    * 
    * @return the messageListener.
    */
   public String getMessageListener()
   {
      return messageListener;
   }

   /**
    * Set the messageListener.
    * 
    * @param messageListener the messageListener.
    */
   public void setMessageListener(String messageListener)
   {
      this.messageListener = messageListener;
   }

   /**
    * Get the rarName.
    * 
    * @return the rarName.
    */
   public String getRarName()
   {
      return rarName;
   }

   /**
    * Set the rarName.
    * 
    * @param rarName the rarName.
    */
   public void setRarName(String rarName)
   {
      this.rarName = rarName;
   }

   /**
    * Get the defaultMatcher.
    * 
    * @return the defaultMatcher.
    */
   public boolean isDefault()
   {
      return defaultMatcher;
   }

   /**
    * Set the defaultMatcher.
    * 
    * @param defaultMatcher the defaultMatcher.
    */
   public void setDefault(boolean defaultMatcher)
   {
      this.defaultMatcher = defaultMatcher;
   }

   public boolean isMatch(DeploymentUnit unit, JBossMessageDrivenBeanMetaData mdb) throws DeploymentException
   {
      if (noMatch(messageListener, mdb.getMessagingType(), isDefault()))
         return false;
      if (noMatch(rarName, mdb.getResourceAdapterName(), isDefault()))
         return false;
      return true;
   }
   
   protected static boolean noMatch(String check, String parameter, boolean defaultMatch)
   {
      if (check != null)
      {
         if (parameter == null)
            return defaultMatch == false;
         parameter = parameter.trim();
         return check.equals(parameter) == false;
      }
      return false;
   }
}
