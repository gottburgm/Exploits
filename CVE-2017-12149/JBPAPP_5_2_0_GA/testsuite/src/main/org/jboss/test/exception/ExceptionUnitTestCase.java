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
package org.jboss.test.exception;

import java.rmi.RemoteException;
import java.rmi.AccessException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import javax.ejb.EJBException;
import javax.ejb.TransactionRolledbackLocalException;
import javax.ejb.AccessLocalException;
import javax.ejb.CreateException;
import javax.naming.InitialContext;
import javax.transaction.TransactionRolledbackException;

import junit.framework.Test;
import org.jboss.logging.Logger;
import org.jboss.test.JBossTestCase;
import org.jboss.test.util.ejb.EJBTestCase;

/**
 * Test ejb exception propagation
 *  
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */ 
public class ExceptionUnitTestCase extends EJBTestCase
{
   static Logger log = Logger.getLogger(ExceptionUnitTestCase.class);

   public static Test suite() throws Exception
   {
      return JBossTestCase.getDeploySetup(ExceptionUnitTestCase.class,
         "exception.jar");
   }

   public ExceptionUnitTestCase(String name)
   {
      super(name);
   }

   private ExceptionTesterHome exceptionTesterHome;
   private ExceptionTesterLocalHome exceptionTesterLocalHome;

   /**
    * Looks up all of the home interfaces and creates the initial data. Looking
    * up objects in JNDI is expensive, so it should be done once and cached.
    * @throws Exception if a problem occures while finding the home interfaces,
    * or if an problem occures while createing the initial data
    */
   public void setUp() throws Exception
   {
      InitialContext jndi = new InitialContext();
      exceptionTesterHome = (ExceptionTesterHome)
         jndi.lookup("exception/ExceptionTester");
      exceptionTesterLocalHome = (ExceptionTesterLocalHome)
         jndi.lookup("exception/ExceptionTesterLocal");
   }

   public void testSessionEjbCreateException() throws Exception
   {
      log.info("+++ testSessionEjbCreateException");
      InitialContext ctx = new InitialContext();
      ExceptionTesterHome home = (ExceptionTesterHome)
         ctx.lookup("exception/CreateExceptionTesterEJB");
      try
      {
         ExceptionTester bean = home.create();
         fail("Expected CreateException on ExceptionTesterHome.create(), bean="+bean);
      }
      catch(CreateException e)
      {
         log.info("Saw CreateException via remote as expected", e);
      }

      ExceptionTesterLocalHome lhome = (ExceptionTesterLocalHome)
         ctx.lookup("exception/CreateExceptionTesterLocalEJB");
      try
      {
         ExceptionTesterLocal bean = lhome.create();
         fail("Expected CreateException on ExceptionTesterLocalHome.create(), bean="+bean);
      }
      catch(CreateException e)
      {
         log.info("Saw CreateException via local as expected", e);
      }
   }

   public void testApplicationExceptionInTx_remote() throws Exception
   {
      ExceptionTester exceptionTester = null;
      try
      {
         exceptionTester = exceptionTesterHome.create();

         exceptionTester.applicationExceptionInTx();

         fail("Expected application exception to be thrown");

      }
      catch (ApplicationException e)
      {
         // good this was expected
      }
      catch (Exception e)
      {
         fail("Expected ApplicationException but got " + e);
      }
      finally
      {
         if (exceptionTester != null)
         {
            exceptionTester.remove();
         }
      }
   }

   public void testApplicationErrorInTx_remote() throws Exception
   {
      ExceptionTester exceptionTester = null;
      try
      {
         exceptionTester = exceptionTesterHome.create();

         exceptionTester.applicationErrorInTx();

         fail("Expected transaction rolled back exception to be thrown");

      }
      catch (TransactionRolledbackException e)
      {
         // good this was expected
         assertNotNull("TransactionRolledbackException.detail should not be null",
            e.detail);

         assertEquals("TransactionRolledbackException.detail should " +
            "be a ApplicationError",
            ApplicationError.class,
            e.detail.getClass());

      }
      catch (Exception e)
      {
         fail("Expected TransactionRolledbackException but got " + e);
      }
      finally
      {
         if (exceptionTester != null)
         {
            exceptionTester.remove();
         }
      }
   }

