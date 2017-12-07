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
package org.jboss.mx.metadata.xb;

/**
 @author Scott.Stark@jboss.org
 @version $Revision: 81026 $
 */
public class PersistPolicy
{
   private String persistPolicy;
   private String persistPeriod;
   private String persistLocation;
   private String persistName;

   public String getPersistPolicy()
   {
      return persistPolicy;
   }

   public void setPersistPolicy(String persistPolicy)
   {
      this.persistPolicy = persistPolicy;
   }

   public String getPersistPeriod()
   {
      return persistPeriod;
   }

   public void setPersistPeriod(String persistPeriod)
   {
      this.persistPeriod = persistPeriod;
   }

   public String getPersistLocation()
   {
      return persistLocation;
   }

   public void setPersistLocation(String persistLocation)
   {
      this.persistLocation = persistLocation;
   }

   public String getPersistName()
   {
      return persistName;
   }

   public void setPersistName(String persistName)
   {
      this.persistName = persistName;
   }

}
