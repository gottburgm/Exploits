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
package org.jboss.ha.hasessionstate.server;

import org.jboss.system.ServiceMBeanSupport;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;

import org.jboss.ha.framework.interfaces.HAPartition;
import org.jboss.ha.hasessionstate.server.HASessionStateImpl;

/**
 *   Service class for HASessionState
 *
 *   @see org.jboss.ha.hasessionstate.interfaces.HASessionState
 *   @author sacha.labourey@cogito-info.ch
 *   @version $Revision: 81001 $
 *
 * <p><b>Revisions:</b><br>
 */

public class HASessionStateService 
   extends ServiceMBeanSupport 
   implements HASessionStateServiceMBean
{
   protected String jndiName;
   protected HAPartition clusterPartition;
   protected long beanCleaningDelay = 0;   
   protected HASessionStateImpl sessionState;
   
   public String getName ()
   {
      return this.getJndiName ();
   }

   public String getJndiName ()
   {
      return this.jndiName;
   }
   
   public void setJndiName (String newName)
   {
      this.jndiName = newName;
   }
   
   public String getPartitionName ()
   {
      return clusterPartition.getPartitionName();
   }
   
   public HAPartition getHAPartition()
   {
      return clusterPartition;
   }

   public void setHAPartition(HAPartition clusterPartition)
   {
      this.clusterPartition = clusterPartition;
   }

   public long getBeanCleaningDelay ()
   {
      if (this.sessionState == null)
         return this.beanCleaningDelay;
      else
         return this.sessionState.beanCleaningDelay;
   }   
   
   public void setBeanCleaningDelay (long newDelay)
   {
      this.beanCleaningDelay = newDelay;
   }
   
   // ******************************************************************
   
   protected ObjectName getObjectName (MBeanServer server, ObjectName name)
      throws MalformedObjectNameException
   {
      return name == null ? OBJECT_NAME : name;
   }
   
   // ******************************************************************
   
   
   protected void createService()
      throws Exception
   {
      if (clusterPartition == null)
      {
         throw new IllegalStateException("HAPartition property must be set before starting SessionState service");
      }

      sessionState = new HASessionStateImpl (jndiName, clusterPartition, beanCleaningDelay);
      sessionState.init ();
   }

   protected void startService () throws Exception
   {
      this.sessionState.start ();
   }
   
   protected void stopService() throws Exception
   {
      this.sessionState.stop ();
   }

   protected void destroyService() throws Exception
   {
      this.sessionState.destroy();
      this.sessionState = null;
   }
   
}

