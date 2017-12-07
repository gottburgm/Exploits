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
import java.util.Vector;
import java.util.Collection;

import java.sql.*;

import javax.naming.*;
import javax.ejb.*;

/**
 * @author Bill Burke
 * @version $Revision: 81036 $
 */ 
public class SimpleBMPBean
   implements EntityBean
{
   EntityContext ctx = null;

   // bmp fields
   Integer id;
   String name;
   
   public Integer ejbCreate (int _id, String _name)
      throws CreateException, RemoteException
   {
      id = new Integer (_id);
      name = _name;
      return id;      
   }
   
   public void ejbPostCreate (int _id, String _name)
      throws CreateException, RemoteException
   {
   }
   
   public void ejbLoad ()
   {
   }
   
   public void ejbStore ()
   {
   }
   
   public void ejbRemove ()
   {
   }

   
   public Integer ejbFindByPrimaryKey (Integer _key) throws FinderException
   {
      return _key;
   }

   public void ejbActivate ()
   {
   }
   
   public void ejbPassivate ()
   {
   }
   
   public void setEntityContext (EntityContext _ctx)
   {
      ctx = _ctx;
   }
   
   public void unsetEntityContext ()
   {
      ctx = null;
   }
   
   // business methods ---------------------------------------------------------------
   
   public void setName (String _name)
   {
      name = _name;
   }
   
   public String getName ()
   {
      return name;
   }
   
   
}
