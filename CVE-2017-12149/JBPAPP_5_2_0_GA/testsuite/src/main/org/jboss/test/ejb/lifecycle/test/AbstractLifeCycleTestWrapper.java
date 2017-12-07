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
package org.jboss.test.ejb.lifecycle.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;

import javax.management.ObjectInstance;
import javax.management.ObjectName;

import junit.framework.Protectable;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;

import org.jboss.ejb.MessageDrivenContainer;
import org.jboss.logging.Logger;
import org.jboss.system.ServiceControllerMBean;
import org.jboss.test.JBossTestCase;
import org.jboss.test.util.jms.JMSDestinationsUtil;

/**
 * Base TestCase for lifecycle tests. This test overrides the run method of the JUnit test
 * to execute a external test case, restart a service and run the external test case again. 
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 105321 $
 */
public abstract class AbstractLifeCycleTestWrapper extends JBossTestCase
{
   /** The service controller objectName. */
   private static final ObjectName serviceController = ServiceControllerMBean.OBJECT_NAME;
   
   /** The logger. */
   protected static final Logger log = Logger.getLogger(AbstractLifeCycleTestWrapper.class);
   
   public AbstractLifeCycleTestWrapper(String name)
   {
      super(name);
   }
   
   /**
    * Get the package which should be deployed.
    * 
    * @return the deploy package
    */
   protected abstract String getPackage();
   
   /**
    * The tests to execute.
    * 
    * @return the tests.
    */
   protected abstract Enumeration<TestCase> getTests();
 
   /**
    * Restart a service.
    * 
    * @param name the service name
    * @throws Exception
    */
   protected void restart(String name) throws Exception
   {
      restart(new String[] { name });
   }
   
   /**
    * Restart more services.
    * 
    * @param names the service names
    * @throws Exception
    */
   protected void restart(String... names) throws Exception
   {
      for(String restartName : names)
      {
         ObjectName objectName = new ObjectName(restartName);
         restart(objectName);
      }
   }
   
   /**
    * Restart the container. This calls stop, destroy, create and start
    * over the ServiceController.
    * 
    * @param name the objectName
    * @throws Exception
    */
   protected void restart(ObjectName name) throws Exception
   {
      log.debug("restarting service: " + name);
      Object[] args = { name };
      String[] sig = { ObjectName.class.getName() };
      
      invoke(serviceController, "stop", args, sig);
      invoke(serviceController, "destroy", args, sig);
      invoke(serviceController, "create", args, sig);
      invoke(serviceController, "start", args, sig);      
   }
   
   /**
    * Get the JNDI name of a MDB bean. This is needed as the jndi name
    * is basically something like "local/" + ejbName + '@' + System.identityHashCode(ejbName)
    * 
    * @param name the partial jndi name
    * @return the jndi name of the MDB bean
    * @throws Exception
    */
   protected String getMDBName(String name) throws Exception
   {
      Set<ObjectInstance> set = getServer().queryMBeans(new ObjectName("jboss.j2ee:service=EJB,*"), null);
      
      for(ObjectInstance i : set)
      {
         if(i.getClassName().equals(MessageDrivenContainer.class.getName()))
         {
            if( i.getObjectName().getKeyProperty("plugin") == null )
            {
               if(i.getObjectName().getKeyProperty("binding") == null)
               {
                  String jndi = i.getObjectName().getKeyProperty("jndiName");
                  if(jndi != null && jndi.startsWith(name))
                  {
                     return jndi;
                  }
               }
            }
         }
      }
      return null;
   }
   
   /**
    * Deploy a deployment.
    * 
    * @throws Exception
    */
   protected void deploy() throws Exception
   {
      super.setUp();
      try
      {
         JMSDestinationsUtil.setupBasicDestinations();
         redeploy(getPackage());
      }
      catch(Exception e)
      {
         undeploy(getPackage());
         throw e;
      }
   }
   
   /**
    * Undeploy a deployment.
    * 
    * @throws Exception
    */
   protected void undeploy() throws Exception
   {
      undeploy(getPackage());
      JMSDestinationsUtil.destroyDestinations();
   }

   /**
    * Override the JUnit run test to execute external tests,
    * restart the service and run the test again.
    * FIXME this is a hack.
    * 
    * @param result the JUNIT TestResult.
    */
   @Override
   public void run(TestResult result)
   {
      try
      {
         // deploy
         deploy();
         
         // get the external tests
         Enumeration<TestCase> e = getTests();
         while(e.hasMoreElements())
         {
            TestCase t = e.nextElement();
            // run the external test
            result.runProtected(this, getProctectable(t));
         }

         // run the test case as usual
         super.run(result);

         // 
         e = getTests();
         while(e.hasMoreElements())
         {
            TestCase t = e.nextElement();
            // run the external test again
            result.runProtected(this, getProctectable(t));
         }
      }
      catch(Exception e)
      {
         result.addError(this, e);
      }
      finally
      {
         try
         {
            // undeploy
            undeploy();
         } 
         catch(Exception e)
         {
            result.addError(this, e);
         }
      }
   }
   
   protected Enumeration<TestCase> getTestCases(Class<? extends TestCase> testClass)
   {
      return getTestCases(testClass, null);
   }
   
   protected Enumeration<TestCase> getTestCases(Class<? extends TestCase> testClass, Collection<String> excludes)
   {
      if(testClass == null ) return Collections.enumeration(Collections.EMPTY_SET);
      
      Enumeration<TestCase> testCases = new TestSuite(testClass).tests();
      if(excludes == null || excludes.isEmpty()) return testCases;
      
      List<TestCase> filtered = new ArrayList<TestCase>();
      while(testCases.hasMoreElements())
      {
         TestCase t = testCases.nextElement();
         if(! excludes.contains(t.getName()))
            filtered.add(t);
      }
      return Collections.enumeration(filtered);
   }
   

   /**
    * Helper to wrap a test for JUnit.
    * 
    * @param test the Test
    * @return a Protectable 
    */
   private Protectable getProctectable(final TestCase test)
   {
      return new Protectable() {
         public void protect() throws Throwable {
             test.runBare();
         }
     };
   }
   
}