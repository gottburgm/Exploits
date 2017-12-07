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
package org.jboss.test.testbyvalue.bean;


import java.rmi.*;
import javax.ejb.*;
import javax.naming.Context;
import javax.sql.DataSource;
import javax.naming.InitialContext;
import java.sql.Connection;
import org.jboss.test.testbean.interfaces.StatelessSession;
import org.jboss.test.testbean.interfaces.StatelessSessionHome;
import java.util.Collection;
import java.util.Collections;
import java.util.Vector;
import java.util.Enumeration;

public class ByValueEntityBean implements EntityBean {
  private EntityContext entityContext;
  public String name;

  public String ejbCreate() throws RemoteException, CreateException {

       name="nothing";
       return name;
  
	  
  }
  public String ejbCreate(String name) throws RemoteException, CreateException {
      this.name=name;
      return name;
  }


  public String ejbFindByPrimaryKey(String name) throws RemoteException, FinderException {
      return name;
  }


  public void ejbPostCreate() throws RemoteException, CreateException {
  }
  
  public void ejbPostCreate(String name) throws RemoteException, CreateException {
  }
  

  public void ejbActivate() throws RemoteException {
  }

  public void ejbLoad() throws RemoteException {
  }

  public void ejbPassivate() throws RemoteException {
  }

  public void ejbRemove() throws RemoteException, RemoveException {
  }

  public void ejbStore() throws RemoteException {
  }


  public void setEntityContext(EntityContext context) throws RemoteException {
     entityContext = context;
    }

   public void unsetEntityContext() throws EJBException, RemoteException
   {
       this.entityContext=null;
   }

   public String getName()
   {
       return name;
   }

   public void setName(String name)
   {
       this.name=name;
   }

   public void doByValueTest(ClassWithProperty property)
   {
       property.setX(property.getX()+1000);
   }

}
