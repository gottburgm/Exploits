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
import java.util.Date;

/**
 The ejb timers table object mapping
 
 @author Scott.Stark@jboss.org
 @version $Revision: 81036 $
 */
public class Timers implements Serializable
{
   private static final long serialVersionUID = 1L;

   private TimersID id;
   private Date initialDate;
   private Long timerInterval;
   private byte[] instancePK;
   private byte[] info;

   public Timers()
   {
   }
   public Timers(TimersID id)
   {
      this.id = id;
   }

   public TimersID getId()
   {
      return this.id;
   }
   public void setId(TimersID id)
   {
      this.id = id;
   }

   public Date getInitialDate()
   {
      return initialDate;
   }
   public void setInitialDate(Date initialDate)
   {
      this.initialDate = initialDate;
   }

   public Long getTimerInterval()
   {
      return timerInterval;
   }
   public void setTimerInterval(Long timerInterval)
   {
      this.timerInterval = timerInterval;
   }

   public byte[] getInstancePK()
   {
      return instancePK;
   }
   public void setInstancePK(byte[] instancePK)
   {
      this.instancePK = instancePK;
   }

   public byte[] getInfo()
   {
      return this.info;
   }
   public void setInfo(byte[] info)
   {
      this.info = info;
   }

}
