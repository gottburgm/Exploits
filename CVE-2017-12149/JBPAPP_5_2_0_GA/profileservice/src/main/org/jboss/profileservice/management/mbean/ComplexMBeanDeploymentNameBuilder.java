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

import java.util.List;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jboss.profileservice.spi.MBeanDeploymentNameBuilder;

/**
 * An MBeanDeploymentNameBuilder that supports multiple keys, prefix, suffix
 * and separator notions
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 88974 $
 */
public class ComplexMBeanDeploymentNameBuilder
   implements MBeanDeploymentNameBuilder
{
   private String prefix = "";
   private String suffix = "";
   private String separator = "";
   private List<String> keyNames;
   public String getPrefix()
   {
      return prefix;
   }
   public void setPrefix(String prefix)
   {
      this.prefix = prefix;
   }
   public String getSuffix()
   {
      return suffix;
   }
   public void setSuffix(String suffix)
   {
      this.suffix = suffix;
   }
   public String getSeparator()
   {
      return separator;
   }
   public void setSeparator(String separator)
   {
      this.separator = separator;
   }
   public List<String> getKeyNames()
   {
      return keyNames;
   }
   public void setKeyNames(List<String> keyNames)
   {
      this.keyNames = keyNames;
   }

   public String getName(ObjectName name, MBeanServer server)
   {
      StringBuilder tmp = new StringBuilder(prefix);
      for(String key : keyNames)
      {
         String value = name.getKeyProperty(key);
         tmp.append(value);
         if(separator != null)
            tmp.append(separator);
      }
      // Remove the last separator
      if(separator != null)
         tmp.setLength(tmp.length() - separator.length());
      return tmp.toString();
   }
}
