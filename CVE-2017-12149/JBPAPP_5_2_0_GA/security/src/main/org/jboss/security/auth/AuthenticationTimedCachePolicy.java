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
package org.jboss.security.auth;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jboss.util.TimedCachePolicy;

/**
 * Implementation of TimedCachePolicy that also returns invalid keys
 * 
 * @author <a href="mmoyses@redhat.com">Marcus Moyses</a>
 * @version $Revision: 1 $
 */
public class AuthenticationTimedCachePolicy extends TimedCachePolicy implements Serializable
{
   public AuthenticationTimedCachePolicy(int defaultCacheTimeout, boolean b, int defaultCacheResolution)
   {
      super(defaultCacheTimeout,b, defaultCacheResolution);      
   }

   public List getInvalidKeys()
   {
      ArrayList invalidKeys = new ArrayList();
      synchronized (entryMap)
      {
         Iterator iter = entryMap.entrySet().iterator();
         while (iter.hasNext())
         {
            Map.Entry entry = (Map.Entry) iter.next();
            TimedEntry value = (TimedEntry) entry.getValue();
            if (value.isCurrent(now) == false)
               invalidKeys.add(entry.getKey());
         }
      }
      return invalidKeys;
   }

}
