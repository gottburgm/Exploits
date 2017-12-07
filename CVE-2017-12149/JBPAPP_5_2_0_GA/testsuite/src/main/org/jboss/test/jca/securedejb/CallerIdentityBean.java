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
package org.jboss.test.jca.securedejb;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.SQLException;
import java.security.Principal;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.EJBException;
import javax.naming.InitialContext;
import javax.naming.directory.DirContext;
import javax.sql.DataSource;

import org.jboss.logging.Logger;
import org.jboss.test.jca.fs.DirContextFactory;

/** An ejb for testing the ejb caller identity propagation
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public class CallerIdentityBean implements SessionBean 
{
   static Logger log = Logger.getLogger(CallerIdentityBean.class);
   private SessionContext ctx;

   public void ejbCreate()
   {
   }
   public void ejbActivate()
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
      this.ctx = ctx;
   }
   public void unsetSessionContext() throws RemoteException
   {
      this.ctx = null;
   }

   public void useCallerForAuth()
   {
      try
      {
         Principal caller = ctx.getCallerPrincipal();
         String name0 = caller.getName();
         boolean isCallerIdentityUser = ctx.isCallerInRole("CallerIdentityUser");
         boolean isUseCallerForAuth = ctx.isCallerInRole("UseCallerForAuth");
         log.info("useCallerForAuth#0, caller="+caller
            +", isCallerIdentityUser="+isCallerIdentityUser
            +", isUseCallerForAuth="+isUseCallerForAuth);
         InitialContext enc = new InitialContext();
         DataSource ds = (DataSource) enc.lookup("java:comp/env/jdbc/CallerIdentityDS");
         testConnection(ds);
         caller = ctx.getCallerPrincipal();
         String name1 = caller.getName();
         isCallerIdentityUser = ctx.isCallerInRole("CallerIdentityUser");
         isUseCallerForAuth = ctx.isCallerInRole("UseCallerForAuth");
         log.info("useCallerForAuth#1, caller="+caller
            +", isCallerIdentityUser="+isCallerIdentityUser
            +", isUseCallerForAuth="+isUseCallerForAuth);
         if( name0.equals(name1) == false )
            throw new EJBException(name0+" != "+name1);
         if( isCallerIdentityUser == false || isUseCallerForAuth == false )
            throw new EJBException("Lost CallerIdentityUser, UseCallerForAuth roles");
      }
      catch(Exception e)
      {
         throw new EJBException(e);
      }
   }

   public void useConfiguredForAuth()
   {
      try
      {
         Principal caller = ctx.getCallerPrincipal();
         String name0 = caller.getName();
         boolean isCallerIdentityUser = ctx.isCallerInRole("CallerIdentityUser");
         boolean isUseConfiguredForAuth = ctx.isCallerInRole("UseConfiguredForAuth");
         log.info("useConfiguredForAuth#0, caller="+caller
            +", isCallerIdentityUser="+isCallerIdentityUser
            +", isUseConfiguredForAuth="+isUseConfiguredForAuth);
         InitialContext enc = new InitialContext();
         DataSource ds = (DataSource) enc.lookup("java:comp/env/jdbc/ConfiguredIdentityDS");
         testConnection(ds);
         caller = ctx.getCallerPrincipal();
         String name1 = caller.getName();
         isCallerIdentityUser = ctx.isCallerInRole("CallerIdentityUser");
         isUseConfiguredForAuth = ctx.isCallerInRole("UseConfiguredForAuth");
         log.info("useConfiguredForAuth#1, caller="+caller
            +", isCallerIdentityUser="+isCallerIdentityUser
            +", isUseConfiguredForAuth="+isUseConfiguredForAuth);
         if( name0.equals(name1) == false )
            throw new EJBException(name0+" != "+name1);
         if( isCallerIdentityUser == false || isUseConfiguredForAuth == false )
            throw new EJBException("Lost CallerIdentityUser, UseConfiguredForAuth roles");

         // Access the connection again
         ds = (DataSource) enc.lookup("java:comp/env/jdbc/ConfiguredIdentityDS");
         for(int n = 0; n < 1000; n ++)
         {
            testConnection(ds);
         }
         caller = ctx.getCallerPrincipal();
         String name2 = caller.getName();
         isCallerIdentityUser = ctx.isCallerInRole("CallerIdentityUser");
         isUseConfiguredForAuth = ctx.isCallerInRole("UseConfiguredForAuth");
         log.info("useRunAsForAuthDS#2, caller="+caller
            +", isCallerIdentityUser="+isCallerIdentityUser
            +", isUseConfiguredForAuth="+isUseConfiguredForAuth);
         if( name0.equals(name2) == false )
            throw new EJBException(name0+" != "+name2);
         if( isCallerIdentityUser == false || isUseConfiguredForAuth == false )
            throw new EJBException("Lost CallerIdentityUser, UseConfiguredForAuth roles");
      }
      catch(Exception e)
      {
         throw new EJBException(e);
      }
   }

   public void useRunAsForAuthDS()
   {
      try
      {
         Principal caller = ctx.getCallerPrincipal();
         String name0 = caller.getName();
         boolean isCallerIdentityUser = ctx.isCallerInRole("CallerIdentityUser");
         boolean isUseConfiguredForAuth = ctx.isCallerInRole("UseConfiguredForAuth");
         log.info("useRunAsForAuthDS#0, caller="+caller
            +", isCallerIdentityUser="+isCallerIdentityUser
            +", isUseConfiguredForAuth="+isUseConfiguredForAuth);
         InitialContext enc = new InitialContext();
         DataSource ds = (DataSource) enc.lookup("java:comp/env/jdbc/RunAsIdentityDS");
         testConnection(ds);
         caller = ctx.getCallerPrincipal();
         String name1 = caller.getName();
         isCallerIdentityUser = ctx.isCallerInRole("CallerIdentityUser");
         isUseConfiguredForAuth = ctx.isCallerInRole("UseConfiguredForAuth");
         log.info("useRunAsForAuthDS#1, caller="+caller
            +", isCallerIdentityUser="+isCallerIdentityUser
            +", isUseConfiguredForAuth="+isUseConfiguredForAuth);
         if( name0.equals(name1) == false )
            throw new EJBException(name0+" != "+name1);
         if( isCallerIdentityUser == false || isUseConfiguredForAuth == false )
            throw new EJBException("Lost CallerIdentityUser, UseConfiguredForAuth roles");

         // Access the connection again
         ds = (DataSource) enc.lookup("java:comp/env/jdbc/RunAsIdentityDS");
         for(int n = 0; n < 1000; n ++)
         {
            testConnection(ds);
         }
         caller = ctx.getCallerPrincipal();
         String name2 = caller.getName();
         isCallerIdentityUser = ctx.isCallerInRole("CallerIdentityUser");
         isUseConfiguredForAuth = ctx.isCallerInRole("UseConfiguredForAuth");
         log.info("useRunAsForAuthDS#2, caller="+caller
            +", isCallerIdentityUser="+isCallerIdentityUser
            +", isUseConfiguredForAuth="+isUseConfiguredForAuth);
         if( name0.equals(name2) == false )
            throw new EJBException(name0+" != "+name2);
         if( isCallerIdentityUser == false || isUseConfiguredForAuth == false )
            throw new EJBException("Lost CallerIdentityUser, UseConfiguredForAuth roles");
      }
      catch(Exception e)
      {
         throw new EJBException(e);
      }      
   }

   public void useRunAsForAuthFS()
   {
      try
      {
         Principal caller = ctx.getCallerPrincipal();
         String name0 = caller.getName();
         boolean isCallerIdentityUser = ctx.isCallerInRole("CallerIdentityUser");
         boolean isUseConfiguredForAuth = ctx.isCallerInRole("UseConfiguredForAuth");
         log.info("useRunAsForAuthFS#0, caller="+caller
            +", isCallerIdentityUser="+isCallerIdentityUser
            +", isUseConfiguredForAuth="+isUseConfiguredForAuth);
         InitialContext enc = new InitialContext();
         DirContextFactory dcf = (DirContextFactory) enc.lookup("java:comp/env/jndi/RunAsIdentityFS");
         DirContext dc = dcf.getConnection();
         caller = ctx.getCallerPrincipal();
         dc.close();
         String name1 = caller.getName();
         isCallerIdentityUser = ctx.isCallerInRole("CallerIdentityUser");
         isUseConfiguredForAuth = ctx.isCallerInRole("UseConfiguredForAuth");
         log.info("useRunAsForAuthFS#1, caller="+caller
            +", isCallerIdentityUser="+isCallerIdentityUser
            +", isUseConfiguredForAuth="+isUseConfiguredForAuth);
         if( name0.equals(name1) == false )
            throw new EJBException(name0+" != "+name1);
         if( isCallerIdentityUser == false || isUseConfiguredForAuth == false )
            throw new EJBException("Lost CallerIdentityUser, UseConfiguredForAuth roles");
      }
      catch(Exception e)
      {
         throw new EJBException(e);
      }      
   }

   private void testConnection(DataSource ds) throws SQLException
   {
      Connection conn = ds.getConnection();
      conn.close();
   }
}
