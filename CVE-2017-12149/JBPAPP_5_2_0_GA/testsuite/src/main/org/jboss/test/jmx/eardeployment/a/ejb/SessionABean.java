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
package org.jboss.test.jmx.eardeployment.a.ejb;



import java.rmi.RemoteException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.InitialContext;
import org.jboss.logging.Logger;
import org.jboss.test.jmx.eardeployment.b.interfaces.SessionB;
import org.jboss.test.jmx.eardeployment.b.interfaces.SessionBHome;
import org.jboss.test.jmx.eardeployment.util.X;

/**
 * SessionABean.java
 *
 *
 * Created: Thu Feb 21 14:48:18 2002
 *
 * @ejb:bean   name="SessionA"
 *             jndi-name="eardeployment/SessionA"
 *             local-jndi-name="eardeployment/LocalSessionA"
 *             view-type="both"
 *             type="Stateless"
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public class SessionABean implements SessionBean
{
    private static String version = X.VERSION;
   /**
    * Describe <code>callB</code> method here.
    *
    * @exception RemoteException if an error occurs
    * @ejb:interface-method
    */
   public boolean callB()
   {
      try 
      {
         
         SessionBHome bhome = (SessionBHome)new InitialContext().lookup("eardeployment/SessionB");
         SessionB b = bhome.create();
         b.doNothing();
         return true;
      }
      catch (Exception e)
      {
         Logger.getLogger(getClass()).error("error in callB", e);
         return false;  
      } // end of try-catch
      
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

   public void ejbActivate()
   {
   }

   public void ejbPassivate()
   {
   }

   public void ejbRemove()
   {
   }

   public void setSessionContext(SessionContext ctx)
   {
   }
}
