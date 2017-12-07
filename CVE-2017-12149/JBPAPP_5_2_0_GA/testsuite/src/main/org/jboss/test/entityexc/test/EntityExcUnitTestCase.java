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
package org.jboss.test.entityexc.test;


import java.util.Collection;
import java.util.Iterator;

import javax.ejb.Handle;
import javax.ejb.HomeHandle;
import javax.ejb.CreateException;
import javax.ejb.FinderException;
import javax.ejb.EJBException;

import javax.naming.InitialContext;

import javax.transaction.TransactionRolledbackException;

import javax.rmi.PortableRemoteObject;

import org.jboss.test.entityexc.interfaces.EntityExcHome;
import org.jboss.test.entityexc.interfaces.EntityExc;
import org.jboss.test.entityexc.interfaces.MyAppException;

import junit.framework.Test;
import junit.framework.AssertionFailedError;

import org.jboss.test.JBossTestCase;


/**
 *  Test case for testing transactions and exceptions
 *  in entity beans.
 *
 *  @author <a href="mailto:osh@sparre.dk">Ole Husgaard</a>
 *  @version $Revision: 81036 $
 */
public class EntityExcUnitTestCase
   extends JBossTestCase
{
   /**
    *  Create a new test case instance.
    */
   public EntityExcUnitTestCase(String name)
   {
      super(name);
   }

   /**
    *  Re-deploy and reset the database.
    */
   private void reset()
      throws Exception
   {
      getLog().debug("     Resetting...");
      undeploy("entityexc.jar");
      deploy("entityexc.jar");
      getHome().resetDatabase();
      getLog().debug("     ...reset done OK");
   }

   /**
    *  Get a bean home interface.
    */
   private EntityExcHome getHome()
      throws Exception
   {
      return (EntityExcHome)getInitialContext().lookup(EntityExcHome.JNDI_NAME);
   }


   /**
    *  Test for bug #463548: Bug in cache / setRollbackOnly bug.
    */
   public void testBug463548()
      throws Exception
   {
      getLog().debug(
         "**************************************************************");
      getLog().debug("     testBug463548()");

      reset();

      boolean gotException = false;

      try {
         EntityExc bean = getHome().create(new Integer(1), EntityExc.EXC_CREATEEXCEPTION|EntityExc.F_FAIL_POSTCREATE|EntityExc.F_SETROLLBACKONLY);

         // No cache problem if failure happens in ejbCreate
         // EntityExc bean = getHome().create(new Integer(1), EntityExc.EXC_CREATEEXCEPTION|EntityExc.F_SETROLLBACKONLY);
      } catch (TransactionRolledbackException ex) {
         gotException = true;
         getLog().error("Got unexpected TransactionRolledbackException", ex);
         getLog().error("Container started the transaction, so we should get the CreateException");
         fail("EJB2.0 section 17.6.2.8 violation.");
      } catch (CreateException ex) {
         gotException = true;
         getLog().debug("Got expected CreateException", ex);
      } catch (Exception ex) {
         gotException = true;
         getLog().error("Unexpected exception", ex);
// Check if the bean instance was removed from the cache
// It isn't in the database, since the transaction was marked for rollback only
try {
   EntityExc bean = getHome().findByPrimaryKey(new Integer(1), 0);
   fail("Rolled back bean creation, but still in cache.");
} catch (AssertionFailedError ex2) {
   // Just re-throw this
   throw ex2;
} catch (FinderException ex2) {
         //ex2.printStackTrace();
         getLog().error("Got expected FinderException", ex2);
} catch (Throwable ex2) {
         //ex2.printStackTrace();
         getLog().error("Got unexpected exception", ex2);
}
         fail("Unexpected exception: " + ex);
      }

      if (!gotException)
         fail("Did not get expected CreateException.");

// Check if the bean instance was removed from the cache
// It isn't in the database, since the transaction was marked for rollback only
try {
   EntityExc bean = getHome().findByPrimaryKey(new Integer(1), 0);
   fail("Rolled back bean creation, but still in cache 2.");
} catch (AssertionFailedError ex2) {
   // Just re-throw this
   throw ex2;
} catch (FinderException ex2) {
         //ex2.printStackTrace();
         getLog().error("Got expected FinderException", ex2);
} catch (Throwable ex2) {
         //ex2.printStackTrace();
         getLog().error("Got unexpected exception", ex2);
}
      getLog().debug(
         "**************************************************************");
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(EntityExcUnitTestCase.class, "entityexc.jar");
   }
}
