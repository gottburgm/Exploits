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
package org.jboss.services.xml;

import java.util.Map;
import java.util.Iterator;
import java.util.Properties;

import org.jboss.util.xml.JBossEntityResolver;

/**
 A simple mbean for managing the static JBossEntityResolver class entity
 mappings.

 @todo update this to support xml catalog mappings

 @author Scott.Stark@jboss.org
 @version $Revision: 81038 $
 */
public class JBossEntityResolverMgr
   implements JBossEntityResolverMgrMBean
{

   public boolean isWarnOnNonFileURLs()
   {
      return JBossEntityResolver.isWarnOnNonFileURLs();
   }
   public void setWarnOnNonFileURLs(boolean flag)
   {
      JBossEntityResolver.setWarnOnNonFileURLs(flag);
   }

   public Properties getEntityMap()
   {
      Map map = JBossEntityResolver.getEntityMap();
      Properties props = new Properties();
      Iterator entries = map.entrySet().iterator();
      while( entries.hasNext() )
      {
         Map.Entry entry = (Map.Entry) entries.next();
         String key = (String) entry.getKey();
         String value = (String) entry.getValue();
         props.setProperty(key, value);
      }
      return props;
   }
   public void setEntityMap(Properties map)
   {
      Iterator entries = map.entrySet().iterator();
      while( entries.hasNext() )
      {
         Map.Entry entry = (Map.Entry) entries.next();
         String key = (String) entry.getKey();
         String value = (String) entry.getValue();
         JBossEntityResolver.registerEntity(key, value);
      }
   }

   public void registerEntity(String id, String file)
   {
      JBossEntityResolver.registerEntity(id, file);
   }
}
