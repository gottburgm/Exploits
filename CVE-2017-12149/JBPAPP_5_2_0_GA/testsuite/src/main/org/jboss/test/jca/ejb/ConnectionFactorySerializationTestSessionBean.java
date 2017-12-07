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
package org.jboss.test.jca.ejb;

import java.rmi.MarshalledObject;
import java.rmi.RemoteException;
import java.sql.Connection;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.jboss.logging.Logger;

/**
 * ConnectionFactorySerializationTestSessionBean.java
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version $Revision: 81036 $
 *
 * @ejb:bean   name="ConnectionFactorySerializationTestSession"
 *             jndi-name="ConnectionFactorySerializationTestSession"
 *             view-type="remote"
 *             type="Stateless"
 */
public class ConnectionFactorySerializationTestSessionBean 
   implements SessionBean  
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 1L;
   private final Logger log = Logger.getLogger(getClass());

   /**
    * Describe <code>testConnectionFactorySerialization</code> method here.
    *
    * @exception EJBException if an error occurs
    * @ejb:interface-method
    */
   public void testConnectionFactorySerialization() 
   {
      try 
      {
         DataSource ds = (DataSource)new InitialContext().lookup("java:/DefaultDS");
         Connection c = ds.getConnection();
         c.close();
         MarshalledObject mo = new MarshalledObject(ds);
         ds = (DataSource)mo.get();
         c = ds.getConnection();
         c.close();
      }
      catch (Exception e)
      {
         log.info("Exception: ", e);
         throw new EJBException("Exception: " + e);
      } // end of try-catch
   }

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

