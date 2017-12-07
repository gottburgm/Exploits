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
package org.jboss.test.cmp2.lob;

import java.rmi.RemoteException;
import javax.ejb.EJBHome;
import javax.ejb.CreateException;
import javax.ejb.FinderException;
import java.util.Collection;

/**
 * Remote home interface for a LOBBean.
 *
 * @see javax.ejb.EJBHome
 *
 * @version <tt>$Revision: 86171 $</tt>
 * @author  <a href="mailto:steve@resolvesw.com">Steve Coy</a>
 *
 */
public interface LOBHome extends EJBHome
{
   // Constants -----------------------------------------------------
   String LOB_HOME_CONTEXT = "cmp2/lob/Lob";

   public LOB create(Integer id)
      throws CreateException, RemoteException;

   public LOB findByPrimaryKey(Integer id)
      throws FinderException, RemoteException;

   public Collection findAll()
      throws FinderException, RemoteException;

   public java.util.Collection select(java.lang.String query , java.lang.Object[] params) 
      throws javax.ejb.FinderException, RemoteException;
}
