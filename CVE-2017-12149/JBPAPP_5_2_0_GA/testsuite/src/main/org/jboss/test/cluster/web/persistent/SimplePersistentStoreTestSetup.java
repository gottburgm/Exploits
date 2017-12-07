/**
 * 
 */
package org.jboss.test.cluster.web.persistent;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.test.cluster.testutil.DBSetup;
import org.jboss.test.cluster.testutil.DBSetupDelegate;
import org.jboss.web.tomcat.service.session.persistent.PersistentStore;

/**
 * Extends {@link DBSetup} by using a {@link PersistentStoreTableSetupDelegate} to
 * set up the storage table for use by the {@link PersistentStore}.
 *
 * @author Brian Stansberry
 * 
 * @version $Revision: $
 */
public class SimplePersistentStoreTestSetup extends TestSetup
{
   private final DBSetupDelegate dbDelegate;
   private final PersistentStoreSetupDelegate tableDelegate;

   /**
    * @param test
    * @param jarNames
    * @throws Exception
    */
   public SimplePersistentStoreTestSetup(Test test) throws Exception
   {
      super(test);
      this.dbDelegate = new DBSetupDelegate();
      this.tableDelegate = new PersistentStoreSetupDelegate();
   }

   /**
    * @param test
    * @param jarNames
    * @throws Exception
    */
   public SimplePersistentStoreTestSetup(Test test, String dbAddress, int port) throws Exception
   {
      super(test);
      this.dbDelegate = new DBSetupDelegate(dbAddress, port);
      this.tableDelegate = new PersistentStoreSetupDelegate(dbAddress, port);
   }

   public static Test getDeploySetup(final Test test)
      throws Exception
   {
      return new SimplePersistentStoreTestSetup(test);
   }

   public static Test getDeploySetup(final Class<?> clazz)
      throws Exception
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new TestSuite(clazz));
      return getDeploySetup(suite);
   }

   public static Test getDeploySetup(final Test test, String dbAddress, int port)
      throws Exception
   {
      return new SimplePersistentStoreTestSetup(test, dbAddress, port);
   }

   public static Test getDeploySetup(final Class<?> clazz, String dbAddress, int port)
      throws Exception
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new TestSuite(clazz));
      return getDeploySetup(suite, dbAddress, port);
   }

   protected void setUp() throws Exception
   {
      dbDelegate.setUp();
      tableDelegate.setUp();
         
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
         try
         {
            tableDelegate.tearDown();
         }
         finally
         {
            dbDelegate.tearDown();
         }
      }
      
   }
   

}
