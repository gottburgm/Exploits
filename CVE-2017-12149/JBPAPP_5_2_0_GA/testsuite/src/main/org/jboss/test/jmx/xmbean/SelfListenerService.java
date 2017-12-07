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
package org.jboss.test.jmx.xmbean;

import javax.management.AttributeChangeNotification;
import javax.management.Notification;
import javax.management.NotificationListener;

import org.jboss.system.ServiceMBeanSupport;

/**
 * An XMBean wrapped Service that listens for notification from
 * itself, and expects to receive an AttributeChangeNotification
 * for Attr1. Baseclass ServiceMBeanSupport will also produce AVC
 * for the inherited State attribute, but we are not interested
 * for this.
 * 
 * @author  gunter.zeilinger@tiani.com
 * @author  <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * 
 * @version $Revision: 81036 $
 */
public class SelfListenerService
   extends ServiceMBeanSupport
   implements NotificationListener
{
   private boolean attr1 = false;
   private boolean gotAttr1AVC = false;
   
   public boolean getAttr1()
   {
      return attr1;
   }
   
   public void setAttr1(boolean attr1)
   {
      this.attr1 = attr1;
   }
   
   public boolean getGotAttr1AVC()
   {
      return gotAttr1AVC;
   }
   
   protected void startService()
      throws Exception
   {
      server.addNotificationListener(super.serviceName, this, null, null);
   }
   
   protected void stopService()
      throws Exception
   {
      server.removeNotificationListener(super.serviceName, this);
   }
   
   public void handleNotification(Notification notif, Object handback)
   {
      log.info("handleNotification: " + notif);
      
      if (notif instanceof AttributeChangeNotification)
      {
         AttributeChangeNotification avc = (AttributeChangeNotification)notif;
         
         if (avc.getAttributeName().equals("Attr1"))
         {
            Boolean newValue = (Boolean)avc.getNewValue();
            
            if (Boolean.TRUE.equals(newValue))
               this.gotAttr1AVC = true;
         }
      }
   }
}
