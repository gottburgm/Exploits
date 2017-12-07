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
package org.jboss.spring.cluster;

import org.jboss.cache.pojo.PojoCache;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;

/**
 * Pojo cache scope.
 *
 * It enables plain spring beans to be clustered
 * via JBoss Pojo cache.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class CacheScope extends CacheLookup implements Scope
{
   public CacheScope(PojoCache pojoCache)
   {
      super(pojoCache);
   }

   public String getConversationId()
   {
      return null;
   }

   public Object get(String name, ObjectFactory objectFactory)
   {
      Object result = get(name);
      return (result != null) ?  result : put(name,  objectFactory.getObject());
   }

   public void registerDestructionCallback(String string, Runnable runnable)
   {
   }
}
