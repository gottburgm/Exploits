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
package org.jboss.test.cluster.ejb2.basic.bean;

import javax.ejb.*;

import java.rmi.RemoteException;
import java.rmi.dgc.VMID;

import org.jboss.test.cluster.ejb2.basic.interfaces.NodeAnswer;

/**
 * @author <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>.
 * @version $Revision: 85945 $
 */
public class StatefulSessionBean extends org.jboss.test.testbean.bean.StatefulSessionBean
{

   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   
   public transient VMID myId = null; 
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   public void ejbCreate(String name) throws RemoteException, CreateException
   {
      super.ejbCreate(name);

      this.myId = new VMID();
      log.debug("My ID: " + this.myId);
   }

   public void ejbActivate() throws RemoteException
   {
      super.ejbActivate();
      if (this.myId == null)
      {
         //it is a failover: we need to assign ourself an id
         this.myId = new VMID();
      }
      log.debug("Activate. My ID: " + this.myId + " name: " + this.name);
   }

   public void ejbPassivate() throws RemoteException
   {
      super.ejbPassivate();
      log.debug("Passivate. My ID: " + this.myId + " name: " + this.name);
   }
   // Public --------------------------------------------------------
   
   // Remote Interface implementation ----------------------------------------------
   
   public NodeAnswer getNodeState() throws RemoteException
   {
      NodeAnswer state = new NodeAnswer(this.myId, this.name);
      log.debug("getNodeState, " + state);
      return state;
   }

   public void setName(String name) throws RemoteException
   {
      this.name = name;
      log.debug("Name set to " + name);
   }

   public void setNameOnlyOnNode(String name, VMID node) throws RemoteException
   {
      if (node.equals(this.myId))
         this.setName(name);
      else
         throw new EJBException("Trying to assign value on node " + this.myId + " but this node expected: " + node);
   }

   // Y overrides ---------------------------------------------------
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------

}
