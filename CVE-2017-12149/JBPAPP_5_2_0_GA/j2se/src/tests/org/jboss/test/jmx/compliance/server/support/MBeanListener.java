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
package org.jboss.test.jmx.compliance.server.support;

import javax.management.Notification;
import javax.management.NotificationListener;

/**
 * Simple Listener
 *
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 * @version $Revision: 81019 $
 *   
 */
public class MBeanListener
   implements NotificationListener, MBeanListenerMBean
{
   public long count = 0;
   public Object source = null;
   public Object handback = null;
   public long count1 = 0;
   public Object source1 = new Object();
   public Object handback1 = new Object();
   public long count2 = 0;
   public Object source2 = new Object();
   public Object handback2 = new Object();

   Object hb1 = null;
   Object hb2 = null;

   public MBeanListener()
   {
   }

   public MBeanListener(String hb1, String hb2)
   {
      this.hb1 = hb1;
      this.hb2 = hb2;
   }

   public void handleNotification(Notification n, Object nhb)
   {
      if (nhb != null && nhb.equals(hb1))
      {
         count1++;
         source1 = n.getSource();
         handback1 = nhb;
      }
      else if (nhb != null && nhb.equals(hb2))
      {
         count2++;
         source2 = n.getSource();
         handback2 = nhb;
      }
      else
      {
         count++;
         source = n.getSource();
         handback = nhb;
      }
   }
}
