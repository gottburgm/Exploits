/**
 * 
 */
package org.jboss.test.cluster.defaultcfg.simpleweb.test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Test;
import junit.framework.TestCase;

import org.jboss.test.cluster.testutil.DBSetupDelegate;
import org.jboss.test.cluster.web.persistent.MockOutgoingSessionData;
import org.jboss.test.cluster.web.persistent.SimplePersistentStoreTestSetup;
import org.jboss.web.tomcat.service.session.distributedcache.spi.DistributableSessionMetadata;
import org.jboss.web.tomcat.service.session.distributedcache.spi.IncomingDistributableSessionData;
import org.jboss.web.tomcat.service.session.distributedcache.spi.OutgoingSessionGranularitySessionData;
import org.jboss.web.tomcat.service.session.persistent.DriverManagerPersistentStore;
import org.jboss.web.tomcat.service.session.persistent.RDBMSStoreBase;

/**
 * Unit tests for {@link DriverManagerPersistentStore}, although really more of
 * a test of its abstract superclass RDBMSStoreBase where we use the 
 * DriverManager-based subclass as a simple way to get a concrete implementation.
 *
 * @author Brian Stansberry
 * 
 * @version $Revision: $
 */
public class DriverManagerPersistentStoreUnitTestCase extends TestCase
{
   private static final String DB_ADDRESS = System.getProperty(DBSetupDelegate.DBADDRESS_PROPERTY, DBSetupDelegate.DEFAULT_ADDRESS);
   
   private static final String CONNECTION_URL = "jdbc:hsqldb:hsql://" + DB_ADDRESS + ":" + DBSetupDelegate.DEFAULT_PORT;
   
   private static final String CONTEXT_PATH = "localhost/test";

   private static AtomicInteger id = new AtomicInteger();
   
   private DriverManagerPersistentStore testee;
   
   /**
    * @param name
    */
   public DriverManagerPersistentStoreUnitTestCase(String name)
   {
      super(name);
   }

   public static Test suite() throws Exception
   {
      return SimplePersistentStoreTestSetup.getDeploySetup(DriverManagerPersistentStoreUnitTestCase.class, DB_ADDRESS, DBSetupDelegate.DEFAULT_PORT);
   }
   
