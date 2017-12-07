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
package org.jboss.test.kernel.deployment.jboss.beans.servicepojo;

import java.util.List;

/**
 * A simple pojo to represent a hypothetical service
 *
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81036 $
 */
public class POJOService extends AbstractService
{
   // Private -------------------------------------------------------
   
   private PoolService pool;
   private List addresses;
   
   // Constructor ---------------------------------------------------
   
   public POJOService()
   {
      super("POJOService");
   }
   
   // Accessors/Mutators --------------------------------------------

   public void setPoolService(PoolService pool)
   {
      log("setPoolService(" + pool + ")");      
      this.pool = pool;
   }

   public void setBindAddresses(List addresses)
   {
      log("setBindAddresses(" + addresses + ")");       
      this.addresses = addresses;
   }
   
   // Overrides -----------------------------------------------------
   
   public void create() throws Exception
   {
      super.create();
      
      if (!(pool.getState().equals(CREATED)))
      {
         state = FAILED;
         throw new IllegalStateException("pool not CREATED");
      }
   }
   
   public void start() throws Exception
   {
      super.start();
      
      if (!(pool.getState().equals(STARTED)))
      {
         state = FAILED;
         throw new IllegalStateException("pool not STARTED");
      }
   }
   
   public void stop() throws Exception
   {
      super.stop();
      
      if (!(pool.getState().equals(STARTED)))
      {
         state = FAILED;
         throw new IllegalStateException("pool not STARTED");
      }
   }
   
   public void destroy() throws Exception
   {
      super.destroy();
      
      if (!(pool.getState().equals(STOPPED)))
      {
         state = FAILED;
         throw new IllegalStateException("pool not STOPPED");
      }
   }
   
   public String toString()
   {
      StringBuffer sbuf = new StringBuffer();
      sbuf
      .append(getClass().getName())
      .append("[ name=").append(name)
      .append(", state=").append(state)      
      .append(", pool=").append(pool)
      .append(", addresses=").append(addresses)
      .append(" ]");
      
      return sbuf.toString();
   }
}
