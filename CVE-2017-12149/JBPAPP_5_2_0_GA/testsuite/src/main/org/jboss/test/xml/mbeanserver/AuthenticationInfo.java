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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Iterator;
import javax.security.auth.login.AppConfigurationEntry;

/**
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public class AuthenticationInfo
{
   private String name;
   private ArrayList loginModules = new ArrayList();

   public AuthenticationInfo()
   {
      //System.out.println("AuthenticationInfo");
   }
   public AuthenticationInfo(String name)
   {
      this.name = name;
      //System.out.println("AuthenticationInfo.ctor, name="+name);
   }

   public String getName()
   {
      return name;
   }
   public void setName(String name)
   {
      //System.out.println("AuthenticationInfo, name="+name);
      this.name = name;
   }

   /** Get a copy of the  application authentication configuration. This requires
    an AuthPermission("getLoginConfiguration") access.
    */
   public AppConfigurationEntry[] copyAppConfigurationEntry()
   {
      AppConfigurationEntry[] copy = new AppConfigurationEntry[loginModules.size()];
      for(int i = 0; i < copy.length; i ++)
      {
         AppConfigurationEntry entry = (AppConfigurationEntry) loginModules.get(i);
         copy[i] = new AppConfigurationEntry(entry.getLoginModuleName(),
                                entry.getControlFlag(), entry.getOptions());
      }
      return copy;
   }

   public void addAppConfigurationEntry(AppConfigurationEntry entry)
   {
      //System.out.println("AuthenticationInfo, addAppConfigurationEntry="+name);
      loginModules.add(entry);
   }
   /** Get an application authentication configuration. This requires an
    AuthPermission("getLoginConfiguration") access.
    */
   public AppConfigurationEntry[] getAppConfigurationEntry()
   {
      AppConfigurationEntry[] entries = new AppConfigurationEntry[loginModules.size()];
      loginModules.toArray(entries);
      return entries;
   }
   /** Set an application authentication configuration. This requires an
    AuthPermission("setLoginConfiguration") access.
    */
   public void setAppConfigurationEntry(AppConfigurationEntry[] loginModules)
   {
      this.loginModules.addAll(Arrays.asList(loginModules));
   }

   public String toString()
   {
      StringBuffer buffer = new StringBuffer("AppConfigurationEntry[]:\n");
      for(int i = 0; i < loginModules.size(); i ++)
      {
         AppConfigurationEntry entry = (AppConfigurationEntry) loginModules.get(i);
         buffer.append("["+i+"]");
         buffer.append("\nLoginModule Class: "+entry.getLoginModuleName());
         buffer.append("\nControlFlag: "+entry.getControlFlag());
         buffer.append("\nOptions:");
         Map options = entry.getOptions();
         Iterator iter = options.entrySet().iterator();
         while( iter.hasNext() )
         {
            Map.Entry e = (Map.Entry) iter.next();
            buffer.append("name="+e.getKey());
            buffer.append(", value="+e.getValue());
            buffer.append("\n");
         }
      }
      return buffer.toString();
   }

}
