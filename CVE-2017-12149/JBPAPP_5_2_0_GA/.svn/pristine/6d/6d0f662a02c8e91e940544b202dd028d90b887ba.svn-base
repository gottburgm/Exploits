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
package org.jboss.test.txtimer.ejb;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.InitialContext;

import org.jboss.logging.Logger;
import org.jboss.test.txtimer.interfaces.TimerEntity;
import org.jboss.test.txtimer.interfaces.TimerEntityHome;
import org.jboss.test.txtimer.interfaces.TimerSession;
import org.jboss.test.txtimer.interfaces.TimerSessionHome;

/**
 * Session Bean Timer Test
 */
public class TimerFacadeBean implements SessionBean
{
   private static Logger log = Logger.getLogger(TimerFacadeBean.class);

   private SessionContext context;

   /**
    * @ejb.interface-method view-type="both"
    */
   public void rollbackAfterCreateSession(long duration)
           throws Exception
   {
      InitialContext iniCtx = new InitialContext();
      TimerSessionHome home = (TimerSessionHome) iniCtx.lookup(TimerSessionHome.JNDI_NAME);
      TimerSession bean = home.create();
      bean.createTimer(duration, 0, null);
      context.setRollbackOnly();
   }

   /**
    * @ejb.interface-method view-type="both"
    */
   public void rollbackAfterCancelSession()
           throws Exception
   {
      InitialContext iniCtx = new InitialContext();
      TimerSessionHome home = (TimerSessionHome) iniCtx.lookup(TimerSessionHome.JNDI_NAME);
      TimerSession bean = home.create();
      bean.cancelFirstTimer();
      context.setRollbackOnly();
   }

   /**
    * @ejb.interface-method view-type="both"
    */
   public void rollbackAfterCreateEntity(long duration)
           throws Exception
   {
      InitialContext iniCtx = new InitialContext();
      TimerEntityHome home = (TimerEntityHome) iniCtx.lookup(TimerEntityHome.JNDI_NAME);
      TimerEntity bean = home.findByPrimaryKey(new Integer(1));
      bean.createTimer(duration, 0, null);
      context.setRollbackOnly();
   }

   /**
    * @ejb.interface-method view-type="both"
    */
   public void rollbackAfterCancelEntity()
           throws Exception
   {
      InitialContext iniCtx = new InitialContext();
      TimerEntityHome home = (TimerEntityHome) iniCtx.lookup(TimerEntityHome.JNDI_NAME);
      TimerEntity bean = home.findByPrimaryKey(new Integer(1));
      bean.cancelFirstTimer();
      context.setRollbackOnly();
   }


   // -------------------------------------------------------------------------
   // Framework Callbacks
   // -------------------------------------------------------------------------
   
   public void setSessionContext(SessionContext ctx) throws EJBException, RemoteException
   {
      this.context = ctx;
   }

   /**
    * @ejb.create-method view-type="both"
    */
   public void ejbCreate() throws CreateException
   {
   }

   public void ejbRemove() throws EJBException, RemoteException
   {
   }

   public void ejbActivate() throws EJBException, RemoteException
   {
   }

   public void ejbPassivate() throws EJBException, RemoteException
   {
   }
}
