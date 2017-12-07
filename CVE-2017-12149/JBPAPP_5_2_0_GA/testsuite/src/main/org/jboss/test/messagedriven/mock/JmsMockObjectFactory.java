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
package org.jboss.test.messagedriven.mock;

import java.util.HashMap;
import java.util.Map;

/**
 * A JmsMockObjectFactory.
 * 
 * @author <a href="weston.price@jboss.com">Weston Price</a>
 * @version $Revision: 81036 $
 */
public class JmsMockObjectFactory
{
   
   private static final String DEFAULT_PROVIDER_URL = "";
   
   private static final Map objectUrlMap = new HashMap();
   private static final Map mockObjectMap = new HashMap();
   
   static{
      
      objectUrlMap.put(JmsMockObjectType.PROVIDER_ADAPTER, DEFAULT_PROVIDER_URL);
      
   }
   
   public static JmsMockObject loadObject(JmsMockObjectType type, String config){
    
      return null;
   }
   
   
   public static JmsMockObject loadObject(JmsMockObjectType type){
      
      final String url = (String)objectUrlMap.get(type);
      return loadObject(type, url);
   
   }
   
}
