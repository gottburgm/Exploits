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
package org.jboss.system;

import javax.management.ObjectName;

/**
 * BarrierController service interface
 * 
 * @author <a href="dimitris@jboss.org">Dimitris Andreadis</a>
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81033 $
 */
public interface BarrierControllerMBean extends ListenerServiceMBean
{
   // Attributes ----------------------------------------------------

   /** The controlled barrier StateString */
   String getBarrierStateString();
   
   /** The controlled barrier ObjectName */
   void setBarrierObjectName(ObjectName barrierName);
   ObjectName getBarrierObjectName();
   
   /** The initial state of the barrier */
   void setBarrierEnabledOnStartup(Boolean enableOnStartup);
   Boolean getBarrierEnabledOnStartup();
   
   /** The notification subscription handback string that starts the barrier */
   void setStartBarrierHandback(String startHandback);
   String getStartBarrierHandback();
   
   /** The notification subscription handback string that stops the barrier */
   void setStopBarrierHandback(String stopHandback);
   String getStopBarrierHandback();
   
   /** The ability to dynamically subscribe for notifications */
   void setDynamicSubscriptions(Boolean dynamicSubscriptions);
   Boolean getDynamicSubscriptions();
   
   // Operations ----------------------------------------------------
   
   /** Manually start the controlled barrier */
   void startBarrier();
   
   /** Manually stop the controlled barrier */
   void stopBarrier();
}
