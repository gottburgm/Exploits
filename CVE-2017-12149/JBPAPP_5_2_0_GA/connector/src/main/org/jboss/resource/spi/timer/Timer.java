/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.resource.spi.timer;

import java.util.List;

/**
 * A Timer represents a.
 * 
 * @author <a href="weston.price@jboss.com">Weston Price</a>
 * @version $Revision: 71554 $
 */
public interface Timer
{
   
   /**
    * Cancel the timer.
    * 
    */
   public void cancel();
   
   /**
    * Set the timer name;
    * 
    * @param name the name of the timer.
    * 
    */
   public void setName(String name);
   
   /**
    * Get the timer name.
    * 
    * @return the timer name;
    * 
    */
   public String getName();
   
   public List getTimerListeners() throws TimerException;
   public void setTimerListeners(List listeners);
   
   public boolean isPersistent();
   
   public boolean hasFired();
   
   public boolean isCanceled();
   
   public long getPeriod();
   
   public long getScheduledExecutionTime();
   
   
}
