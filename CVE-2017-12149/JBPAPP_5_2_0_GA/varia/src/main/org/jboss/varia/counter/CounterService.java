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
package org.jboss.varia.counter;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.naming.NonSerializableFactory;

/**
 * A service offering accumulator style counters to help in diagnosing
 * performance issues.
 *
 * @jmx:mbean extends="org.jboss.system.ServiceMBean"
 * 
 * @author <a href="mailto:danch@nvisia.com">Dan Christopherson</href>
 * @version $Revision: 81038 $
 */
public class CounterService
   extends ServiceMBeanSupport
   implements CounterServiceMBean
{
   public static final String JNDI_NAME = "java:/CounterService";

   private HashMap counterMap = new HashMap();
   
   /**
    * accumulate a count into a named counter. will initialize the named
    * counter if neccessary.
    */
   public void accumulate(String counterName, long add)
   {
      Counter counter = null;
      synchronized (counterMap)
      {
         counter = (Counter)counterMap.get(counterName);
         if (counter == null)
         {
            counter = new Counter(counterName);
            counterMap.put(counterName, counter);
         }
      }
      counter.addToCount(add);
   }
   
   protected void startService() throws Exception
   {
      InitialContext ctx = new InitialContext();
      
      //bind myself into JNDI, at java:/CounterService
      NonSerializableFactory.bind(JNDI_NAME, this);
      StringRefAddr addr = new StringRefAddr("nns", JNDI_NAME);
      Reference ref = new Reference(this.getClass().getName(), addr, NonSerializableFactory.class.getName(), null);
      ctx.bind(JNDI_NAME, ref);
   }
   
   protected void stopService() throws Exception
   {
      InitialContext ctx = new InitialContext();
      ctx.unbind(JNDI_NAME);
      NonSerializableFactory.unbind(JNDI_NAME);
   }
   
   /**
    * @jmx:managed-operation
    */
   public String list()
   {
      DecimalFormat format = new DecimalFormat("####0.0000");
      String retVal = "";
      Iterator keys = counterMap.keySet().iterator();
      while (keys.hasNext())
      {
         String key = (String)keys.next();
         Counter counter = (Counter)counterMap.get(key);
         long total = 0;
         int entries = 0;
         synchronized (counter)
         {//so we dont catch half of it.
            total = counter.getCount();
            entries = counter.getEntries();
         }
         double avg = ((double)total)/((double)entries);
         String descrip = key+": total="+total+" on "+entries+"entries for "+
         "an average of "+format.format(avg)+"<br>\n";
         retVal += descrip;
      }
      return retVal;
   }
   
   private static class Counter
   {
      private String name;
      private long count=0;
      private int entries=0;
      
      public Counter(String n)
      {
         name = n;
      }
      
      public String getName()
      {
         return name;
      }
      
      public synchronized long getCount()
      {
         return count;
      }
      
      public synchronized int getEntries()
      {
         return entries;
      }
      
      public synchronized void addToCount(long add)
      {
         count += add;
         entries++;
      }
   }
}
