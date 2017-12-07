/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.util.ejb;

import java.lang.reflect.Constructor;
import java.util.Properties;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.Binding;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.NamingEnumeration;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.jboss.logging.Logger;
import org.jboss.tm.TransactionManagerLocator;

/**
 * Implementation of the ejb test runner.
 *
 * @see EJBTestRunner
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @author Scott.Stark@jboss.org
 * @version $Revision: 65582 $
 */
public class EJBTestRunnerBean implements SessionBean
{
   private static final Logger log = Logger.getLogger(EJBTestRunnerBean.class);
   transient private SessionContext ctx;
   private String runnerJndiName;

   /** Run the specified test method on the given class name using a Properties
    * map built from all java:comp/env entries.
    * 
    * @param className the name of the test class
    * @param methodName the name of the test method
    * @throws RemoteTestException If any throwable is thrown during 
    * execution of the method, it is wrapped with a RemoteTestException and 
    * rethrown.
    */
   public void run(String className, String methodName)
      throws RemoteTestException
   {
      Properties props = new Properties();
      try
      {
         InitialContext ctx = new InitialContext();
         NamingEnumeration bindings = ctx.listBindings("java:comp/env");
         while( bindings.hasMore() )
         {
            Binding binding = (Binding) bindings.next();
            String name = binding.getName();
            String value = binding.getObject().toString();
            props.setProperty(name, value);
         }
      }
      catch(NamingException e)
      {
         throw new RemoteTestException(e);
      }
      run(className, methodName, props);
   }

   /** Run the specified test method on the given class name
    *  
    * @param className the name of the test class
    * @param methodName the name of the test method
    * @param props
    * @throws RemoteTestException If any throwable is thrown during 
    * execution of the method, it is wrapped with a RemoteTestException and 
    * rethrown.
    */ 
   public void run(String className, String methodName, Properties props)
      throws RemoteTestException
   {
      EJBTestCase testCase = getTestInstance(className, methodName);

      setUpEJB(testCase, props);

      RemoteTestException exception = null;
      try
      {
         runTestCase(testCase);
      }
      catch (RemoteTestException e)
      {
         exception = e;
      }
      finally
      {
         try
         {
            tearDownEJB(testCase, props);
         }
         catch (RemoteTestException e)
         {
            // favor the run exception if one was thrown
            if (exception != null)
            {
               exception = e;
            }
         }
         if (exception != null)
         {
            throw exception;
         }
      }
   }

   private static boolean wantUserTransaction(Properties props)
   {
      return props == null || props.get("NO_USER_TRANSACTION") == null;
   }
   
   /**
    * Runs the setUpEJB method on the specified test case
    * @param testCase the actual test case that will be run
    * @throws RemoteTestException If any throwable is thrown during execution 
    * of the method, it is wrapped with a RemoteTestException and rethrown.
    */
   private void setUpEJB(EJBTestCase testCase, Properties props)
      throws RemoteTestException
   {
      boolean wantUserTransaction = wantUserTransaction(props);
      try
      {
         if (wantUserTransaction)
            ctx.getUserTransaction().begin();
         try
         {
            testCase.setUpEJB(props);
         }
         catch (Throwable e)
         {
            throw new RemoteTestException(e);
         }
         if (wantUserTransaction && ctx.getUserTransaction().getStatus() == Status.STATUS_ACTIVE)
         {
            ctx.getUserTransaction().commit();
         }
      }
      catch (Throwable e)
      {
         try
         {
            if (wantUserTransaction)
               ctx.getUserTransaction().rollback();
         }
         catch (SystemException unused)
         {
            // eat the exception we are exceptioning out anyway
         }
         if (e instanceof RemoteTestException)
         {
            throw (RemoteTestException) e;
         }
         throw new RemoteTestException(e);
      }
   }

