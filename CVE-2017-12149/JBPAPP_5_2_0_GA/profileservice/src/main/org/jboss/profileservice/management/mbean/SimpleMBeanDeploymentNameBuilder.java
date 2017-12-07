/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.profileservice.management.mbean;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jboss.profileservice.spi.MBeanDeploymentNameBuilder;

/**
 * A simple MBeanDeploymentNameBuilder that returns the value of the key
 * property value as the name.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 88974 $
 */
public class SimpleMBeanDeploymentNameBuilder
   implements MBeanDeploymentNameBuilder
{
   private String keyName = "name";

   public String getKeyName()
   {
      return keyName;
   }
   public void setKeyName(String keyName)
   {
      this.keyName = keyName;
   }

   /*
    * Return the key property specified by the keyName as the deployment name.
    * @param name - the 
    */
   public String getName(ObjectName name, MBeanServer server)
   {
      return name.getKeyProperty(keyName);
   }
}
