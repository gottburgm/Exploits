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
package org.jboss.test.txiiop.test;

import javax.naming.Context;
import javax.rmi.PortableRemoteObject;
import javax.transaction.UserTransaction;

import junit.framework.Test;
import org.jboss.test.JBossIIOPTestCase;
import org.jboss.test.txiiop.interfaces.StatefulSession;
import org.jboss.test.txiiop.interfaces.StatefulSessionHome;

/** Tests of IIOP UserTransaction
 *
 *   @author kimptoc 
 *   @author Scott.Stark@jboss.org 
 *   @author d_jencks converted to JBossTestCase, added logging.
 *   @author reverbel@ime.usp.br 
 *   @version $Revision: 81036 $
 */
public class IIOPUserTransactionStressTestCase
   extends JBossIIOPTestCase
{
    // Constructors --------------------------------------------------
   public IIOPUserTransactionStressTestCase (String name)
   {
      super(name);
   }

   // Public --------------------------------------------------------

   public void testUserTx()
      throws Exception
   {
      getLog().debug("+++ testUsrTx");

      getLog().debug("Obtain home interface");
      // Create a new session object
      Context ctx  = getInitialContext();
      Object ref = ctx.lookup("txiiop/StatefulSessionBean");
      StatefulSessionHome home = 
         (StatefulSessionHome) PortableRemoteObject.narrow(
                                               ref, StatefulSessionHome.class);
      StatefulSession bean = home.create("testUserTx");

      bean.setCounter(100);
      getLog().debug("Try to instantiate a UserTransaction");
      UserTransaction userTx = (UserTransaction)ctx.lookup("UserTransaction");
      userTx.begin();
         bean.incCounter();
         bean.incCounter();
      userTx.commit();
      int counter = bean.getCounter();
      assertTrue("counter == 102", counter == 102);

      bean.setCounter(100);
      userTx.begin();
         bean.incCounter();
         bean.incCounter();
      userTx.rollback();
      counter = bean.getCounter();
      assertTrue("counter == 100", counter == 100);

      bean.remove();
   }

   public void testTxMandatory()
      throws Exception
   {
      getLog().debug("+++ testTxMandatory");

      getLog().debug("Obtain home interface");
      // Create a new session object
      Context ctx  = getInitialContext();
      Object ref = ctx.lookup("txiiop/StatefulSessionBean");
      StatefulSessionHome home = 
         (StatefulSessionHome) PortableRemoteObject.narrow(
                                               ref, StatefulSessionHome.class);
      StatefulSession bean = home.create("testTxMandatory");
      getLog().debug("Call txMandatoryMethod without a UserTransaction");
      try
      {
         bean.txMandatoryMethod("without a UserTransaction");
         getLog().debug("Should not get here!");
         fail("TransactionRequiredException should have been thrown");
      }
      catch (javax.transaction.TransactionRequiredException e)
      {
         getLog().debug("Expected exception: " + e);
      }
      getLog().debug("Begin UserTransaction");
      UserTransaction userTx = (UserTransaction)ctx.lookup("UserTransaction");
      userTx.begin();
         bean.txMandatoryMethod("within a UserTransaction");
      getLog().debug("Commit UserTransaction");
      userTx.commit();
      bean.remove();
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(IIOPUserTransactionStressTestCase.class, 
                            "txiiop.jar");
   }

}
