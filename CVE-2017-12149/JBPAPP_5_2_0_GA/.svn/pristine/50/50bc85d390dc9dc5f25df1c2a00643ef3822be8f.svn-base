/**
 * 
 */
package org.jboss.test.cluster.testutil;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.jboss.logging.Logger;
import org.jboss.test.AbstractTestSetup;
import org.jboss.test.JBossClusteredTestCase;
import org.jboss.test.JBossTestCase;
import org.jboss.test.JBossTestClusteredSetup;
import org.jboss.test.JBossTestServices;

import java.util.List;
import java.util.ListIterator;
import java.util.StringTokenizer;

/**
 *  JBossClusteredTestCase extension that accepts a list of 
 *  {@link TestSetupDelegate} that can be processed as part of setup/tearDown of 
 *  the suite.
 *
 * @author Brian Stansberry
 * 
 * @version $Revision$
 */
public class DelegatingClusteredTestCase extends JBossClusteredTestCase
{
   /**
    * @param name
    */
   public DelegatingClusteredTestCase(String name)
   {
      super(name);
   }

   public static Test getDeploySetup(Test test, String jarNames)
       throws Exception
   {
       return getDeploySetup(test, jarNames, null);
   }

   public static Test getDeploySetup(Class clazz, String jarNames)
       throws Exception
   {
       return getDeploySetup(clazz, jarNames, null);
   }

   public static Test getDeploySetup(Test test, String jarNames, List<TestSetupDelegate> delegates)
       throws Exception
   {
       return new DelegatingTestSetup(test, jarNames, delegates);
   }

   public static Test getDeploySetup(Class<?> clazz, String jarNames, List<TestSetupDelegate> delegates)
       throws Exception
   {
       TestSuite suite = new TestSuite();
       suite.addTest(new TestSuite(clazz));
       return getDeploySetup(suite, jarNames, delegates);
   }
   
   public static class DelegatingTestSetup extends JBossTestClusteredSetup
   {
      private static final Logger log = Logger.getLogger(DelegatingTestSetup.class);
      private final List<TestSetupDelegate> setupDelegates;
      private String jarNames = null;
      
      /**
       * @param test
       * @throws Exception
       */
      public DelegatingTestSetup(Test test, String jarNames, List<TestSetupDelegate> delegates) throws Exception
      {
         // Don't pass the jarNames through to the superclass -- we handle them ourselves
         super(test, null);
         this.jarNames = jarNames;
         this.setupDelegates = delegates;
      }
      
      @Override
      protected void setUp() throws Exception
      {     
         super.setUp();        
         
         JBossTestServices services = this.delegate;
         if (setupDelegates != null)
         {
            for (TestSetupDelegate setupDelegate : setupDelegates)
            {
               setupDelegate.setTestServices(services);
               setupDelegate.setUp();
            }
         }
         
         deployJars();
      }

      @Override
      protected void tearDown() throws Exception
      {
         try
         {
            undeployJars();
         }
         finally
         {
            try
            {
               if (setupDelegates != null)
               {
                  for (ListIterator<TestSetupDelegate> it = setupDelegates.listIterator(); it.hasPrevious(); )
                  {
                     TestSetupDelegate setupDelegate = it.previous();
                     try
                     {
                        setupDelegate.tearDown();
                     }
                     catch (Exception e)
                     {
                       log.error("Caught exception tearing down " + setupDelegate, e);
                     }
                  }
               }
            }
            finally
            {
            
               try
               {
                  super.tearDown();
               }
               finally
               {
                  AbstractTestSetup.delegate = null;
               }
            }
         }
      }
      
      private void deployJars() throws Exception
      {      
         JBossTestCase.deploymentException = null;
         try
         {
            // deploy the comma seperated list of jars
            StringTokenizer st = new StringTokenizer(jarNames, ", ");
            while (st.hasMoreTokens())
            {
               String jarName = st.nextToken();
               this.redeploy(jarName);
               this.getLog().debug("deployed package: " + jarName);
            }
         }
         catch (Exception ex)
         {
            // Throw this in testServerFound() instead.
            JBossTestCase.deploymentException = ex;
         }
             
         // wait a couple seconds to let the cluster stabilize
         synchronized (this)
         {
            wait(2000);
         }
      }
      
      private void undeployJars() throws Exception
      {
         // deploy the comma seperated list of jars
         StringTokenizer st = new StringTokenizer(jarNames, ", ");
         String[] depoyments = new String[st.countTokens()];
         for (int i = depoyments.length - 1; i >= 0; i--)
            depoyments[i] = st.nextToken();
         Exception failure = null;
         for (int i = 0; i < depoyments.length; i++)
         {
            String jarName = depoyments[i];
            this.getLog().debug("Attempt undeploy of " + jarName);
            try
            {
               this.undeploy(jarName);
               this.getLog().debug("undeployed package: " + jarName);
            }
            catch (Exception e)
            {
               log.error("Failure undeploying " + jarName, e);
               if (failure == null)
               {
                  failure = e;
               }
            }
         }   
         
         if (failure != null)
         {
            throw failure;
         }
      }

   }

}