   public void testEJBExceptionInTx_remote() throws Exception
   {
      ExceptionTester exceptionTester = null;
      try
      {
         exceptionTester = exceptionTesterHome.create();

         exceptionTester.ejbExceptionInTx();

         fail("Expected transaction rolled back exception to be thrown");

      }
      catch (TransactionRolledbackException e)
      {
         // good this was expected
         assertNotNull("TransactionRolledbackException.detail should not be null",
            e.detail);
         assertEquals("TransactionRolledbackException.detail should " +
            "be an EJBException",
            EJBException.class,
            e.detail.getClass());

      }
      catch (Exception e)
      {
         fail("Expected TransactionRolledbackException but got " + e);
      }
      finally
      {
         if (exceptionTester != null)
         {
            exceptionTester.remove();
         }
      }
   }


   public void testRuntimeExceptionInTx_remote() throws Exception
   {
      ExceptionTester exceptionTester = null;
      try
      {
         exceptionTester = exceptionTesterHome.create();

         exceptionTester.runtimeExceptionInTx();

         fail("Expected transaction rolled back exception to be thrown");

      }
      catch (TransactionRolledbackException e)
      {
         // good this was expected
         assertNotNull("TransactionRolledbackException.detail should not be null",
            e.detail);

         assertEquals("TransactionRolledbackException.detail should " +
            "be a RuntimeException",
            RuntimeException.class,
            e.detail.getClass());

      }
      catch (Exception e)
      {
         fail("Expected TransactionRolledbackException but got " + e);
      }
      finally
      {
         if (exceptionTester != null)
         {
            exceptionTester.remove();
         }
      }
   }


   public void testRemoteExceptionInTx_remote() throws Exception
   {
      ExceptionTester exceptionTester = null;
      try
      {
         exceptionTester = exceptionTesterHome.create();

         exceptionTester.remoteExceptionInTx();

         fail("Expected transaction rolled back exception to be thrown");

      }
      catch (TransactionRolledbackException e)
      {
         // good this was expected
         assertNotNull("TransactionRolledbackException.detail should not be null",
            e.detail);
         assertEquals("TransactionRolledbackException.detail should " +
            "be a RemoteException",
            RemoteException.class,
            e.detail.getClass());

      }
      catch (Exception e)
      {
         fail("Expected TransactionRolledbackException but got " + e);
      }
      finally
      {
         if (exceptionTester != null)
         {
            exceptionTester.remove();
         }
      }
   }

   public void testApplicationExceptionNewTx_remote() throws Exception
   {
      ExceptionTester exceptionTester = null;
      try
      {
         exceptionTester = exceptionTesterHome.create();

         exceptionTester.applicationExceptionNewTx();

         fail("Expected application exception to be thrown");

      }
      catch (ApplicationException e)
      {
         // good this was expected
      }
      catch (Exception e)
      {
         fail("Expected ApplicationException but got " + e);
      }
      finally
      {
         if (exceptionTester != null)
         {
            exceptionTester.remove();
         }
      }
   }

   public void testApplicationErrorNewTx_remote() throws Exception
   {
      ExceptionTester exceptionTester = null;
      try
      {
         exceptionTester = exceptionTesterHome.create();

         exceptionTester.applicationErrorNewTx();

         fail("Expected RemoteException to be thrown");

      }
      catch (RemoteException e)
      {
         // good this was expected
         assertNotNull("RemoteException.detail should not be null",
            e.detail);

         assertEquals("RemoteException.detail should be a ApplicationError",
            ApplicationError.class,
            e.detail.getClass());
      }
      catch (Exception e)
      {
         fail("Expected RemoteException but got " + e);
      }
      finally
      {
         if (exceptionTester != null)
         {
            exceptionTester.remove();
         }
      }
   }

   public void testEJBExceptionNewTx_remote() throws Exception
   {
      ExceptionTester exceptionTester = null;
      try
      {
         exceptionTester = exceptionTesterHome.create();

         exceptionTester.ejbExceptionNewTx();

         fail("Expected RemoteException to be thrown");

      }
      catch (RemoteException e)
      {
         // good this was expected
         assertNotNull("RemoteException.detail should not be null",
            e.detail);

         assertEquals("RemoteException.detail should be a EJBException",
            EJBException.class,
            e.detail.getClass());
      }
      catch (Exception e)
      {
         fail("Expected RemoteException but got " + e);
      }
      finally
      {
         if (exceptionTester != null)
         {
            exceptionTester.remove();
         }
      }
   }


