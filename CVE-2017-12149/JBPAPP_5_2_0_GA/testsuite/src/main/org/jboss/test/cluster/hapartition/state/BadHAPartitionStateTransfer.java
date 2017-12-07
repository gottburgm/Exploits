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
package org.jboss.test.cluster.hapartition.state;

import java.io.Serializable;

/**
 * HAPartitionStateTransfer impl that will trigger an exception either
 * on the state sender side or the state receiver side during state
 * transfer processing. See {@link #getCurrentState()} for details.
 * 
 * @author <a href="brian.stansberry@jboss.com">Brian Stansberry</a>
 * @version $Revision: 85945 $
 */
public class BadHAPartitionStateTransfer 
      extends AbstractHAPartitionStateTransfer 
      implements BadHAPartitionStateTransferMBean
{   
   private boolean returnState;

   /**
    * Depending on whether {@link #getReturnState()} is <code>true</code>, either
    * returns 
    * {@link BadHAPartitionState an object that will throw an exception when deserialized}
    * or immediately throws {@link BadHAPartitionStateException}.  The former
    * allows testing of problems on the state receiver side; the latter allows
    * testing of problems on the state generator side.
    */
   public Serializable getCurrentState()
   {
      if (returnState)
         return new BadHAPartitionState();
      
      throw new BadHAPartitionStateException("Configured not to return state");
   }

   public void setCurrentState(Serializable newState)
   {
      // no-op
   }

   /**
    * Gets whether {@link #getCurrentState()} should return state or throw an
    * exception.
    */
   public boolean getReturnState()
   {
      return returnState;
   }

   /**
    * Sets whether {@link #getCurrentState()} should return state or throw an
    * exception.
    */
   public void setReturnState(boolean returnState)
   {
      this.returnState = returnState;
   }
   
   

}
