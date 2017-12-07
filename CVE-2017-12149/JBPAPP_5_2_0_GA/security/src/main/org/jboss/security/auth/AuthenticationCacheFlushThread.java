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

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.logging.Logger;
import org.jboss.security.integration.SecurityConstantsBridge;
import org.jboss.security.plugins.SecurityDomainContext;
import org.jboss.util.CachePolicy;
import org.jboss.util.TimedCachePolicy;

public class AuthenticationCacheFlushThread extends Thread
{
   private static Logger log = Logger.getLogger(AuthenticationCacheFlushThread.class);
   
   private static ConcurrentHashMap<String,SecurityDomainContext> securityMgrMap;
   
   public AuthenticationCacheFlushThread(ConcurrentHashMap<String,SecurityDomainContext> securityMgrMap)
   {
      super("AuthenticationCacheFlushThread");
      this.securityMgrMap = securityMgrMap;
   }
   
   public void run()
   {
      if (log.isDebugEnabled())
         log.debug("Starting authentication cache flush thread");
      while (true)
      {
         if (log.isTraceEnabled())
            log.trace("Running authentication cache flush thread");
         // scan all security domains
         for (Entry<String, SecurityDomainContext> entry : securityMgrMap.entrySet())
         {
            String securityDomain = entry.getKey();
            SecurityDomainContext securityDomainCtx = entry.getValue();
            CachePolicy cache = securityDomainCtx.getAuthenticationCache();
            AuthenticationTimedCachePolicy timedCache = null;
            if (cache instanceof TimedCachePolicy)
            {
               timedCache = (AuthenticationTimedCachePolicy) cache;
            }
            if (timedCache != null)
            {
               if (log.isDebugEnabled())
                  log.debug("Scanning security domain " + securityDomain + " for expired entries");
               List expiredEntries = timedCache.getInvalidKeys();
               if (log.isTraceEnabled())
                  log.trace("Found " + expiredEntries.size() + " expired entries");
               for (Iterator iterator = expiredEntries.iterator(); iterator.hasNext();)
               {
                  Object expiredEntry = iterator.next();
                  timedCache.remove(expiredEntry);
               }
            }
         }
         try
         {
            if (this.isInterrupted() == false)
               Thread.sleep(SecurityConstantsBridge.defaultCacheFlushPeriod * 1000);
            else
               break;
         }
         catch (InterruptedException ie)
         {
            break;
         }
      }
      if (log.isDebugEnabled())
         log.debug("Stopping authentication cache flush thread");
   }
}
