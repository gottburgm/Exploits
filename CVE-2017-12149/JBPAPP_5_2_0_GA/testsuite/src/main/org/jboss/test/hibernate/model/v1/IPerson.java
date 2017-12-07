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
package org.jboss.test.hibernate.model.v1;

import org.hibernate.HibernateException;
/**
 Remote interface for Person accessor.

 @author Scott.Stark@jboss.org
 @version $Revision: 81036 $
 */
public interface IPerson
   extends javax.ejb.EJBObject
{
   public void init( )
      throws java.rmi.RemoteException, HibernateException;
   public void sessionInit( )
      throws java.rmi.RemoteException, HibernateException;

   public Person storeUser( Person user )
      throws java.rmi.RemoteException, HibernateException;

   public Person loadUser( long id )
      throws java.rmi.RemoteException, HibernateException;

   public Person loadUser( Long id )
      throws java.rmi.RemoteException, HibernateException;

   public java.util.List listPeople()
      throws java.rmi.RemoteException, HibernateException;

}

