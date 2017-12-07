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
package org.jboss.monitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;

import org.jboss.naming.NonSerializableFactory;
import org.jboss.system.ServiceMBeanSupport;

/**
 * MBean implementation for providing Locking Stats for EntityBeans
 * 
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81030 $
 */
public class EntityLockMonitor  extends ServiceMBeanSupport
   implements EntityLockMonitorMBean
{
   // Constants ----------------------------------------------------
   
   public static final String JNDI_NAME = "EntityLockMonitor";
   
   // Protected -----------------------------------------------------
   
   protected HashMap monitorMap = new HashMap();
   protected long contenders = 0;
   protected long maxContenders = 0;
   protected ArrayList times = new ArrayList();
   protected long contentions = 0;
   protected long totalTime = 0;
   protected long sumContenders = 0;
   
   // Constructors -------------------------------------------------
   
   public EntityLockMonitor()
   {
      // empty
   }
   
   // ServiceMBeanSupport overrides ---------------------------------
   
   protected void startService()
      throws Exception
   {
      bind();

     log.info("EntityLockMonitor started");
   }

   protected void stopService() {
      try
      {
         unbind();
      }
      catch (Exception ignored) {}

     log.info("EntityLockMonitor stopped");
   }
   
   // Attributes ----------------------------------------------------
   
   /**
    * @jmx.managed-attribute
    */
   public synchronized long getAverageContenders()
   {
      if (contentions == 0)
      {
         return 0;
      }
      else
      {
         return sumContenders / contentions;
      }
   }

   /**
    * @jmx.managed-attribute
    */
   public synchronized long getMaxContenders()
   {
      return maxContenders;
   }

   /**
    * @jmx.managed-attribute
    */
   public synchronized long getMedianWaitTime()
   {
      if (times.size() < 1)
      {
         return 0;
      }

      Long[] alltimes = (Long[])times.toArray(new Long[times.size()]);
      long[] thetimes = new long[alltimes.length];
      for (int i = 0; i < thetimes.length; i++)
      {
         thetimes[i] = alltimes[i].longValue();
      }
      Arrays.sort(thetimes);
      return thetimes[thetimes.length / 2];
   }

   /**
    * @jmx.managed-attribute
    */
   public synchronized long getTotalContentions()
   {
      return contentions;
   }
   
   // Operations ----------------------------------------------------
   
   /**
    * @jmx.managed-operation
    */
   public Set listMonitoredBeans()
   {
      synchronized(monitorMap)
      {
         return new TreeSet(monitorMap.keySet());
      }
   }
   
   /**
    * @jmx.managed-operation
    * 
    * @return the LockMonitor that corresponds to the jndiName or null
    */
   public LockMonitor getLockMonitor(String jndiName)
   {
      synchronized(monitorMap)
      {
         return (LockMonitor)monitorMap.get(jndiName);
      }
   }
   
   /**
    * @jmx.managed-operation
    */
   public String printLockMonitor()
   {
      StringBuffer rtn = new StringBuffer();
      rtn.append("<table width=\"1\" border=\"1\">");
      rtn.append("<tr><td><b>EJB JNDI-NAME</b></td><td><b>Total Lock Time</b></td><td><b>Num Contentions</b></td><td><b>Time Outs</b></td><td><b>Max Contenders</b></td></tr>");
      synchronized(monitorMap)
      {
         Iterator it = monitorMap.keySet().iterator();
         while (it.hasNext())
         {
            rtn.append("<tr>");
            String jndiName = (String)it.next();
            rtn.append("<td>");
            rtn.append(jndiName);
            rtn.append("</td>");
            LockMonitor lm = (LockMonitor)monitorMap.get(jndiName);
            rtn.append("<td>");
            rtn.append(("" + lm.getTotalTime()));
            rtn.append("</td><td>");
            rtn.append(("" + lm.getNumContentions()));
            rtn.append("</td><td>");
            rtn.append(("" + lm.getTimeouts()));
            rtn.append("</td><td>");
            rtn.append(("" + lm.getMaxContenders()));
            rtn.append("</td></tr>");
         }
      }
      rtn.append("</table>");
      return rtn.toString();
   }
   
   /**
    * @jmx.managed-operation
    */
   public synchronized void clearMonitor()
   {
      contenders = 0;
      maxContenders = 0;
      times.clear();
      contentions = 0;
      totalTime = 0;
      sumContenders = 0;

      synchronized(monitorMap)
      {
         Iterator it = monitorMap.keySet().iterator();
         while (it.hasNext())
         {
            String jndiName = (String)it.next();
            LockMonitor lm = (LockMonitor)monitorMap.get(jndiName);
            lm.reset();
         }
      }
   }
   
   // Public -------------------------------------------------------
   
   public synchronized void incrementContenders()
   {
      ++contenders;
      ++contentions;
      sumContenders += contenders;
      
      if (contenders > maxContenders)
      {
         maxContenders = contenders;
      }
   }
   
   public synchronized void decrementContenders(long time)
   {
      times.add(new Long(time));
      --contenders;
   }

   public LockMonitor getEntityLockMonitor(String jndiName)
   {
      LockMonitor lm = null;
      
      synchronized(monitorMap)
      {
         lm = (LockMonitor)monitorMap.get(jndiName);
         if (lm == null)
         {
            lm = new LockMonitor(this);
            monitorMap.put(jndiName, lm);
         }
      }
      return lm;
   }
   
   // Private -------------------------------------------------------
   
   private void bind() throws NamingException
   {
      Context ctx = new InitialContext();

      // Ah ! We aren't serializable, so we use a helper class
      NonSerializableFactory.bind(JNDI_NAME, this);
      
      // The helper class NonSerializableFactory uses address type nns, we go on to
      // use the helper class to bind ourselves in JNDI
      StringRefAddr addr = new StringRefAddr("nns", JNDI_NAME);
      Reference ref = new Reference(EntityLockMonitor.class.getName(), addr, NonSerializableFactory.class.getName(), null);
      ctx.bind(JNDI_NAME, ref);
   }
   
   private void unbind() throws NamingException
   {
      new InitialContext().unbind(JNDI_NAME);
      NonSerializableFactory.unbind(JNDI_NAME);
   }
   
}