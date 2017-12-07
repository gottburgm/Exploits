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

package org.jboss.web.tomcat.service.deployers;

import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.jboss.logging.Logger;

/**
 * {@link LifecycleListener} that delegates handling of event callbacks
 * to an arbitrary microcontainer bean that also implements 
 * <code>LifecycleListener</code>.  Serves as an integration hook to allow
 * microcontainer-based beans to integrate into the <code>server.xml</code> and
 * <code>org.apache.tomcat.util.digester.Digester</code>-based process by
 * which the JBoss Web server is instantiated.
 * <p>
 * Listener exposes a {@link #getDelegateBeanName() delegateBeanName} property
 * (configurable in <code>server.xml</code>).  Upon first 
 * {@link #lifecycleEvent(LifecycleEvent) receipt of a lifecycle event}, 
 * a service lookup of the bean registered in the microcontainer under
 * <code>delegateBeanName</code> is performed.  All lifecycle event callbacks
 * are delegated to that bean.
 * </p>
 * 
 * @author Brian Stansberry
 */
public class MicrocontainerIntegrationLifecycleListener implements LifecycleListener
{
   private static final Logger log = Logger.getLogger(MicrocontainerIntegrationLifecycleListener.class);
   
   private volatile boolean inited;
   private LifecycleListener delegate;
   private String delegateBeanName;
   private boolean failIfBeanMissing = true;
   private boolean warnIfBeanMissing = true;
   
   public final String getDelegateBeanName()
   {
      return this.delegateBeanName;
   }

   public final void setDelegateBeanName(String delegateBeanName)
   {
      this.delegateBeanName = delegateBeanName;
   }  

   public boolean getFailIfBeanMissing()
   {
      return this.failIfBeanMissing;
   }

   public void setFailIfBeanMissing(boolean failIfBeanMissing)
   {
      this.failIfBeanMissing = failIfBeanMissing;
   }

   public final boolean getWarnIfBeanMissing()
   {
      return this.warnIfBeanMissing;
   }

   public final void setWarnIfBeanMissing(boolean warnIfBeanMissing)
   {
      this.warnIfBeanMissing = warnIfBeanMissing;
   }

   /**
    * Passes the event to the delegate bean.  On first invocation does
    * the lookup of the delegate bean.
    * 
    * @param event the event
    */
   public void lifecycleEvent(LifecycleEvent event)
   {
      if (!inited)
      {
         init();
      }
      
      if (this.delegate != null)
      {
         this.delegate.lifecycleEvent(event);
      }
   }
   
   private synchronized void init()
   {
      if (!inited)
      {         
         if (this.delegateBeanName != null)
         {
            this.delegate = (LifecycleListener) JBossWebMicrocontainerBeanLocator.getInstalledBean(this.delegateBeanName);
         }
         
         if (this.delegate == null)
         {  
            if (this.failIfBeanMissing)
            {
               throw new IllegalStateException("Unable to locate delegate bean " + this.delegateBeanName + "; listener is nonfunctional");
            }
            else if (this.warnIfBeanMissing)
            {
               log.warn("Unable to locate delegate bean " + this.delegateBeanName + "; listener is nonfunctional");
            }
         }
         
         this.inited = true;
      }
   }

}
