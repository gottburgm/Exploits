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


/**
 * A simple pojo to represent a hypothetical thread pool service
 *
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81036 $
 */
public class PoolService extends AbstractService
{
   // Private -------------------------------------------------------
   
   private String groupName = "JBoss System Threads";
   private int poolSize = 10;
   private int queueSize = 1000;
   
   // Constructor ---------------------------------------------------
   
   public PoolService()
   {
      super("PoolService");
   }
   
   // Accessors/Mutators --------------------------------------------

   public void setGroupName(String groupName)
   {
      log("setGroupName(" + groupName + ")");      
      this.groupName = groupName;
   }
   
   public String getGroupName()
   {
      return groupName;
   }
   
   public void setPoolSize(int poolSize)
   {
      log("setPoolSize(" + poolSize + ")");       
      this.poolSize = poolSize;
   }
   
   public int getPoolSize()
   {
      return poolSize;
   }
   
   public void setQueueSize(int queueSize)
   {
      log("setQueueSize(" + queueSize + ")");       
      this.queueSize = queueSize;
   }
   
   public int getQueueSize()
   {
      return queueSize;
   }
   
   // Overrides -----------------------------------------------------
   
   public String toString()
   {
      StringBuffer sbuf = new StringBuffer();
      sbuf
      .append("PoolService")
      .append("[ name=").append(name)
      .append(", state=").append(state)      
      .append(", groupName=").append(groupName)
      .append(", poolSize=").append(poolSize)
      .append(", queueSize=").append(queueSize)
      .append(" ]");
      
      return sbuf.toString();
   }
}