   /**
    * Runs the test method on the specified test case
    * @param testCase the actual test case that will be run
    * @throws RemoteTestException If any throwable is thrown during execution 
    * of the method, it is wrapped with a RemoteTestException and rethrown.
    */
   private void runTestCase(EJBTestCase testCase) throws RemoteTestException
   {
      try
      {
         boolean wantUserTransaction = wantUserTransaction(testCase.getProps());
         try
         {
            if (wantUserTransaction)
               ctx.getUserTransaction().begin();
            try
            {
               testCase.runBare();
            }
            catch (Throwable e)
            {
               throw new RemoteTestException(e);
            }
            if (wantUserTransaction && ctx.getUserTransaction().getStatus() == Status.STATUS_ACTIVE)
            {
               ctx.getUserTransaction().commit();
            }
         }
         catch (Throwable e)
         {
            try
            {
               if (wantUserTransaction)
                  ctx.getUserTransaction().rollback();
            }
            catch (SystemException unused)
            {
               // eat the exception we are exceptioning out anyway
            }
            if (e instanceof RemoteTestException)
            {
               throw (RemoteTestException) e;
            }
            throw new RemoteTestException(e);
         }
      }
      finally
      {
         Transaction tx = null;
         TransactionManager tm = TransactionManagerLocator.getInstance().locate();
         try
         {
            tx = tm.getTransaction();
            if (tx != null)
            {
               try
               {
                  tx.rollback();
               }
               finally
               {
                  tm.suspend();
               }
            }
         }
         catch (Exception e)
         {
            log.error("Error rolling back incomplete transaction: " + tx, e);
         }
      }
   }

   /**
    * Runs the tearDownEJB method on the specified test case
    * @param testCase the actual test case that will be run
    * @throws RemoteTestException If any throwable is thrown during execution 
    * of the method, it is wrapped with a RemoteTestException and rethrown.
    */
   private void tearDownEJB(EJBTestCase testCase, Properties props)
      throws RemoteTestException
   {
      boolean wantUserTransaction = wantUserTransaction(props);
      try
      {
         if (wantUserTransaction)
            ctx.getUserTransaction().begin();
         try
         {
            testCase.tearDownEJB(props);
         }
         catch (Throwable e)
         {
            throw new RemoteTestException(e);
         }
         if (wantUserTransaction && ctx.getUserTransaction().getStatus() == Status.STATUS_ACTIVE)
         {
            ctx.getUserTransaction().commit();
         }
      }
      catch (Throwable e)
      {
         try
         {
            if (wantUserTransaction)
               ctx.getUserTransaction().rollback();
         }
         catch (SystemException unused)
         {
            // eat the exception we are exceptioning out anyway
         }
         if (e instanceof RemoteTestException)
         {
            throw (RemoteTestException) e;
         }
         throw new RemoteTestException(e);
      }
   }

   /**
    * Gets a instance of the test class with the specified class name and
    * initialized to execute the specified method.
    *
    * @param className the name of the test class
    * @param methodName the name of the test method
    * @return a new instance of the test class with the specified class name and
    *    initialized to execute the specified method.
    */
   private EJBTestCase getTestInstance(String className, String methodName)
   {
      Class testClass = null;
      try
      {
         ClassLoader loader = Thread.currentThread().getContextClassLoader();
         testClass = loader.loadClass(className);
      }
      catch (ClassNotFoundException e)
      {
         throw new EJBException("Test class not found : " + className);
      }

      Constructor constructor = null;
      try
      {
         constructor = testClass.getConstructor(new Class[]{String.class});
      }
      catch (Exception e)
      {
         throw new EJBException("Test class does not have a constructor " +
            "which has a single String argument.", e);
      }

      try
      {
         EJBTestCase testCase =
            (EJBTestCase) constructor.newInstance(new Object[]{methodName});
         testCase.setServerSide(true);
         return testCase;
      }
      catch (Exception e)
      {
         throw new EJBException("Cannot instantiate test class: " +
            testClass.getName(), e);
      }
   }

   public void ejbCreate()
   {
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

   public void setSessionContext(SessionContext ctx)
   {
      this.ctx = ctx;
   }
}
