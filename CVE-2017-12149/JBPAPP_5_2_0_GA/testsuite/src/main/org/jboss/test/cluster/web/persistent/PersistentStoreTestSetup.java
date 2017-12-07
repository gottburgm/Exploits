/**
 * 
 */
package org.jboss.test.cluster.web.persistent;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.test.cluster.testutil.DBSetup;
import org.jboss.test.cluster.testutil.TestSetupDelegate;
import org.jboss.web.tomcat.service.session.persistent.PersistentStore;

/**
 * Extends {@link DBSetup} by using a {@link PersistentStoreTableSetupDelegate} to
 * set up the storage table for use by the {@link PersistentStore}.
 *
 * @author Brian Stansberry
 * 
 * @version $Revision: $
 */
public class PersistentStoreTestSetup extends DBSetup
{
   private final List<TestSetupDelegate> delegates;

   /**
    * @param test
    * @param jarNames
    * @throws Exception
    */
   public PersistentStoreTestSetup(Test test, String jarNames, List<TestSetupDelegate> delegates) throws Exception
   {
      super(test, jarNames);
      this.delegates = delegates;
   }

   public static Test getDeploySetup(final Test test, final String jarNames, final List<TestSetupDelegate> delegates)
      throws Exception
   {
      return new PersistentStoreTestSetup(test, jarNames, delegates);
   }

   public static Test getDeploySetup(final Class<?> clazz, final String jarNames, final List<TestSetupDelegate> delegates)
      throws Exception
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new TestSuite(clazz));
      return getDeploySetup(suite, jarNames, delegates);
   }

   protected void setUp() throws Exception
   {
         if (delegates != null)
         {
            for (TestSetupDelegate delegate : delegates)
            {
               delegate.setUp();
            }
         }
         
         super.setUp();
   }

   protected void tearDown() throws Exception
   {
      try
      {
         super.tearDown();
      }
      finally
      {
         if (delegates != null)
         {
            for (TestSetupDelegate delegate : delegates)
            {
               delegate.tearDown();
            }
         }
      }
      
   }
   

}
