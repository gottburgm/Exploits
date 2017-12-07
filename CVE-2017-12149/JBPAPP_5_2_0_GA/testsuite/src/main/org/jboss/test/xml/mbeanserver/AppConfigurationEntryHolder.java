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
package org.jboss.test.xml.mbeanserver;

import java.util.HashMap;
import javax.security.auth.login.AppConfigurationEntry;
import javax.xml.namespace.QName;

import org.jboss.xb.binding.GenericValueContainer;

/**
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public class AppConfigurationEntryHolder
   implements GenericValueContainer
{
   String code;
   AppConfigurationEntry.LoginModuleControlFlag controlFlag;
   HashMap options = new HashMap();

   // GenericValueContainer should have default ctor
   public AppConfigurationEntryHolder()
   {
   }

   AppConfigurationEntryHolder(String code, String flag)
   {
      this.code = code;
      controlFlag = AppConfigurationEntry.LoginModuleControlFlag.REQUIRED;
      if (flag != null)
      {
         // Lower case is what is used by the jdk1.4.1 implementation
         flag = flag.toLowerCase();
         if (AppConfigurationEntry.LoginModuleControlFlag.REQUIRED.toString().indexOf(flag) > 0)
            controlFlag = AppConfigurationEntry.LoginModuleControlFlag.REQUIRED;
         else if (AppConfigurationEntry.LoginModuleControlFlag.REQUISITE.toString().indexOf(flag) > 0)
            controlFlag = AppConfigurationEntry.LoginModuleControlFlag.REQUISITE;
         else if (AppConfigurationEntry.LoginModuleControlFlag.SUFFICIENT.toString().indexOf(flag) > 0)
            controlFlag = AppConfigurationEntry.LoginModuleControlFlag.SUFFICIENT;
         else if (AppConfigurationEntry.LoginModuleControlFlag.OPTIONAL.toString().indexOf(flag) > 0)
            controlFlag = AppConfigurationEntry.LoginModuleControlFlag.OPTIONAL;
      }
   }

   public AppConfigurationEntry getEntry()
   {
      AppConfigurationEntry entry = new AppConfigurationEntry(code, controlFlag, options);
      return entry;
   }

   public void addOption(ModuleOption option)
   {
      options.put(option.name, option.value);
   }

   // GenericValueContainer impl

   public void addChild(QName name, Object value)
   {
      if("code".equals(name.getLocalPart()))
      {
         this.code = (String)value;
      }
      else if("flag".equals(name.getLocalPart()))
      {
         // Lower case is what is used by the jdk1.4.1 implementation
         String flag = ((String)value).toLowerCase();
         if (AppConfigurationEntry.LoginModuleControlFlag.REQUIRED.toString().indexOf(flag) > 0)
            controlFlag = AppConfigurationEntry.LoginModuleControlFlag.REQUIRED;
         else if (AppConfigurationEntry.LoginModuleControlFlag.REQUISITE.toString().indexOf(flag) > 0)
            controlFlag = AppConfigurationEntry.LoginModuleControlFlag.REQUISITE;
         else if (AppConfigurationEntry.LoginModuleControlFlag.SUFFICIENT.toString().indexOf(flag) > 0)
            controlFlag = AppConfigurationEntry.LoginModuleControlFlag.SUFFICIENT;
         else if (AppConfigurationEntry.LoginModuleControlFlag.OPTIONAL.toString().indexOf(flag) > 0)
            controlFlag = AppConfigurationEntry.LoginModuleControlFlag.OPTIONAL;
      }
      else if("module-option".equals(name.getLocalPart()))
      {
         addOption((ModuleOption)value);
      }
   }

   public Object instantiate()
   {
      return new AppConfigurationEntry(code, controlFlag, options);
   }

   public Class getTargetClass()
   {
      return AppConfigurationEntry.class; 
   }
}
