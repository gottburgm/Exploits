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

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.InitialContext;

import org.jboss.logging.Logger;
import org.jboss.mx.util.MBeanServerLocator;

/**
 * RollbackOnlyReleaseConnectionSessionBean.java
 *
 * @author <a href="mailto:noel.rocher@jboss.org">Noel Rocher</a>
 * @version <tt>$Revision: 81036 $</tt>
 *
 * @ejb.bean name="RollbackOnlyReleaseConnectionSession"
 *           display-name="Name for RollbackOnlyReleaseConnectionSession"
 *           description="Description for RollbackOnlyReleaseConnectionSession"
 *           jndi-name="RollbackOnlyReleaseConnectionSession"
 *           type="Stateless"
 *           view-type="remote"
 * 			 transaction-type = "Container"
 * @ejb.transaction type = "Required" 
 */
public class RollbackOnlyReleaseConnectionSessionBean implements SessionBean
{

   /** The serialVersionUID */
   private static final long serialVersionUID = 1L;

   private SessionContext bean_context;

   private Logger log = Logger.getLogger(getClass());

   public void ejbCreate() throws CreateException
   {
   }

   /**
    * 
    */
   public RollbackOnlyReleaseConnectionSessionBean()
   {
      super();
   }

   /**
    * @ejb.interface-method view-type = "both" 
    * @param in_name
    */
   public boolean testConnectionRelease() throws java.rmi.RemoteException
   {
      Connection conn = null;
      long pre_connection_number = 0;
      long post_connection_number = 0;
      boolean result = false;
      try
      {
         // set Transaction to Rollback Only
         this.bean_context.setRollbackOnly();

         Thread.sleep(500); // simulate some processing

         javax.sql.DataSource ds = (javax.sql.DataSource) (new InitialContext()).lookup("java:DefaultDS");
         pre_connection_number = getConnectionInUseNumber();
         conn = ds.getConnection();
         conn.createStatement().execute("select 1");
         conn.close();

      }
      catch (SQLException e)
      {// this is the expected exception}
      }
      catch (Exception e)
      {
         log.warn("Unexpected ", e);
         throw new EJBException("unexpected exception: " + e);
      }
      finally
      {
         try
         {
            conn.close();
         }
         catch (Exception ignore)
         {
         }
      }

      // compare in use connection numbers
      try
      {
         post_connection_number = getConnectionInUseNumber();
         log.debug("Pre # = " + pre_connection_number + " ; Post #" + post_connection_number);
         if (pre_connection_number == post_connection_number)
         {
            log.debug("Test is OK ");
            result = true;
         }
         else
         {
            log.debug("Test is *NOT* OK ");
            result = false;
         }
      }
      catch (Exception e)
      {
         log.warn("Unexpected: ", e);
         throw new EJBException("unexpected exception: " + e);
      }

      return result;

   }

   private long getConnectionInUseNumber() throws Exception
   {
      long result = 0;
      MBeanServer server = MBeanServerLocator.locateJBoss();
      result = ((Long) server.getAttribute(
            new ObjectName("jboss.jca:name=DefaultDS,service=ManagedConnectionPool"), "InUseConnectionCount"))
            .longValue();
      return result;
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

   public void setSessionContext(SessionContext ctx) throws EJBException, RemoteException
   {

      bean_context = ctx;
   }

}
