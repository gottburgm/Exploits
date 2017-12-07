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
package org.jboss.varia.threaddump;

import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

/**
 * Threaddump code adapted from:
 * http://www.javaspecialists.eu/archive/Issue132.html
 */
public class ThreadDumpBean
   implements Serializable
{
   private static final long serialVersionUID = 1L;
   
   private final Map<Thread, StackTraceElement[]> stackTraces;
   
   /**
    * Compare threads by name and id.
    */
   private static final Comparator<Thread> THREAD_COMPARATOR =
      new Comparator<Thread>()
      {
         public int compare(Thread t1, Thread t2)
         {
            int result = t1.getName().compareTo(t2.getName());
            if (result == 0)
            {
               Long tid1 = t1.getId();
               Long tid2 = t2.getId();
               result = tid1.compareTo(tid2);
            }
            return result;
         }
      };
      
   public final static void main(String[] args)
   {
      ThreadDumpBean dump = new ThreadDumpBean();
      
      System.out.println(dump.getStackTraces());
   }
   
   public ThreadDumpBean()
   {
      stackTraces = new TreeMap<Thread, StackTraceElement[]>(THREAD_COMPARATOR);
      stackTraces.putAll(Thread.getAllStackTraces());
   }
   
   public Collection<Thread> getThreads()
   {
      return stackTraces.keySet();
   }
   
   public Map<Thread, StackTraceElement[]> getStackTraces()
   {
      return stackTraces;
   }
}

