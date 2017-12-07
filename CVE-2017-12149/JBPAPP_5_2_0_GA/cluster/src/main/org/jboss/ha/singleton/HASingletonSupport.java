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
package org.jboss.ha.singleton;

import javax.management.Notification;

import org.jboss.ha.framework.interfaces.HASingleton;
import org.jboss.ha.framework.interfaces.HASingletonElectionPolicy;
import org.jboss.ha.framework.interfaces.HASingletonLifecycle;
import org.jboss.ha.framework.server.HAServiceRpcHandler;
import org.jboss.ha.framework.server.HASingletonImpl;
import org.jboss.ha.framework.server.HASingletonRpcHandler;
import org.jboss.ha.jmx.AbstractHAServiceMBeanSupport;


/**
 * Base class for HA-Singleton legacy mbean services.
 *
 * @author <a href="mailto:ivelin@apache.org">Ivelin Ivanov</a>
 * @author <a href="mailto:scott.stark@jboss.org">Scott Stark</a>
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @author <a href="mailto:galder.zamarreno@jboss.com">Galder Zamarreno</a>
 * @author <a href="mailto:pferraro@redhat.com">Paul Ferraro</a>
 * @version $Revision: 87733 $
 */
public class HASingletonSupport
   extends AbstractHAServiceMBeanSupport<HASingleton<Notification>>
   implements HASingleton<Notification>, HASingletonRpcHandler<Notification>
{
   /**
    * @see org.jboss.ha.framework.interfaces.HASingletonMBean#isMasterNode()
    */
   public boolean isMasterNode()
   {
      return this.getHAService().isMasterNode();
   }
   
   /**
    * @see org.jboss.ha.framework.interfaces.HASingleton#setElectionPolicy(org.jboss.ha.framework.interfaces.HASingletonElectionPolicy)
    */
   public void setElectionPolicy(HASingletonElectionPolicy electionPolicy)
   {
      this.getHAService().setElectionPolicy(electionPolicy);
   }
   
   /**
    * @see org.jboss.ha.framework.interfaces.HASingleton#getElectionPolicy()
    */
   public HASingletonElectionPolicy getElectionPolicy()
   {
      return this.getHAService().getElectionPolicy();
   }
   
   /**
    * @see org.jboss.ha.framework.interfaces.HASingleton#getRestartOnMerge()
    */
   public boolean getRestartOnMerge()
   {
      return this.getHAService().getRestartOnMerge();
   }
   
   /**
    * @see org.jboss.ha.framework.interfaces.HASingleton#setRestartOnMerge(boolean)
    */
   public void setRestartOnMerge(boolean restartOnMerge)
   {
      this.getHAService().setRestartOnMerge(restartOnMerge);
   }
   
   
   // Public --------------------------------------------------------
   
   /**
    * Extending classes should override this method and implement the custom
    * singleton logic. Only one node in the cluster is the active master.
    * If the current node is elected for master, this method is invoked.
    * When another node is elected for master for some reason, the
    * stopSingleton() method is invokded.
    * <p>
    * When the extending class is a stateful singleton, it will
    * usually use putDistributedState() and getDistributedState() to save in
    * the cluster environment information that will be needed by the next node
    * elected for master should the current master node fail.
    *
    * @see HASingletonLifecycle
    */
   public void startSingleton()
   {
      this.getHAService().startSingleton();
   }
   
   /**
    * Extending classes should override this method and implement the custom
    * singleton logic. Only one node in the cluster is the active master.
    * If the current node is master and another node is elected for master, this
    * method is invoked.
    * 
    * @see HASingletonLifecycle
    */
   public void stopSingleton()
   {
      this.getHAService().stopSingleton();
   }
   
   /**
    * @see org.jboss.ha.jmx.AbstractHAServiceMBeanSupport#createHAService()
    */
   @Override
   protected HASingleton<Notification> createHAService()
   {
      return new HASingletonService();
   }

   /**
    * @see org.jboss.ha.framework.server.HASingletonRpcHandler#stopOldMaster()
    */
   public void stopOldMaster() throws Exception
   {
      ((HASingletonService) this.getHAService()).stopIfMaster();
   }
   
   private class HASingletonService extends HASingletonImpl<Notification>
   {
      HASingletonService()
      {
         super(HASingletonSupport.this, HASingletonSupport.this, HASingletonSupport.this);
      }
      
      /**
       * Expose HASingletonSupport subclass methods to rpc handler
       * @see org.jboss.ha.framework.server.HASingletonImpl#getRpcHandler()
       */
      @Override
      protected HAServiceRpcHandler<Notification> getRpcHandler()
      {
         return HASingletonSupport.this;
      }
      
      /**
       * Expose to parent class
       * @see org.jboss.ha.framework.server.HASingletonImpl#stopIfMaster()
       */
      @Override
      protected void stopIfMaster()
      {
         super.stopIfMaster();
      }
   }
}
