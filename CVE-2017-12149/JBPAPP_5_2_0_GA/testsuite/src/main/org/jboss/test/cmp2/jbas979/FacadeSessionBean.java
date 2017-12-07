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
package org.jboss.test.cmp2.jbas979;

import java.sql.Connection;
import java.sql.PreparedStatement;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.NamingException;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import org.jboss.mx.util.MBeanServerLocator;
import org.jboss.ejb.plugins.cmp.jdbc.JDBCUtil;

/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 81036 $</tt>
 */
public class FacadeSessionBean
   implements SessionBean
{
   // Business methods

   public void create(String ejbJndiName, Integer pk, String name) throws Exception
   {
      ALocalHome ah = getALocalHome(ejbJndiName);
      ah.create(pk, name);
   }

   public void remove(String ejbJndiName, Integer pk) throws Exception
   {
      ALocalHome ah = getALocalHome(ejbJndiName);
      ah.remove(pk);
   }

   public String getName(String ejbJndiName, Integer pk) throws Exception
   {
      ALocalHome ah = getALocalHome(ejbJndiName);
      return ah.findByPrimaryKey(pk).getName();
   }

   public String getNameFlushCacheGetName(String ejbJndiName, Integer pk) throws Exception
   {
      ALocalHome ah = getALocalHome(ejbJndiName);
      String nameBeforeFlush = ah.findByPrimaryKey(pk).getName();
      flushCache(ejbJndiName);
      String nameAfterFlush = ah.findByPrimaryKey(pk).getName();
      if(!nameBeforeFlush.equals(nameAfterFlush))
      {
         throw new EJBException(
            "The value of the name field before flush (" + nameBeforeFlush +
            ") is not equal to the value after flush (" + nameAfterFlush + ")!");
      }
      return nameAfterFlush;
   }

   public String getNameFlushCacheSetName(String ejbJndiName, Integer pk, String value) throws Exception
   {
      ALocalHome ah = getALocalHome(ejbJndiName);
      ah.findByPrimaryKey(pk).getName();
      flushCache(ejbJndiName);
      ALocal a = ah.findByPrimaryKey(pk);
      a.setName(value);
      String name = a.getName();
      if(!name.equals(value))
      {
         throw new EJBException("setName(" + value + ") was ignored: " + name);
      }
      return name;
   }

   public void updateDB(String tableName, Integer pk, String value) throws Exception
   {
      DataSource ds = (DataSource)lookup("java:/DefaultDS");
      Connection con = null;
      PreparedStatement st = null;
      try
      {
         con = ds.getConnection();
         st = con.prepareStatement("update " + tableName + " set name=? where id=?");
         st.setString(1, value);
         st.setInt(2, pk.intValue());
         int rowsAffected = st.executeUpdate();
         if(rowsAffected != 1)
         {
            throw new EJBException("Failed to update column name in the row with pk " + pk +
               " in the table " + tableName + ": expected 1 updated row but got " + rowsAffected);
         }
      }
      finally
      {
         JDBCUtil.safeClose(st);
         JDBCUtil.safeClose(con);
      }
   }

   public void flushCache(String ejbJndiName) throws Exception
   {
      MBeanServer server = MBeanServerLocator.locateJBoss();
      ObjectName container = new ObjectName("jboss.j2ee:service=EJB,jndiName=" + ejbJndiName);
      server.invoke(container, "flushCache", null, null);
   }

   public void longTx(String ejbJndiName, Integer pk, long ms) throws Exception
   {
      ALocalHome ah = getALocalHome(ejbJndiName);
      ah.findByPrimaryKey(pk).longTx();
      try
      {
         Thread.sleep(ms);
      }
      catch(InterruptedException e)
      {
      }
   }

   // SessionBean implementation

   /**
    * @exception  javax.ejb.CreateException Description of Exception
    * @ejb.create-method
    */
   public void ejbCreate() throws CreateException
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

   // Private

   private ALocalHome getALocalHome(String name)
      throws NamingException
   {
      return (ALocalHome)lookup(name);
   }

   private Object lookup(String name) throws NamingException
   {
      InitialContext ic = null;
      try
      {
         ic = new InitialContext();
         return ic.lookup(name);
      }
      finally
      {
         if(ic != null)
         {
            ic.close();
         }
      }
   }
}