   public void testRuntimeExceptionNewTx_remote() throws Exception
   {
      ExceptionTester exceptionTester = null;
      try
      {
         exceptionTester = exceptionTesterHome.create();

         exceptionTester.runtimeExceptionNewTx();

         fail("Expected RemoteException to be thrown");

      }
      catch (RemoteException e)
      {
         // good this was expected
         assertNotNull("RemoteException.detail should not be null",
            e.detail);

         assertEquals("RemoteException.detail should be a RuntimeException",
            RuntimeException.class,
            e.detail.getClass());

      }
      catch (Exception e)
      {
         fail("Expected RemoteException but got " + e);
      }
      finally
      {
         if (exceptionTester != null)
         {
            exceptionTester.remove();
         }
      }
   }


   public void testRemoteExceptionNewTx_remote() throws Exception
   {
      ExceptionTester exceptionTester = null;
      try
      {
         exceptionTester = exceptionTesterHome.create();

         exceptionTester.remoteExceptionNewTx();

         fail("Expected RemoteException to be thrown");

      }
      catch (RemoteException e)
      {
         // good this was expected
      }
      catch (Exception e)
      {
         fail("Expected RemoteException but got " + e);
      }
      finally
      {
         if (exceptionTester != null)
         {
            exceptionTester.remove();
         }
      }
   }

   public void testApplicationExceptionNoTx_remote() throws Exception
   {
      ExceptionTester exceptionTester = null;
      try
      {
         exceptionTester = exceptionTesterHome.create();

         exceptionTester.applicationExceptionNoTx();

         fail("Expected application exception to be thrown");

      }
      catch (ApplicationException e)
      {
         // good this was expected
      }
      catch (Exception e)
      {
         fail("Expected ApplicationException but got " + e);
      }
      finally
      {
         if (exceptionTester != null)
         {
            exceptionTester.remove();
         }
      }
   }

   public void testApplicationErrorNoTx_remote() throws Exception
   {
      ExceptionTester exceptionTester = null;
      try
      {
         exceptionTester = exceptionTesterHome.create();

         exceptionTester.applicationErrorNoTx();

         fail("Expected RemoteException to be thrown");

      }
      catch (RemoteException e)
      {
         // good this was expected
         assertNotNull("RemoteException.detail should not be null",
            e.detail);

         assertEquals("RemoteException.detail should be a ApplicationError",
            ApplicationError.class,
            e.detail.getClass());
      }
      catch (Exception e)
      {
         fail("Expected RemoteException but got " + e);
      }
      finally
      {
         if (exceptionTester != null)
         {
            exceptionTester.remove();
         }
      }
   }

   public void testEJBExceptionNoTx_remote() throws Exception
   {
      ExceptionTester exceptionTester = null;
      try
      {
         exceptionTester = exceptionTesterHome.create();

         exceptionTester.ejbExceptionNoTx();

         fail("Expected RemoteException to be thrown");

      }
      catch (RemoteException e)
      {
         // good this was expected
         assertNotNull("RemoteException.detail should not be null",
            e.detail);

         assertEquals("RemoteException.detail should be a EJBException",
            EJBException.class,
            e.detail.getClass());
      }
      catch (Exception e)
      {
         fail("Expected RemoteException but got " + e);
      }
      finally
      {
         if (exceptionTester != null)
         {
            exceptionTester.remove();
         }
      }
   }


   public void testRuntimeExceptionNoTx_remote() throws Exception
   {
      ExceptionTester exceptionTester = null;
      try
      {
         exceptionTester = exceptionTesterHome.create();

         exceptionTester.runtimeExceptionNoTx();

         fail("Expected RemoteException to be thrown");

      }
      catch (RemoteException e)
      {
         // good this was expected
         assertNotNull("RemoteException.detail should not be null",
            e.detail);

         assertEquals("RemoteException.detail should be a RuntimeException",
            RuntimeException.class,
            e.detail.getClass());

      }
      catch (Exception e)
      {
         fail("Expected RemoteException but got " + e);
      }
      finally
      {
         if (exceptionTester != null)
         {
            exceptionTester.remove();
         }
      }
   }


