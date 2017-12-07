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
package org.jboss.test.entityexc.interfaces;

import java.util.Collection;

import java.rmi.RemoteException;

import javax.ejb.EJBHome;
import javax.ejb.CreateException;
import javax.ejb.FinderException;

/**
 *  Home interface of the entity exception test bean.
 *
 *  @author <a href="mailto:osh@sparre.dk">Ole Husgaard</a>
 *  @version $Revision: 81036 $
 */
public interface EntityExcHome extends EJBHome
{
  /**
   *  JNDI name of this home. 
   */
  public final String JNDI_NAME = "EntityExc";

  /**
   *  Create a new entity instance.
   */
  public EntityExc create(Integer id, int flags)
    throws MyAppException, CreateException, RemoteException;

  /**
   *  Find by primary key.
   */
  public EntityExc findByPrimaryKey(Integer key, int flags)
    throws MyAppException, FinderException, RemoteException;
 
  /**
   *  Find all beans in this interface.
   */
  public Collection findAll(int flags)
    throws MyAppException, FinderException, RemoteException;
 
  /**
   *  Reset the database to a known state.
   *  This is used for initializing, and should be called after deployment
   *  but before the tests start.
   *  It will create the database table, if it does not exist, or is not
   *  correctly defined.
   *  If the database table is not empty, all records in it will be deleted.
   */
  public void resetDatabase()
    throws RemoteException;
}
