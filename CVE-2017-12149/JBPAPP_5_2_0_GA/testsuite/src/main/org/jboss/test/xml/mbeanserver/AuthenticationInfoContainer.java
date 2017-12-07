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

import javax.xml.namespace.QName;
import javax.security.auth.login.AppConfigurationEntry;

import org.jboss.xb.binding.GenericValueContainer;

/**
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public class AuthenticationInfoContainer
      implements GenericValueContainer
{
   AuthenticationInfo info;

   public void addChild(QName name, Object value)
   {
      //System.out.println("AuthenticationInfoContainer.addChild, name=" + name + ", value="+value);
      if("name".equals(name.getLocalPart()))
      {
         String infoName = (String)value;
         info = new AuthenticationInfo(infoName);
      }
      else if( value instanceof AppConfigurationEntryHolder )
      {
         AppConfigurationEntryHolder ace = (AppConfigurationEntryHolder) value;
         info.addAppConfigurationEntry(ace.getEntry());
      }
      else if( value instanceof AppConfigurationEntry )
      {
         AppConfigurationEntry ace = (AppConfigurationEntry) value;
         info.addAppConfigurationEntry(ace);
      }
   }

   public Object instantiate()
   {
      return info;
   }

   public Class getTargetClass()
   {
      return AuthenticationInfo.class;
   }
}
