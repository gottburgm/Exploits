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
package org.jboss.test.aop.simpleejb;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * @author ifedorenko
 * 
 * @ejb:bean type="Stateless" name="test/Simple"
 *      view-type="remote"
 *      jndi-name="ejb/test/Simple"
 * @ejb:transaction type="Required"
 */
public class SimpleBean implements SessionBean
{
   /**
    * @ejb:interface-method
    */
   public String getTest()
   {
      System.out.println("**** in SimpleBean.getTest()");
      return "Test";
   }

   public void ejbActivate() throws EJBException, RemoteException
   {
   }

   public void ejbPassivate() throws EJBException, RemoteException
   {
   }

   public void ejbRemove() throws EJBException, RemoteException
   {
   }

   public void setSessionContext(SessionContext arg0)
      throws EJBException, RemoteException
   {
   }

   public void ejbCreate()
   {
   }

}
