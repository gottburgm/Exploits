/**
 * 
 */
package org.jboss.test.cluster.web.persistent;

import org.jboss.test.JBossTestServices;
import org.jboss.test.cluster.testutil.DBSetupDelegate;
import org.jboss.test.cluster.testutil.TestSetupDelegate;
import org.jboss.web.tomcat.service.session.persistent.PersistentStore;

/**
 * Extends {@link DBSetupDelegate} by using a {@link PersistentStoreTableSetup} to
 * set up the storage table for use by the {@link PersistentStore}.
 *
 * @author Brian Stansberry
 * 
 * @version $Revision: $
 */
public class PersistentStoreSetupDelegate implements TestSetupDelegate
{
   
   private final String address;
   private final int port;
   
   public PersistentStoreSetupDelegate()
   {
      this(DBSetupDelegate.DEFAULT_ADDRESS, DBSetupDelegate.DEFAULT_PORT);
   }
   
   public PersistentStoreSetupDelegate(String address, int port)
   {
      if (address == null)
      {
         throw new IllegalArgumentException("Null address");
      }
      this.address = address;
      this.port = port;
   }

   public void setTestServices(JBossTestServices services)
   {
      // no-op
   }

   public void setUp() throws Exception
   {
      
      PersistentStoreTableSetup tableSetup = new PersistentStoreTableSetup();
      tableSetup.setJdbcURL("jdbc:hsqldb:hsql://" + address + ":" + port);
      tableSetup.start();
   }

   public void tearDown() throws Exception
   {
      // no-op
   } 
   

}
