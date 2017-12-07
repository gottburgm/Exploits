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
package org.jboss.test.perf.ejb;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.util.Collection;
import java.util.Iterator;
import javax.ejb.CreateException;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;
import javax.ejb.SessionContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

import org.jboss.logging.Logger;

import org.jboss.test.perf.interfaces.EntityLocalHome;
import org.jboss.test.perf.interfaces.EntityLocal;
import org.jboss.test.perf.interfaces.EntityPK;

public class SessionBean implements javax.ejb.SessionBean
{
   private static Logger log = Logger.getLogger(SessionBean.class);
   private EntityLocalHome entityHome;

   public void setSessionContext(SessionContext context)
   {
   }

   public void ejbCreate(String entityName) throws CreateException
   {
      try
      {
         Context context = new InitialContext();
         Object ref = context.lookup("java:comp/env/"+entityName);
         entityHome = (EntityLocalHome) PortableRemoteObject.narrow(ref, EntityLocalHome.class);
      }
      catch(NamingException e)
      {
         throw new CreateException("Cound not resolve name: " + e);
      }
   }

   private EntityLocal findByPrimaryKey(int key) throws FinderException
   {
      EntityPK primaryKey = new EntityPK(key);
      return entityHome.findByPrimaryKey(primaryKey);
   }

   private Collection findInRange(int min, int max) throws FinderException
   {
      return entityHome.findInRange(min, max);
   }

   public void create(int low, int high)
      throws CreateException
   {
      for(int i = low; i < high; i++)
      {
         entityHome.create(i, 0);
      }
   }
   
   public void remove(int low, int high)
      throws RemoveException
   {
      if(low + 1 == high)
      {
         try
         {
            EntityLocal entity = findByPrimaryKey(low);
            entity.remove();
         }
         catch(FinderException e)
         {
            log.error("Failed to find and remove entity", e);
            throw new RemoveException("Failed to find and remove entity");
         }
      }
      else
      {
         //There is no find in range finder! till someone implements it...
         //java.util.Enumeration elements = findInRange(low, high);
         int count = 0;
         for (int i = low; i < high; i++)
         {
            try 
            {
               EntityLocal entity = findByPrimaryKey(i);
               entity.remove();
               count++;
            }
            catch (Exception e)
            {
               //ignore
            } // end of try-catch
         } // end of for ()

         if( count != (high-low) )
         {
            throw new RemoveException("Removed "+count+" but should remove:"+(high-low));
         }
      }
   }

   public void read(int id) throws RemoteException
   {
      try
      {
         EntityLocal entity = findByPrimaryKey(id);
         entity.read();
      }
      catch(FinderException e)
      {
         throw new ServerException("findByPrimaryKey failed for id="+id, e);
      }
   }

   public void read(int low, int high) throws RemoteException
   {
      Collection elements = null;
      try
      {
         elements = findInRange(low, high);
      }
      catch(FinderException e)
      {
         throw new ServerException("findInRange failed for low="+low+", high="+high, e);
      }

      Iterator iter = elements.iterator();
      while( iter.hasNext() )
      {
         EntityLocal entity = (EntityLocal) iter.next();
         entity.read();
      }
   }

   public void write(int id) throws RemoteException
   {
      try
      {
         EntityLocal entity = findByPrimaryKey(id);
         int value = entity.read();
         entity.write(value + 1);
      }
      catch(FinderException e)
      {
         throw new ServerException("findByPrimaryKey failed for id="+id, e);
      }
   }

   public void write(int low, int high) throws RemoteException
   {
      Collection elements = null;
      try
      {
         elements = findInRange(low, high);
      }
      catch(FinderException e)
      {
         throw new ServerException("findInRange failed for low="+low+", high="+high, e);
      }

      Iterator iter = elements.iterator();
      while( iter.hasNext() )
      {
         EntityLocal entity = (EntityLocal) iter.next();
         int value = entity.read();
         entity.write(value + 1);
      }
   }

   public void ejbRemove()
   {
   }
   
   public void ejbActivate()
   {
   }
   
   public void ejbPassivate()
   {
   }
   
}

