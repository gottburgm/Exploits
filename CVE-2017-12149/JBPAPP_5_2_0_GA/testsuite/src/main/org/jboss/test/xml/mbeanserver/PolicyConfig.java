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

import java.util.Map;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

/**
 * The root object for the login-config.xml descriptor as defined by the
 * login-config2.xsd
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public class PolicyConfig
{
   Map config = Collections.synchronizedMap(new HashMap());

   public PolicyConfig()
   {
      //System.out.println("PolicyConfig.ctor");
   }

   public void add(AuthenticationInfo authInfo)
   {
      //System.out.println("PolicyConfig.add, "+authInfo);
      config.put(authInfo.getName(), authInfo);
   }
   public AuthenticationInfo get(String name)
   {
      AuthenticationInfo info = (AuthenticationInfo) config.get(name);
      return info;
   }
   public AuthenticationInfo remove(String name)
   {
      AuthenticationInfo info = (AuthenticationInfo) config.remove(name);
      return info;
   }
   public void clear()
   {
      config.clear();
   }
   public Set getConfigNames()
   {
      return config.keySet();
   }
   public int size()
   {
      return config.size();
   }
   public boolean containsKey(String name)
   {
      return config.containsKey(name);
   }
   public void copy(PolicyConfig pc)
   {
      config.putAll(pc.config);
   }
}
