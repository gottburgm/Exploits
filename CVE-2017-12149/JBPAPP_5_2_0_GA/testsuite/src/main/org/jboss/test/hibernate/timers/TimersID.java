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
package org.jboss.test.hibernate.timers;

import java.io.Serializable;

/**
 The Timers table key

 @author Scott.Stark@jboss.org
 @version $Revision: 81036 $
 */
public class TimersID implements Serializable
{
   private static final long serialVersionUID = 1L;

   private String timerID;
   private String targetID;

   public TimersID()
   {
   }
   public TimersID(String timerID, String targetID)
   {
      this.timerID = timerID;
      this.targetID = targetID;
   }

   public String getTimerID()
   {
      return this.timerID;
   }

   public void setTimerID(String timerID)
   {
      this.timerID = timerID;
   }

   public String getTargetID()
   {
      return this.targetID;
   }

   public void setTargetID(String targetID)
   {
      this.targetID = targetID;
   }


   public boolean equals(Object other)
   {
      if ((this == other)) return true;
      if ((other == null)) return false;
      if (!(other instanceof TimersID)) return false;
      TimersID castOther = (TimersID) other;

      return ((this.getTimerID() == castOther.getTimerID()) || (this.getTimerID() != null && castOther.getTimerID() != null && this.getTimerID().equals(castOther.getTimerID())))
         && ((this.getTargetID() == castOther.getTargetID()) || (this.getTargetID() != null && castOther.getTargetID() != null && this.getTargetID().equals(castOther.getTargetID())));
   }

   public int hashCode()
   {
      int result = 17;

      result = 37 * result + (getTimerID() == null ? 0 : this.getTimerID().hashCode());
      result = 37 * result + (getTargetID() == null ? 0 : this.getTargetID().hashCode());
      return result;
   }


}
