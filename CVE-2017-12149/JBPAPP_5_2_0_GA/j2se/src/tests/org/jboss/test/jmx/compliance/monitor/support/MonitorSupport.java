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
package org.jboss.test.jmx.compliance.monitor.support;

import org.jboss.test.jmx.compliance.monitor.MonitorSUITE;

public class MonitorSupport 
{
   boolean done = false;
   String last = "set";
   public synchronized void lock(String who)
   {
      if (!done && last.equals(who))
      {
         try
         {
            wait(MonitorSUITE.MAX_WAIT);
         }
         catch (InterruptedException e) {}        
         if (!done && last.equals(who))
            throw new RuntimeException("-- Time Out --");
      }
   }
   public synchronized void unlock(String who)
   {
      if (!done && last.equals(who))
         throw new RuntimeException("-- Synchronization failure --");
      last=who; 
      notifyAll();
   }
   public synchronized void end()
   {
      done = true;
      notifyAll();
   }
}