   private static String nextId()
   {
      return "session" + id.incrementAndGet();
   }

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      
      testee = new DriverManagerPersistentStore();
      testee.setDriverName(org.hsqldb.jdbcDriver.class.getName());
      testee.setConnectionURL(CONNECTION_URL);
      testee.setConnectionName("sa");
      testee.setName(CONTEXT_PATH);
   }
   
   @Override
   protected void tearDown() throws Exception
   {
      try
      {
         if (testee != null && testee.isStarted())
         {
            testee.clear();
            testee.stop();
         }
      }
      finally
      {
         super.tearDown();
      }
   }

   /**
    * Test method for {@link org.jboss.web.tomcat.service.session.persistent.DriverManagerPersistentStore#getInfo()}.
    */
   public void testGetInfo()
   {
      testee.start();
      assertEquals(testee.getStoreName() + "/1.0", testee.getInfo());
   }

   /**
    * Test method for {@link org.jboss.web.tomcat.service.session.persistent.DriverManagerPersistentStore#getStoreName()}.
    */
   public void testGetStoreName()
   {
      assertEquals(testee.getClass().getSimpleName(), testee.getStoreName());
   }

   /**
    * Test method for {@link org.jboss.web.tomcat.service.session.persistent.DriverManagerPersistentStore#setConnectionURL(java.lang.String)}.
    */
   public void testConnectionURL()
   {
      assertEquals(CONNECTION_URL, testee.getConnectionURL());
      testee.setConnectionURL("blah");
      assertEquals("blah", testee.getConnectionURL());
   }

   /**
    * Test method for {@link org.jboss.web.tomcat.service.session.persistent.DriverManagerPersistentStore#setDriverName(java.lang.String)}.
    */
   public void testDriverName()
   {
      assertEquals(org.hsqldb.jdbcDriver.class.getName(), testee.getDriverName());
      testee.setDriverName("blah");
      assertEquals("blah", testee.getDriverName());
   }

   /**
    * Test method for {@link org.jboss.web.tomcat.service.session.persistent.RDBMSStoreBase#getName()}.
    */
   public void testName()
   {
      assertEquals(CONTEXT_PATH, testee.getName());
      testee.setName("blah");
      assertEquals("blah", testee.getName());
   }

   /**
    * Test method for {@link org.jboss.web.tomcat.service.session.persistent.RDBMSStoreBase#setConnectionName(java.lang.String)}.
    */
   public void testConnectionName()
   {
      assertEquals("sa", testee.getConnectionName());
      testee.setConnectionName("blah");
      assertEquals("blah", testee.getConnectionName());
   }

   /**
    * Test method for {@link org.jboss.web.tomcat.service.session.persistent.RDBMSStoreBase#setConnectionPassword(java.lang.String)}.
    */
   public void testConnectionPassword()
   {
      assertNull(testee.getConnectionPassword());
      testee.setConnectionPassword("blah");
      assertEquals("blah", testee.getConnectionPassword());
   }

   /**
    * Test method for {@link org.jboss.web.tomcat.service.session.persistent.RDBMSStoreBase#setSessionTable(java.lang.String)}.
    */
   public void testSessionTable()
   {
      assertEquals(RDBMSStoreBase.DEFAULT_TABLE, testee.getSessionTable());
      testee.setSessionTable("blah");
      assertEquals("blah", testee.getSessionTable());
   }

   /**
    * Test method for {@link org.jboss.web.tomcat.service.session.persistent.RDBMSStoreBase#setSessionAppCol(java.lang.String)}.
    */
   public void testSessionAppCol()
   {
      assertEquals(RDBMSStoreBase.DEFAULT_APP_COL, testee.getSessionAppCol());
      testee.setSessionAppCol("blah");
      assertEquals("blah", testee.getSessionAppCol());
   }

   /**
    * Test method for {@link org.jboss.web.tomcat.service.session.persistent.RDBMSStoreBase#setSessionIdCol(java.lang.String)}.
    */
   public void testSessionIdCol()
   {
      assertEquals(RDBMSStoreBase.DEFAULT_ID_COL, testee.getSessionIdCol());
      testee.setSessionIdCol("blah");
      assertEquals("blah", testee.getSessionIdCol());
   }

   /**
    * Test method for {@link org.jboss.web.tomcat.service.session.persistent.RDBMSStoreBase#setSessionFullIdCol(java.lang.String)}.
    */
   public void testSessionFullIdCol()
   {
      assertEquals(RDBMSStoreBase.DEFAULT_FULLID_COL, testee.getSessionFullIdCol());
      testee.setSessionFullIdCol("blah");
      assertEquals("blah", testee.getSessionFullIdCol());
   }

   /**
    * Test method for {@link org.jboss.web.tomcat.service.session.persistent.RDBMSStoreBase#setSessionCreationTimeCol(java.lang.String)}.
    */
   public void testSessionCreationTimeCol()
   {
      assertEquals(RDBMSStoreBase.DEFAULT_CREATION_TIME_COL, testee.getSessionCreationTimeCol());
      testee.setSessionCreationTimeCol("blah");
      assertEquals("blah", testee.getSessionCreationTimeCol());
   }

   /**
    * Test method for {@link org.jboss.web.tomcat.service.session.persistent.RDBMSStoreBase#setSessionMaxInactiveCol(java.lang.String)}.
    */
   public void testSessionMaxInactiveCol()
   {
      assertEquals(RDBMSStoreBase.DEFAULT_MAX_INACTIVE_COL, testee.getSessionMaxInactiveCol());
      testee.setSessionMaxInactiveCol("blah");
      assertEquals("blah", testee.getSessionMaxInactiveCol());
   }

   /**
    * Test method for {@link org.jboss.web.tomcat.service.session.persistent.RDBMSStoreBase#setSessionNewCol(java.lang.String)}.
    */
   public void testSessionNewCol()
   {
      assertEquals(RDBMSStoreBase.DEFAULT_ISNEW_COL, testee.getSessionNewCol());
      testee.setSessionNewCol("blah");
      assertEquals("blah", testee.getSessionNewCol());
   }

   /**
    * Test method for {@link org.jboss.web.tomcat.service.session.persistent.RDBMSStoreBase#setSessionVersionCol(java.lang.String)}.
    */
   public void testSessionVersionCol()
   {
      assertEquals(RDBMSStoreBase.DEFAULT_VERSION_COL, testee.getSessionVersionCol());
      testee.setSessionVersionCol("blah");
      assertEquals("blah", testee.getSessionVersionCol());
   }

   /**
    * Test method for {@link org.jboss.web.tomcat.service.session.persistent.RDBMSStoreBase#setSessionLastAccessedCol(java.lang.String)}.
    */
   public void testSessionLastAccessedCol()
   {
      assertEquals(RDBMSStoreBase.DEFAULT_LAST_ACCESSED_COL, testee.getSessionLastAccessedCol());
      testee.setSessionLastAccessedCol("blah");
      assertEquals("blah", testee.getSessionLastAccessedCol());
   }

   /**
    * Test method for {@link org.jboss.web.tomcat.service.session.persistent.RDBMSStoreBase#setSessionValidCol(java.lang.String)}.
    */
   public void testSessionValidCol()
   {
      assertEquals(RDBMSStoreBase.DEFAULT_ISVALID_COL, testee.getSessionValidCol());
      testee.setSessionValidCol("blah");
      assertEquals("blah", testee.getSessionValidCol());
   }

   /**
    * Test method for {@link org.jboss.web.tomcat.service.session.persistent.RDBMSStoreBase#setSessionMetadataCol(java.lang.String)}.
    */
   public void testSessionMetadataCol()
   {
      assertEquals(RDBMSStoreBase.DEFAULT_METADATA_COL, testee.getSessionMetadataCol());
      testee.setSessionMetadataCol("blah");
      assertEquals("blah", testee.getSessionMetadataCol());
   }

   /**
    * Test method for {@link org.jboss.web.tomcat.service.session.persistent.RDBMSStoreBase#setCleanupInterval(int)}.
    */
   public void testSetCleanupInterval()
   {
      assertEquals(RDBMSStoreBase.DEFAULT_CLEANUP_INTERVAL, testee.getCleanupInterval());
      testee.setCleanupInterval(22);
      assertEquals(22, testee.getCleanupInterval());
   }

   /**
    * Test method for {@link org.jboss.web.tomcat.service.session.persistent.RDBMSStoreBase#clear()}.
    */
   public void testClear()
   {
      testee.start();
      
      int existing = testee.getSize();
      
      DistributableSessionMetadata md = new DistributableSessionMetadata();
      String id = nextId();
      md.setId(id + ".full");
      md.setCreationTime(System.currentTimeMillis());
      md.setNew(true);
      md.setValid(true);
      md.setMaxInactiveInterval(30000);
      Long ts = Long.valueOf(md.getCreationTime() + 1); 
      Map<String, Object> attrs = new HashMap<String, Object>();
      attrs.put("key", "value");
      OutgoingSessionGranularitySessionData sessionData = new MockOutgoingSessionData(id, 0, ts, md, attrs);
      testee.storeSessionData(sessionData);
      
      assertEquals(Integer.valueOf(0), testee.getSessionVersion(id));
      
      md = new DistributableSessionMetadata();
      String id2 = nextId();
      md.setId(id2 + ".full");
      md.setCreationTime(System.currentTimeMillis());
      md.setNew(true);
      md.setValid(true);
      md.setMaxInactiveInterval(30000);
      ts = Long.valueOf(md.getCreationTime() + 1); 
      attrs = new HashMap<String, Object>();
      attrs.put("key", "value");
      sessionData = new MockOutgoingSessionData(id2, 0, ts, md, attrs);
      testee.storeSessionData(sessionData);
      
      assertEquals(Integer.valueOf(0), testee.getSessionVersion(id2));
      
      assertEquals(2 + existing, testee.getSize());
      
      testee.clear();
      
      assertNull(testee.getSessionVersion(id));      
      assertNull(testee.getSessionVersion(id2));
      
      assertEquals(0, testee.getSize());
   }

   /**
    * Test method for {@link org.jboss.web.tomcat.service.session.persistent.RDBMSStoreBase#getSessionIds()}.
    */
   public void testGetSessionIds()
   {
      testee.start();
      DistributableSessionMetadata md = new DistributableSessionMetadata();
      String id = nextId();
      md.setId(id + ".full");
      md.setCreationTime(System.currentTimeMillis());
      md.setNew(true);
      md.setValid(true);
      md.setMaxInactiveInterval(30000);
      Long ts = Long.valueOf(md.getCreationTime() + 1); 
      Map<String, Object> attrs = new HashMap<String, Object>();
      attrs.put("key", "value");
      OutgoingSessionGranularitySessionData sessionData = new MockOutgoingSessionData(id, 0, ts, md, attrs);
      testee.storeSessionData(sessionData);
      
      md = new DistributableSessionMetadata();
      String id2 = nextId();
      md.setId(id2 + ".full");
      md.setCreationTime(System.currentTimeMillis());
      md.setNew(true);
      md.setValid(true);
      md.setMaxInactiveInterval(30000);
      ts = Long.valueOf(md.getCreationTime() + 1); 
      attrs = new HashMap<String, Object>();
      attrs.put("key", "value");
      sessionData = new MockOutgoingSessionData(id2, 0, ts, md, attrs);
      testee.storeSessionData(sessionData);
      
      Set<String> ids = testee.getSessionIds();
      assertNotNull(ids);
      assertTrue(ids.contains(id));
      assertTrue(ids.contains(id2));
      assertEquals(2, ids.size());
      
      testee.remove(id);
      
      ids = testee.getSessionIds();
      assertNotNull(ids);
      assertFalse(ids.contains(id));
      assertTrue(ids.contains(id2));
      assertEquals(1, ids.size());
   }

   /**
    * Test method for {@link org.jboss.web.tomcat.service.session.persistent.RDBMSStoreBase#getSessionData(java.lang.String, boolean)}.
    */
   public void testGetSessionData()
   {
      testee.start();
      DistributableSessionMetadata md = new DistributableSessionMetadata();
      String id = nextId();
      md.setId(id + ".full");
      md.setCreationTime(System.currentTimeMillis());
      md.setNew(true);
      md.setValid(true);
      md.setMaxInactiveInterval(30000);
      Long ts = Long.valueOf(md.getCreationTime() + 1); 
      Map<String, Object> attrs = new HashMap<String, Object>();
      attrs.put("key", "value");
      OutgoingSessionGranularitySessionData sessionData = new MockOutgoingSessionData(id, 0, ts, md, attrs);
      testee.storeSessionData(sessionData);
      
      IncomingDistributableSessionData incoming = testee.getSessionData(id, true);
      assertEquals(0, incoming.getVersion());
      assertEquals(md.getCreationTime() + 1, incoming.getTimestamp());
      assertEquals(md.getId(), incoming.getMetadata().getId());
      assertEquals(md.getCreationTime(), incoming.getMetadata().getCreationTime());
      assertEquals(md.isNew(), incoming.getMetadata().isNew());
      assertEquals(md.isValid(), incoming.getMetadata().isValid());
      assertEquals(md.getMaxInactiveInterval(), incoming.getMetadata().getMaxInactiveInterval());
      assertTrue(incoming.providesSessionAttributes());
      assertEquals(attrs, incoming.getSessionAttributes());
      
      incoming = testee.getSessionData(id, false);
      assertEquals(0, incoming.getVersion());
      assertEquals(md.getCreationTime() + 1, incoming.getTimestamp());
      assertEquals(md.getId(), incoming.getMetadata().getId());
      assertEquals(md.getCreationTime(), incoming.getMetadata().getCreationTime());
      assertEquals(md.isNew(), incoming.getMetadata().isNew());
      assertEquals(md.isValid(), incoming.getMetadata().isValid());
      assertEquals(md.getMaxInactiveInterval(), incoming.getMetadata().getMaxInactiveInterval());
      assertFalse(incoming.providesSessionAttributes());
   }

   /**
    * Test method for {@link org.jboss.web.tomcat.service.session.persistent.RDBMSStoreBase#remove(java.lang.String)}.
    */
   public void testRemove()
   {
      testee.start();
      
      testee.clear();
      
      int existing = testee.getSize();
      
      DistributableSessionMetadata md = new DistributableSessionMetadata();
      String id = nextId();
      md.setId(id + ".full");
      md.setCreationTime(System.currentTimeMillis());
      md.setNew(true);
      md.setValid(true);
      md.setMaxInactiveInterval(30000);
      Long ts = Long.valueOf(md.getCreationTime() + 1); 
      Map<String, Object> attrs = new HashMap<String, Object>();
      attrs.put("key", "value");
      OutgoingSessionGranularitySessionData sessionData = new MockOutgoingSessionData(id, 0, ts, md, attrs);
      testee.storeSessionData(sessionData);
      
      testee.remove(id);
      
      assertNull(testee.getSessionData(id, false));
      assertNull(testee.getSessionData(id, true));
      assertNull(testee.getSessionVersion(id));
      assertNull(testee.getSessionTimestamp(id));
      
      Set<String> ids = testee.getSessionIds();
      assertNotNull(ids);
      assertFalse(ids.contains(id));
      assertEquals(existing, ids.size());
      
      assertEquals(existing, testee.getSize());
   }

   /**
    * Test method for {@link org.jboss.web.tomcat.service.session.persistent.RDBMSStoreBase#storeSessionData(org.jboss.web.tomcat.service.session.distributedcache.spi.OutgoingSessionGranularitySessionData)}.
    */
   public void testInsertSessionData()
   {
      testee.start();
      DistributableSessionMetadata md = new DistributableSessionMetadata();
      String id = nextId();
      md.setId(id + ".full");
      md.setCreationTime(System.currentTimeMillis());
      md.setNew(true);
      md.setValid(true);
      md.setMaxInactiveInterval(30000);
      Long ts = Long.valueOf(md.getCreationTime() + 1); 
      Map<String, Object> attrs = new HashMap<String, Object>();
      attrs.put("key", "value");
      OutgoingSessionGranularitySessionData sessionData = new MockOutgoingSessionData(id, 0, ts, md, attrs);
      testee.storeSessionData(sessionData);
      
      IncomingDistributableSessionData incoming = testee.getSessionData(id, true);
      assertEquals(0, incoming.getVersion());
      assertEquals(md.getCreationTime() + 1, incoming.getTimestamp());
      assertEquals(md.getId(), incoming.getMetadata().getId());
      assertEquals(md.getCreationTime(), incoming.getMetadata().getCreationTime());
      assertEquals(md.isNew(), incoming.getMetadata().isNew());
      assertEquals(md.isValid(), incoming.getMetadata().isValid());
      assertEquals(md.getMaxInactiveInterval(), incoming.getMetadata().getMaxInactiveInterval());
      assertTrue(incoming.providesSessionAttributes());
      assertEquals(attrs, incoming.getSessionAttributes());
   }

   /**
    * Test method for {@link org.jboss.web.tomcat.service.session.persistent.RDBMSStoreBase#storeSessionData(org.jboss.web.tomcat.service.session.distributedcache.spi.OutgoingSessionGranularitySessionData)}.
    */
   public void testInsertSessionDataNullAttributes()
   {
      testee.start();
      DistributableSessionMetadata md = new DistributableSessionMetadata();
      String id = nextId();
      md.setId(id + ".full");
      md.setCreationTime(System.currentTimeMillis());
      md.setNew(true);
      md.setValid(true);
      md.setMaxInactiveInterval(30000);
      Long ts = Long.valueOf(md.getCreationTime() + 1); 
      Map<String, Object> attrs = null;
      OutgoingSessionGranularitySessionData sessionData = new MockOutgoingSessionData(id, 0, ts, md, attrs);
      testee.storeSessionData(sessionData);
      
      IncomingDistributableSessionData incoming = testee.getSessionData(id, true);
      assertEquals(0, incoming.getVersion());
      assertEquals(md.getCreationTime() + 1, incoming.getTimestamp());
      assertEquals(md.getId(), incoming.getMetadata().getId());
      assertEquals(md.getCreationTime(), incoming.getMetadata().getCreationTime());
      assertEquals(md.isNew(), incoming.getMetadata().isNew());
      assertEquals(md.isValid(), incoming.getMetadata().isValid());
      assertEquals(md.getMaxInactiveInterval(), incoming.getMetadata().getMaxInactiveInterval());
      Map<String, Object> map = incoming.getSessionAttributes();
      assertNotNull(map);
      assertEquals(0, map.size());
   }

   /**
    * Test method for {@link org.jboss.web.tomcat.service.session.persistent.RDBMSStoreBase#storeSessionData(org.jboss.web.tomcat.service.session.distributedcache.spi.OutgoingSessionGranularitySessionData)}.
    */
   public void testInsertSessionDataExistingRecord()
   {
      testee.start();
      DistributableSessionMetadata md = new DistributableSessionMetadata();
      String id = nextId();
      md.setId(id + ".full");
      md.setCreationTime(1);
      md.setNew(true);
      md.setValid(true);
      md.setMaxInactiveInterval(30000);
      Long ts = Long.valueOf(2); 
      Map<String, Object> attrs = new HashMap<String, Object>();
      attrs.put("key", "value");
      OutgoingSessionGranularitySessionData sessionData = new MockOutgoingSessionData(id, 0, ts, md, attrs);
      testee.storeSessionData(sessionData);
      
      // Don't call md.setNew(false); !! Treat it as a new session
      DistributableSessionMetadata newmd = new DistributableSessionMetadata();
      newmd.setId(id + ".full");
      newmd.setCreationTime(3);
      newmd.setNew(true);
      newmd.setValid(true);
      newmd.setMaxInactiveInterval(30000);
      newmd.setMaxInactiveInterval(20000);
      attrs.clear();
      attrs.put("key", "newvalue");
      Long newts = Long.valueOf(4); 
      sessionData = new MockOutgoingSessionData(id, 0, newts, newmd, attrs);
      testee.storeSessionData(sessionData);
      
      IncomingDistributableSessionData incoming = testee.getSessionData(id, true);
      assertEquals(0, incoming.getVersion());
      assertEquals(newts.longValue(), incoming.getTimestamp());
      assertEquals(newmd.getId(), incoming.getMetadata().getId());
      assertEquals(newmd.getCreationTime(), incoming.getMetadata().getCreationTime());
      assertEquals(newmd.isNew(), incoming.getMetadata().isNew());
      assertEquals(newmd.isValid(), incoming.getMetadata().isValid());
      assertEquals(newmd.getMaxInactiveInterval(), incoming.getMetadata().getMaxInactiveInterval());
      assertTrue(incoming.providesSessionAttributes());
      assertEquals(attrs, incoming.getSessionAttributes());
   }

   /**
    * Test method for {@link org.jboss.web.tomcat.service.session.persistent.RDBMSStoreBase#storeSessionData(org.jboss.web.tomcat.service.session.distributedcache.spi.OutgoingSessionGranularitySessionData)}.
    */
   public void testFullUpdateSessionData()
   {
      testee.start();
      DistributableSessionMetadata md = new DistributableSessionMetadata();
      String id = nextId();
      md.setId(id + ".full");
      md.setCreationTime(System.currentTimeMillis());
      md.setNew(true);
      md.setValid(true);
      md.setMaxInactiveInterval(30000);
      Long ts = Long.valueOf(md.getCreationTime() + 1); 
      Map<String, Object> attrs = new HashMap<String, Object>();
      attrs.put("key", "value");
      OutgoingSessionGranularitySessionData sessionData = new MockOutgoingSessionData(id, 1, ts, md, attrs);
      testee.storeSessionData(sessionData);
      
      md.setNew(false);
      md.setMaxInactiveInterval(20000);
      attrs.put("key", "newvalue");
      ts = Long.valueOf(System.currentTimeMillis());
      sessionData = new MockOutgoingSessionData(id, 1, ts, md, attrs);
      testee.storeSessionData(sessionData);
      
      IncomingDistributableSessionData incoming = testee.getSessionData(id, true);
      assertEquals(1, incoming.getVersion());
      assertEquals(ts.longValue(), incoming.getTimestamp());
      assertEquals(md.getId(), incoming.getMetadata().getId());
      assertEquals(md.getCreationTime(), incoming.getMetadata().getCreationTime());
      assertEquals(md.isNew(), incoming.getMetadata().isNew());
      assertEquals(md.isValid(), incoming.getMetadata().isValid());
      assertEquals(md.getMaxInactiveInterval(), incoming.getMetadata().getMaxInactiveInterval());
      assertTrue(incoming.providesSessionAttributes());
      assertEquals(attrs, incoming.getSessionAttributes());
   }

   /**
    * Test method for {@link org.jboss.web.tomcat.service.session.persistent.RDBMSStoreBase#storeSessionData(org.jboss.web.tomcat.service.session.distributedcache.spi.OutgoingSessionGranularitySessionData)}.
    */
   public void testAttributeUpdateSessionData()
   {
      testee.start();
      DistributableSessionMetadata md = new DistributableSessionMetadata();
      String id = nextId();
      md.setId(id + ".full");
      md.setCreationTime(System.currentTimeMillis());
      md.setNew(true);
      md.setValid(true);
      md.setMaxInactiveInterval(30000);
      Long ts = Long.valueOf(md.getCreationTime() + 1); 
      Map<String, Object> attrs = new HashMap<String, Object>();
      attrs.put("key", "value");
      OutgoingSessionGranularitySessionData sessionData = new MockOutgoingSessionData(id, 1, ts, md, attrs);
      testee.storeSessionData(sessionData);
      
      attrs.put("key", "newvalue");
      ts = Long.valueOf(System.currentTimeMillis());
      sessionData = new MockOutgoingSessionData(id, 1, ts, null, attrs);
      testee.storeSessionData(sessionData);
      
      IncomingDistributableSessionData incoming = testee.getSessionData(id, true);
      assertEquals(1, incoming.getVersion());
      assertEquals(ts.longValue(), incoming.getTimestamp());
      assertEquals(md.getId(), incoming.getMetadata().getId());
      assertEquals(md.getCreationTime(), incoming.getMetadata().getCreationTime());
      assertEquals(md.isNew(), incoming.getMetadata().isNew());
      assertEquals(md.isValid(), incoming.getMetadata().isValid());
      assertEquals(md.getMaxInactiveInterval(), incoming.getMetadata().getMaxInactiveInterval());
      assertTrue(incoming.providesSessionAttributes());
      assertEquals(attrs, incoming.getSessionAttributes());
   }

   /**
    * Test method for {@link org.jboss.web.tomcat.service.session.persistent.RDBMSStoreBase#storeSessionData(org.jboss.web.tomcat.service.session.distributedcache.spi.OutgoingSessionGranularitySessionData)}.
    */
   public void testMetadataUpdateSessionData()
   {
      testee.start();
      DistributableSessionMetadata md = new DistributableSessionMetadata();
      String id = nextId();
      md.setId(id + ".full");
      md.setCreationTime(System.currentTimeMillis());
      md.setNew(true);
      md.setValid(true);
      md.setMaxInactiveInterval(30000);
      Long ts = Long.valueOf(md.getCreationTime() + 1); 
      Map<String, Object> attrs = new HashMap<String, Object>();
      attrs.put("key", "value");
      OutgoingSessionGranularitySessionData sessionData = new MockOutgoingSessionData(id, 1, ts, md, attrs);
      testee.storeSessionData(sessionData);
      
      md.setNew(false);
      md.setMaxInactiveInterval(20000);
      ts = Long.valueOf(System.currentTimeMillis());
      sessionData = new MockOutgoingSessionData(id, 1, ts, md, null);
      testee.storeSessionData(sessionData);
      
      IncomingDistributableSessionData incoming = testee.getSessionData(id, true);
      assertEquals(1, incoming.getVersion());
      assertEquals(ts.longValue(), incoming.getTimestamp());
      assertEquals(md.getId(), incoming.getMetadata().getId());
      assertEquals(md.getCreationTime(), incoming.getMetadata().getCreationTime());
      assertEquals(md.isNew(), incoming.getMetadata().isNew());
      assertEquals(md.isValid(), incoming.getMetadata().isValid());
      assertEquals(md.getMaxInactiveInterval(), incoming.getMetadata().getMaxInactiveInterval());
      assertTrue(incoming.providesSessionAttributes());
      assertEquals(attrs, incoming.getSessionAttributes());
   }

   /**
    * Test method for {@link org.jboss.web.tomcat.service.session.persistent.RDBMSStoreBase#storeSessionData(org.jboss.web.tomcat.service.session.distributedcache.spi.OutgoingSessionGranularitySessionData)}.
    */
   public void testSimpleUpdateSessionData()
   {
      testee.start();
      DistributableSessionMetadata md = new DistributableSessionMetadata();
      String id = nextId();
      md.setId(id + ".full");
      md.setCreationTime(System.currentTimeMillis());
      md.setNew(true);
      md.setValid(true);
      md.setMaxInactiveInterval(30000);
      Long ts = Long.valueOf(md.getCreationTime() + 1); 
      Map<String, Object> attrs = new HashMap<String, Object>();
      attrs.put("key", "value");
      OutgoingSessionGranularitySessionData sessionData = new MockOutgoingSessionData(id, 1, ts, md, attrs);
      testee.storeSessionData(sessionData);
      
      ts = Long.valueOf(System.currentTimeMillis());
      sessionData = new MockOutgoingSessionData(id, 1, ts, null, null);
      testee.storeSessionData(sessionData);
      
      IncomingDistributableSessionData incoming = testee.getSessionData(id, true);
      assertEquals(1, incoming.getVersion());
      assertEquals(ts.longValue(), incoming.getTimestamp());
      assertEquals(md.getId(), incoming.getMetadata().getId());
      assertEquals(md.getCreationTime(), incoming.getMetadata().getCreationTime());
      assertEquals(md.isNew(), incoming.getMetadata().isNew());
      assertEquals(md.isValid(), incoming.getMetadata().isValid());
      assertEquals(md.getMaxInactiveInterval(), incoming.getMetadata().getMaxInactiveInterval());
      assertTrue(incoming.providesSessionAttributes());
      assertEquals(attrs, incoming.getSessionAttributes());
   }

   /**
    * Test method for {@link org.jboss.web.tomcat.service.session.persistent.RDBMSStoreBase#getSessionTimestamp(java.lang.String)}.
    */
   public void testGetSessionTimestamp()
   {
      testee.start();
      String id = nextId();
      
      assertNull(testee.getSessionTimestamp(id));
      
      DistributableSessionMetadata md = new DistributableSessionMetadata();
      md.setId(id + ".full");
      md.setCreationTime(System.currentTimeMillis());
      md.setNew(true);
      md.setValid(true);
      md.setMaxInactiveInterval(30000);
      Long ts = Long.valueOf(md.getCreationTime() + 1); 
      Map<String, Object> attrs = new HashMap<String, Object>();
      attrs.put("key", "value");
      OutgoingSessionGranularitySessionData sessionData = new MockOutgoingSessionData(id, 1, ts, md, attrs);
      testee.storeSessionData(sessionData);
      
      IncomingDistributableSessionData incoming = testee.getSessionData(id, true);
      assertEquals(1, incoming.getVersion());
      assertEquals(ts.longValue(), incoming.getTimestamp());
   }

   /**
    * Test method for {@link org.jboss.web.tomcat.service.session.persistent.RDBMSStoreBase#getSessionVersion(java.lang.String)}.
    */
   public void testGetSessionVersion()
   {
      testee.start();
      String id = nextId();
      
      assertNull(testee.getSessionVersion(id));
      
      DistributableSessionMetadata md = new DistributableSessionMetadata();
      md.setId(id + ".full");
      md.setCreationTime(System.currentTimeMillis());
      md.setNew(true);
      md.setValid(true);
      md.setMaxInactiveInterval(30000);
      Long ts = Long.valueOf(md.getCreationTime() + 1); 
      Map<String, Object> attrs = new HashMap<String, Object>();
      attrs.put("key", "value");
      OutgoingSessionGranularitySessionData sessionData = new MockOutgoingSessionData(id, 1, ts, md, attrs);
      testee.storeSessionData(sessionData);
      
      IncomingDistributableSessionData incoming = testee.getSessionData(id, true);
      assertEquals(1, incoming.getVersion());
   }

   /**
    * Test method for {@link org.jboss.web.tomcat.service.session.persistent.RDBMSStoreBase#processExpires()}.
    */
   public void testProcessExpires()
   {
      testee.setCleanupInterval(2);
      testee.setMaxUnreplicatedInterval(0);
      testee.start();
      
      testee.clear();
      
      int existing = testee.getSize();
      
      DistributableSessionMetadata md = new DistributableSessionMetadata();
      String id = nextId();
      md.setId(id + ".full");
      md.setCreationTime(System.currentTimeMillis());
      md.setNew(true);
      md.setValid(true);
      md.setMaxInactiveInterval(1);
      Long ts = Long.valueOf(md.getCreationTime() + 1); 
      Map<String, Object> attrs = new HashMap<String, Object>();
      attrs.put("key", "value");
      OutgoingSessionGranularitySessionData sessionData = new MockOutgoingSessionData(id, 0, ts, md, attrs);
      testee.storeSessionData(sessionData);
      
      sleep(500);
      
      testee.processExpires();
      
      assertEquals(Integer.valueOf(0), testee.getSessionVersion(id));
      
      sleep(2010);
      
      testee.processExpires();
      
      assertNull(testee.getSessionVersion(id));
      
      assertEquals(existing, testee.getSize());
   }

   /**
    * Test method for {@link org.jboss.web.tomcat.service.session.persistent.RDBMSStoreBase#processExpires()}.
    */
   public void testProcessExpiresWithMaxUnreplicatedInterval()
   {
      testee.setCleanupInterval(1);
      testee.setMaxUnreplicatedInterval(1);
      testee.start();
      
      testee.clear();
      
      int existing = testee.getSize();
      
      DistributableSessionMetadata md = new DistributableSessionMetadata();
      String id = nextId();
      md.setId(id + ".full");
      md.setCreationTime(System.currentTimeMillis());
      md.setNew(true);
      md.setValid(true);
      md.setMaxInactiveInterval(2);
      Long ts = Long.valueOf(md.getCreationTime() + 1); 
      Map<String, Object> attrs = new HashMap<String, Object>();
      attrs.put("key", "value");
      OutgoingSessionGranularitySessionData sessionData = new MockOutgoingSessionData(id, 0, ts, md, attrs);
      testee.storeSessionData(sessionData);
      
      sleep(500);
      
      testee.processExpires();
      
      assertEquals(Integer.valueOf(0), testee.getSessionVersion(id));
      
      sleep(2010);
      
      testee.processExpires();
      
      assertEquals(Integer.valueOf(0), testee.getSessionVersion(id));
      
      assertEquals(existing +1 , testee.getSize());
      
      sleep(1010);
      
      testee.processExpires();
      
      assertNull(testee.getSessionVersion(id));
      
      assertEquals(existing, testee.getSize());
   }

   private void sleep(int sleeptime)
   {
      try
      {
         Thread.sleep(sleeptime);
      }
      catch (InterruptedException ignored)
      {
         ;
      }
   }

}