   public void testRemoteExceptionNoTx_remote() throws Exception
   {
      ExceptionTester exceptionTester = null;
      try
      {
         exceptionTester = exceptionTesterHome.create();

         exceptionTester.remoteExceptionNoTx();

         fail("Expected RemoteException to be thrown");

      }
      catch (RemoteException e)
      {
         // good this was expected
      }
      catch (Exception e)
      {
         fail("Expected RemoteException but got " + e);
      }
      finally
      {
         if (exceptionTester != null)
         {
            exceptionTester.remove();
         }
      }
   }

   public void testSecurityException_remote() throws Exception
   {
      ExceptionTester exceptionTester = null;
      try
      {
         InitialContext jndi = new InitialContext();
         ExceptionTesterHome exTesterHome =
            (ExceptionTesterHome) jndi.lookup("exception/SecuredExceptionTester");
         exceptionTester = exTesterHome.create();
         exceptionTester.securityExceptionNoTx();

         fail("Expected AccessException to be thrown");

      }
      catch (AccessException e)
      {
         // good this was expected
      }
      catch (Exception e)
      {
         fail("Expected AccessException but got " + e);
      }
      finally
      {
         if (exceptionTester != null)
         {
            exceptionTester.remove();
         }
      }
   }

   public void testSecurityExceptionByAppNoTx_remote() throws Exception
   {
      ExceptionTester exceptionTester = null;
      try
      {
         InitialContext jndi = new InitialContext();
         ExceptionTesterHome exTesterHome =
            (ExceptionTesterHome) jndi.lookup("exception/ExceptionTester");
         exceptionTester = exTesterHome.create();
         exceptionTester.securityExceptionByAppNoTx();

         fail("Expected InvalidKeyException to be thrown");

      }
      catch (InvalidKeyException e)
      {
         // good this was expected
      }
      catch (Exception e)
      {
         fail("Expected InvalidKeyException but got " + e);
      }
      finally
      {
         if (exceptionTester != null)
         {
            exceptionTester.remove();
         }
      }
   }

   public void testApplicationExceptionInTx_local() throws Exception
   {
      ExceptionTesterLocal exceptionTester = null;
      try
      {
         exceptionTester = exceptionTesterLocalHome.create();

         exceptionTester.applicationExceptionInTx();

         fail("Expected ApplicationException to be thrown");

      }
      catch (ApplicationException e)
      {
         // good this was expected
      }
      catch (Exception e)
      {
         fail("Expected ApplicationException but got " + e);
      }
      finally
      {
         if (exceptionTester != null)
         {
            exceptionTester.remove();
         }
      }
   }

   public void testApplicationErrorInTx_local() throws Exception
   {
      ExceptionTesterLocal exceptionTester = null;
      try
      {
         exceptionTester = exceptionTesterLocalHome.create();

         exceptionTester.applicationErrorInTx();

         fail("Expected TransactionRolledbackLocalException to be thrown");

      }
      catch (TransactionRolledbackLocalException e)
      {
         // good this was expected
         assertNotNull("TransactionRolledbackLocalException.getCausedByException() " +
            "should not be null",
            e.getCausedByException());

         assertEquals("TransactionRolledbackLocalException.getCausedByExcption() " +
            "should be an EJBException",
            EJBException.class,
            e.getCausedByException().getClass());
      }
      catch (Exception e)
      {
         fail("Expected TransactionRolledbackLocalException but got " + e);
      }
      finally
      {
         if (exceptionTester != null)
         {
            exceptionTester.remove();
         }
      }
   }


   public void testEJBExceptionInTx_local() throws Exception
   {
      ExceptionTesterLocal exceptionTester = null;
      try
      {
         exceptionTester = exceptionTesterLocalHome.create();

         exceptionTester.ejbExceptionInTx();

         fail("Expected TransactionRolledbackLocalException to be thrown");

      }
      catch (TransactionRolledbackLocalException e)
      {
         // good this was expected
         assertNotNull("TransactionRolledbackLocalException.getCausedByException() " +
            "should not be null",
            e.getCausedByException());

         assertEquals("TransactionRolledbackLocalException.getCausedByException() " +
            "should be an EJBException",
            EJBException.class,
            e.getCausedByException().getClass());

      }
      catch (Exception e)
      {
         fail("Expected TransactionRolledbackLocalException but got " + e);
      }
      finally
      {
         if (exceptionTester != null)
         {
            exceptionTester.remove();
         }
      }
   }


