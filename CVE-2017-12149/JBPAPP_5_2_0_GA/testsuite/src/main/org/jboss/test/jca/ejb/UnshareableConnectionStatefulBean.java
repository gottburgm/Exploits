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

import java.sql.Connection;
import java.sql.SQLException;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.InitialContext;
import javax.sql.DataSource;

/**
 * A stateful session bean that has an unshareable resource
 *
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @version <tt>$Revision: 81036 $</tt>
 */
public class UnshareableConnectionStatefulBean
   implements SessionBean
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 1L;

   Connection c;

   public void runTestPart1()
   {
      try
      {
         if (c.getAutoCommit())
            throw new EJBException("Autocommit should be off");
      }
      catch (SQLException e)
      {
         throw new EJBException(e.toString());
      }
   }

   public void runTestPart2()
   {
      try
      {
         c.commit();
      }
      catch (SQLException e)
      {
         throw new EJBException(e.toString());
      }
   }

   public void ejbCreate()
      throws CreateException
   {
      initConnection();
   }

   public void ejbActivate()
   {
      initConnection();
   }

   public void ejbPassivate()
   {
      termConnection();
   }

   public void ejbRemove()
   {
      termConnection();
   }

   public void initConnection()
   {
      if (c != null)
         throw new EJBException("Connection already inited");

      try
      {
         DataSource ds = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/DataSource");
         c = ds.getConnection();
         c.setAutoCommit(false);
      }
      catch (Exception e)
      {
         throw new EJBException(e.toString());
      }
   }

   public void termConnection()
   {
      if (c == null)
         throw new EJBException("Connection already terminated");

      try
      {
         c.close();
         c = null;
      }
      catch (Exception e)
      {
         throw new EJBException(e.toString());
      }
   }

   public void setSessionContext(SessionContext ctx)
   {
   }

   public void unsetSessionContext()
   {
   }
}

