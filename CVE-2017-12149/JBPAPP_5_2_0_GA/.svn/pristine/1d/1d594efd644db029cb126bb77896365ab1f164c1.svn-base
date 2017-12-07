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
package org.jboss.test.jmx.eardeployment.b.ejb; // Generated package name



import java.rmi.RemoteException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.InitialContext;
import org.jboss.logging.Logger;
import org.jboss.test.jmx.eardeployment.a.interfaces.SessionA;
import org.jboss.test.jmx.eardeployment.a.interfaces.SessionAHome;

/**
 * SessionBBean.java
 *
 *
 * Created: Thu Feb 21 14:50:22 2002
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version
 *
 *
 * @ejb:bean   name="SessionB"
 *             jndi-name="eardeployment/SessionB"
 *             local-jndi-name="eardeployment/LocalSessionB"
 *             view-type="both"
 *             type="Stateless"
 *
 */

public class SessionBBean implements SessionBean  {

   /**
    * Describe <code>callA</code> method here.
    *
    * @ejb:interface-method
    */
   public boolean callA()
   {
      try
      {
         SessionAHome ahome = (SessionAHome)new InitialContext().lookup("eardeployment/SessionA");
         SessionA a = ahome.create();
         a.doNothing();
         return true;
      }
      catch (Exception e)
      {
         Logger.getLogger(getClass()).error("error in callA", e);
         return false;  
      }
   }

   /**
    * Describe <code>doNothing</code> method here.
    *
    * @ejb:interface-method
    */
   public void doNothing()
   {
   }

   /**
    * Describe <code>ejbCreate</code> method here.
    *
    * @ejb:create-method
    */
   public void ejbCreate() 
   {
   }


   public void ejbActivate() throws RemoteException
   {
   }

   public void ejbPassivate() throws RemoteException
   {
   }

   public void ejbRemove() throws RemoteException
   {
   }

   public void setSessionContext(SessionContext ctx) throws RemoteException
   {
   }

   public void unsetSessionContext() throws RemoteException
   {
   }

}