   public void testRuntimeExceptionInTx_local() throws Exception
   {
      ExceptionTesterLocal exceptionTester = null;
      try
      {
         exceptionTester = exceptionTesterLocalHome.create();

         exceptionTester.runtimeExceptionInTx();

         fail("Expected TransactionRolledbackLocalException to be thrown");

      }
      catch (TransactionRolledbackLocalException e)
      {
         // good this was expected
         assertNotNull("TransactionRolledbackLocalException.getCausedByException() " +
            "should not be null",
            e.getCausedByException());

         assertEquals("TransactionRolledbackLocalException.getCausedByException() " +
            "should be a RuntimeException",
            RuntimeException.class,
            e.getCausedByException().getClass());

      }
      catch (Exception e)
      {
         fail("Expected TransactionRolledbackLocalException but got " + e);
      }
      finally
      {
         if (exceptionTester != null)
         {
            exceptionTester.remove();
         }
      }
   }

   public void testApplicationExceptionNewTx_local() throws Exception
   {
      ExceptionTesterLocal exceptionTester = null;
      try
      {
         exceptionTester = exceptionTesterLocalHome.create();

         exceptionTester.applicationExceptionNewTx();

         fail("Expected ApplicationException to be thrown");

      }
      catch (ApplicationException e)
      {
         // good this was expected
      }
      catch (Exception e)
      {
         fail("Expected ApplicationException but got " + e);
      }
      finally
      {
         if (exceptionTester != null)
         {
            exceptionTester.remove();
         }
      }
   }

   public void testApplicationErrorNewTx_local() throws Exception
   {
      ExceptionTesterLocal exceptionTester = null;
      try
      {
         exceptionTester = exceptionTesterLocalHome.create();

         exceptionTester.applicationErrorNewTx();

         fail("Expected EJBException to be thrown");

      }
      catch (EJBException e)
      {
         // good this was expected
         assertNull("EJBException.getCausedByException() should be null",
            e.getCausedByException());
      }
      catch (Exception e)
      {
         fail("Expected EJBException but got " + e);
      }
      finally
      {
         if (exceptionTester != null)
         {
            exceptionTester.remove();
         }
      }
   }

   public void testEJBExceptionNewTx_local() throws Exception
   {
      ExceptionTesterLocal exceptionTester = null;
      try
      {
         exceptionTester = exceptionTesterLocalHome.create();

         exceptionTester.ejbExceptionNewTx();

         fail("Expected EJBException to be thrown");

      }
      catch (EJBException e)
      {
         // good this was expected
         assertNull("EJBException.getCausedByException() should be null",
            e.getCausedByException());
      }
      catch (Exception e)
      {
         fail("Expected EJBException but got " + e);
      }
      finally
      {
         if (exceptionTester != null)
         {
            exceptionTester.remove();
         }
      }
   }


   public void testRuntimeExceptionNewTx_local() throws Exception
   {
      ExceptionTesterLocal exceptionTester = null;
      try
      {
         exceptionTester = exceptionTesterLocalHome.create();

         exceptionTester.runtimeExceptionNewTx();

         fail("Expected EJBException to be thrown");

      }
      catch (EJBException e)
      {
         // good this was expected
         assertNotNull("EJBException.getCausedByException() should not be null",
            e.getCausedByException());

         assertEquals("EJBException.getCausedByException() should be " +
            "a RuntimeException",
            RuntimeException.class,
            e.getCausedByException().getClass());

      }
      catch (Exception e)
      {
         fail("Expected EJBException but got " + e);
      }
      finally
      {
         if (exceptionTester != null)
         {
            exceptionTester.remove();
         }
      }
   }

   public void testApplicationExceptionNoTx_local() throws Exception
   {
      ExceptionTesterLocal exceptionTester = null;
      try
      {
         exceptionTester = exceptionTesterLocalHome.create();

         exceptionTester.applicationExceptionNoTx();

         fail("Expected application exception to be thrown");

      }
      catch (ApplicationException e)
      {
         // good this was expected
      }
      catch (Exception e)
      {
         fail("Expected ApplicationException but got " + e);
      }
      finally
      {
         if (exceptionTester != null)
         {
            exceptionTester.remove();
         }
      }
   }

