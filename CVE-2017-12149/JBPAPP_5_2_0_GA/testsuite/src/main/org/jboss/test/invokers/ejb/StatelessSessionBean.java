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
package org.jboss.test.invokers.ejb;

import java.rmi.RemoteException;
import javax.ejb.CreateException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.InitialContext;

import org.jboss.test.invokers.interfaces.SimpleBMP;
import org.jboss.test.invokers.interfaces.SimpleBMPHome;

/** A simple session bean for testing access over custom RMI sockets.

@author Scott.Stark@jboss.org
@version $Revision: 81036 $
*/
public class StatelessSessionBean implements SessionBean
{
   static org.jboss.logging.Logger log =
      org.jboss.logging.Logger.getLogger(StatelessSessionBean.class);
   
   private SessionContext sessionContext;

   public void ejbCreate() throws CreateException
   {
      log.debug("StatelessSessionBean.ejbCreate() called");
   }

   public void ejbActivate()
   {
      log.debug("StatelessSessionBean.ejbActivate() called");
   }

   public void ejbPassivate()
   {
      log.debug("StatelessSessionBean.ejbPassivate() called");
   }

   public void ejbRemove()
   {
      log.debug("StatelessSessionBean.ejbRemove() called");
   }

   public void setSessionContext(SessionContext context)
   {
      sessionContext = context;
   }

   public SimpleBMP getBMP(int id) throws RemoteException
   {
      try
      {
         InitialContext ctx = new InitialContext();
         SimpleBMPHome home = (SimpleBMPHome)ctx.lookup("java:comp/env/ejb/SimpleBMP");
         return home.findByPrimaryKey(new Integer(id));
      }
      catch (Exception ex)
      {
         ex.printStackTrace();
         throw new RemoteException("error");
      }
   }

}
