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

import java.rmi.RemoteException;
import java.rmi.dgc.VMID;
import javax.naming.InitialContext;
import javax.naming.Context;
import javax.ejb.CreateException;
import javax.ejb.SessionContext;
import javax.ejb.SessionBean;
import javax.ejb.FinderException;

import org.jboss.logging.Logger;
import org.jboss.test.cluster.ejb2.basic.interfaces.EntityPK;
import org.jboss.test.cluster.ejb2.basic.interfaces.EntityPKHome;
import org.jboss.test.cluster.ejb2.basic.interfaces.NodeAnswer;
import org.jboss.test.testbean.interfaces.AComplexPK;

/**  A stateful session which access an entity bean used in testing the CIF.
 * @author Scott.Stark@jboss.org
 * @version $Revision: 85945 $
 */ 
public class SessionToEntityBean implements SessionBean
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 1L;
   
   private static Logger log = Logger.getLogger(SessionToEntityBean.class);
   private static VMID nodeID = new VMID();

   private int accessCount;
   private AComplexPK theKey;

   public void ejbCreate(AComplexPK key) throws CreateException
   {
      log.debug("ejbCreate(AComplexPK) called, nodeID="+nodeID);
      this.theKey = key;
   }
   public void ejbActivate()
   {
      log.debug("ejbActivate() called, nodeID="+nodeID);
   }
   public void ejbPassivate()
   {
      log.debug("ejbPassivate() called, nodeID="+nodeID);
   }
   public void ejbRemove()
   {
      log.debug("ejbRemove() called, nodeID="+nodeID);
      try
      {
         InitialContext ctx = new InitialContext();
         Context enc = (Context) ctx.lookup("java:comp/env");
         EntityPKHome home = (EntityPKHome) enc.lookup("ejb/EntityPKHome");
         home.remove(theKey);
      }
      catch(Exception e)
      {
         log.error("Failed to remove EntityPK", e);
      }
   }
   public void setSessionContext(SessionContext context)
   {
   }

   public String createEntity()
      throws CreateException
   {
      String msg = null;
      EntityPKHome home = null;
      log.info("Enter createEntity, theKey="+theKey);
      try
      {
         InitialContext ctx = new InitialContext();
         Context enc = (Context) ctx.lookup("java:comp/env");
         home = (EntityPKHome) enc.lookup("ejb/EntityPKHome");
         EntityPK bean = home.findByPrimaryKey(theKey);
         msg = "Found EntityPK, bean="+bean;
         log.info(msg);
      }
      catch(FinderException e)
      {
         EntityPK bean = home.create(theKey.aBoolean, theKey.anInt, theKey.aLong,
            theKey.aDouble, theKey.aString);
         msg = "Created EntityPK, bean="+bean;
         log.info(msg);
      }
      catch(Exception e)
      {
         log.error("Failed to create EntityPK", e);
         throw new CreateException("Failed to create EntityPK: "+e.getMessage());
      }
      return msg;
   }
   public NodeAnswer accessEntity()
   {
      accessCount ++;
      log.debug("Enter accessEntity(), accessCount="+accessCount);
      int beanCount = 0;
      try
      {
         InitialContext ctx = new InitialContext();
         Context enc = (Context) ctx.lookup("java:comp/env");
         EntityPKHome home = (EntityPKHome) enc.lookup("ejb/EntityPKHome");
         EntityPK bean = home.findByPrimaryKey(theKey);
         bean.setOtherField(accessCount);
         log.debug("Set EntityPK.OtherField to: "+accessCount);
         beanCount = bean.getOtherField();
      }
      catch(Exception e)
      {
         log.debug("failed", e);
      }
      log.debug("Exit accessEntity()");
      return new NodeAnswer(nodeID, new Integer(beanCount));
   }

   public int getAccessCount()
   {
      return accessCount;
   }
   
   public NodeAnswer validateAccessCount(int count)
      throws RemoteException
   {
      if( accessCount != count )
      {
         String msg = "AccessCount: " + accessCount + " != " + count;
         log.error(msg);
         throw new RemoteException(msg);
      }         
      
      int beanCount = 0;
      try
      {
         InitialContext ctx = new InitialContext();
         Context enc = (Context) ctx.lookup("java:comp/env");
         EntityPKHome home = (EntityPKHome) enc.lookup("ejb/EntityPKHome");
         EntityPK bean = home.findByPrimaryKey(theKey);
         beanCount = bean.getOtherField();
         if( beanCount != count )
            throw new RemoteException("BeanCount: " + beanCount + " != " + count);
      }
      catch(RemoteException e)
      {
         log.error("Failed to validate EntityPK", e);
         throw e;
      }
      catch(Exception e)
      {
         log.error("Failed to validate EntityPK", e);
         throw new RemoteException("Failed to validate EntityPK");
      }
      return new NodeAnswer(nodeID, new Integer(beanCount));
   }
}