   public void testApplicationErrorNoTx_local() throws Exception
   {
      ExceptionTesterLocal exceptionTester = null;
      try
      {
         exceptionTester = exceptionTesterLocalHome.create();

         exceptionTester.applicationErrorNoTx();

         fail("Expected EJBException to be thrown");

      }
      catch (EJBException e)
      {
         // good this was expected
         assertNull("EJBException.getCausedByException() should be null",
            e.getCausedByException());
      }
      catch (Exception e)
      {
         fail("Expected EJBException but got " + e);
      }
      finally
      {
         if (exceptionTester != null)
         {
            exceptionTester.remove();
         }
      }
   }

   public void testEJBExceptionNoTx_local() throws Exception
   {
      ExceptionTesterLocal exceptionTester = null;
      try
      {
         exceptionTester = exceptionTesterLocalHome.create();

         exceptionTester.ejbExceptionNoTx();

         fail("Expected EJBException to be thrown");

      }
      catch (EJBException e)
      {
         // good this was expected
         assertNull("EJBException.getCausedByException() should be null",
            e.getCausedByException());
      }
      catch (Exception e)
      {
         fail("Expected EJBException but got " + e);
      }
      finally
      {
         if (exceptionTester != null)
         {
            exceptionTester.remove();
         }
      }
   }


   public void testRuntimeExceptionNoTx_local() throws Exception
   {
      ExceptionTesterLocal exceptionTester = null;
      try
      {
         exceptionTester = exceptionTesterLocalHome.create();

         exceptionTester.runtimeExceptionNoTx();

         fail("Expected EJBException to be thrown");

      }
      catch (EJBException e)
      {
         // good this was expected
         assertNotNull("EJBException.getCausedByException() should not be null",
            e.getCausedByException());

         assertEquals("EJBException.getCausedByException() should be " +
            "a RuntimeException",
            RuntimeException.class,
            e.getCausedByException().getClass());

      }
      catch (Exception e)
      {
         fail("Expected EJBException but got " + e);
      }
      finally
      {
         if (exceptionTester != null)
         {
            exceptionTester.remove();
         }
      }
   }

   public void testSecurityException_local() throws Exception
   {
      ExceptionTesterLocal exceptionTester = null;
      try
      {
         InitialContext jndi = new InitialContext();
         ExceptionTesterLocalHome exTesterLocalHome =
            (ExceptionTesterLocalHome) jndi.lookup("exception/SecuredExceptionTesterLocal");
         exceptionTester = exTesterLocalHome.create();

         exceptionTester.securityExceptionNoTx();

         fail("Expected AccessLocalException to be thrown");

      }
      catch (AccessLocalException e)
      {
         // good this was expected
         assertNotNull("AccessLocalException.getCausedByException() should not be null",
            e.getCausedByException());

         Exception ex = e.getCausedByException();
         boolean isSecurityException = ex instanceof SecurityException
            || ex instanceof GeneralSecurityException;
         assertTrue("AccessLocalException.getCausedByException() should be " +
            "a security exception", isSecurityException);
      }
      catch (Exception e)
      {
         fail("Expected EJBException but got " + e);
      }
      finally
      {
         if (exceptionTester != null)
         {
            exceptionTester.remove();
         }
      }
   }

   public void testSecurityExceptionByAppNoTx_local() throws Exception
   {
      ExceptionTesterLocal exceptionTester = null;
      try
      {
         InitialContext jndi = new InitialContext();
         ExceptionTesterLocalHome exTesterLocalHome =
            (ExceptionTesterLocalHome) jndi.lookup("exception/ExceptionTesterLocal");
         exceptionTester = exTesterLocalHome.create();

         exceptionTester.securityExceptionByAppNoTx();

         fail("Expected InvalidKeyException to be thrown");

      }
      catch (InvalidKeyException e)
      {
         // good this was expected
      }
      catch (Exception e)
      {
         fail("Expected InvalidKeyException but got " + e);
      }
      finally
      {
         if (exceptionTester != null)
         {
            exceptionTester.remove();
         }
      }
   }
}
