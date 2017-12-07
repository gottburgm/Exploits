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
package org.jboss.test.cmp2.ejbselect;

import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

import javax.ejb.FinderException;

import junit.framework.Test;
import org.jboss.test.JBossTestCase;
import org.jboss.test.util.ejb.EJBTestCase;

/**
 * @author others + <a href="mailto:alex@jboss.org">Alex Loubyansky</a>
 */
public class EJBSelectUnitTestCase extends EJBTestCase
{
   static org.jboss.logging.Logger log =
      org.jboss.logging.Logger.getLogger(EJBSelectUnitTestCase.class);

   public static Test suite() throws Exception
   {
      return JBossTestCase.getDeploySetup(EJBSelectUnitTestCase.class, "cmp2-ejbselect.jar");
   }

   public EJBSelectUnitTestCase(String name)
   {
      super(name);
   }

   private ALocal a1;
   private ALocal a2;
   private BLocal b1;
   private BLocal b2;
   private BLocal b3;
   private BLocal b4;

   public void setUpEJB(Properties props) throws Exception
   {
      ALocalHome ahome = AUtil.getLocalHome();
      BLocalHome bhome = BUtil.getLocalHome();

      a1 = ahome.create("A1");
      a1.setIntField(3);
      Collection bs = a1.getBs();
      b1 = bhome.create("B1", "Alice", true);
      bs.add(b1);
      b2 = bhome.create("B2", "Bob", true);
      bs.add(b2);
      b3 = bhome.create("B3", "Charlie", false);
      bs.add(b3);
      b4 = bhome.create("B4", "Dan", false);
      bs.add(b4);

      a2 = ahome.create("A2");
      a2.setIntField(9);
   }

   public void tearDownEJB(Properties props) throws Exception
   {
      a1.remove();
      a2.remove();
   }

   public void testReturnedInterface() throws Exception
   {
      Iterator i = a1.getSomeBs().iterator();
      while(i.hasNext())
      {
         Object obj = i.next();
         assertTrue(obj instanceof BLocal);
         BLocal b = (BLocal)obj;
         b.getName();
      }

      i = a1.getSomeBs().iterator();
      while(i.hasNext())
      {
         Object obj = i.next();
         assertTrue(obj instanceof BLocal);
         BLocal b = (BLocal)obj;
         b.getName();
      }
   }

   public void testEJBSelectFromEJBHomeMethod() throws Exception
   {
      ALocalHome ahome = AUtil.getLocalHome();
      Collection results = ahome.getSomeBs(a1);
      for(Iterator iterator = results.iterator(); iterator.hasNext();)
      {
         Object obj = iterator.next();
         assertTrue(obj instanceof BLocal);
         BLocal b = (BLocal)obj;
         b.getName();
      }

      assertTrue(results.contains(b1));
      assertTrue(results.contains(b2));
      assertTrue(results.contains(b3));
      assertTrue(results.contains(b4));
      assertEquals(4, results.size());
   }

   public void testCheckFinderForNullInEJBSELECT() throws Exception
   {
      ALocalHome ahome = AUtil.getLocalHome();
      try
      {
          ahome.checkFinderForNull();
          fail("Should not be here");
      }
      catch (FinderException expected)
      {
         log.debug("Got expected error", expected);
      }
   }

   public void testGetSomeBxDeclaredSQL() throws Exception
   {
      ALocalHome ahome = AUtil.getLocalHome();
      Collection results = ahome.getSomeBsDeclaredSQL(a1);
      for(Iterator iterator = results.iterator(); iterator.hasNext();)
      {
         Object obj = iterator.next();
         assertTrue(obj instanceof BLocal);
         BLocal b = (BLocal)obj;
         b.getName();
      }

      assertTrue(results.contains(b1));
      assertTrue(results.contains(b2));
      assertTrue(results.contains(b3));
      assertTrue(results.contains(b4));
      assertEquals(4, results.size());
   }

   public void testGetTrue() throws Exception
   {
      Collection bs = b1.getTrue();
      assertEquals(2, bs.size());
      assertTrue(bs.contains(b1));
      assertTrue(bs.contains(b2));
      assertTrue(!bs.contains(b3));
      assertTrue(!bs.contains(b4));

      Iterator i = bs.iterator();
      while(i.hasNext())
      {
         BLocal b = (BLocal)i.next();
         assertTrue(b.getBool());
      }
   }

   public void testGetFalse() throws Exception
   {
      Collection bs = b1.getFalse();
      assertEquals(2, bs.size());
      assertTrue(!bs.contains(b1));
      assertTrue(!bs.contains(b2));
      assertTrue(bs.contains(b3));
      assertTrue(bs.contains(b4));

      Iterator i = bs.iterator();
      while(i.hasNext())
      {
         BLocal b = (BLocal)i.next();
         assertTrue(!b.getBool());
      }
   }

   public void testGetAWithBs() throws Exception
   {
      Collection as = a1.getAWithBs();
      assertEquals(1, as.size());
      assertTrue(as.contains(a1));
      assertTrue(!as.contains(a2));

      Iterator i = as.iterator();
      while(i.hasNext())
      {
         ALocal a = (ALocal)i.next();
         assertTrue(!a.getBs().isEmpty());
      }
   }

   // SQL funsctions in SELECT clause

   public void testCountInSelectClause() throws Exception
   {
      Collection result = BUtil.getLocalHome().selectDynamic(
         "SELECT COUNT(b.id) FROM B AS b", new Object[]{}
      );
      assertTrue("COUNT(b.id) = 4", ((Long)result.iterator().next()).longValue() == 4);
   }

   public void testMaxInSelectClause() throws Exception
   {
      Collection result = BUtil.getLocalHome().selectDynamic(
         "SELECT MAX(a.intField) FROM A AS a", new Object[]{}
      );
      assertTrue("MAX(a.id) = 9", ((Double)result.iterator().next()).doubleValue() == 9.0);
   }

   public void testMinInSelectClause() throws Exception
   {
      Collection result = BUtil.getLocalHome().selectDynamic(
         "SELECT MIN(a.intField) FROM A AS a", new Object[]{}
      );
      assertTrue("MIN(a.id) = 3", ((Double)result.iterator().next()).doubleValue() == 3.0);
   }

   public void testSumInSelectClause() throws Exception
   {
      Collection result = BUtil.getLocalHome().selectDynamic(
         "SELECT SUM(a.intField) FROM A AS a", new Object[]{}
      );
      assertTrue("SUM(a.id) = 12", ((Double)result.iterator().next()).doubleValue() == 12.0);
   }

   public void testAvgInSelectClause() throws Exception
   {
      Collection result = BUtil.getLocalHome().selectDynamic(
         "SELECT AVG(a.intField) FROM A AS a", new Object[]{}
      );
      assertTrue("AVG(a.id) = 6", ((Double)result.iterator().next()).doubleValue() == 6.0);
   }

   public void testSqrtInSelectClause() throws Exception
   {
      String pk = "B1";
      BLocal b = BUtil.getLocalHome().findByPrimaryKey(pk);
      b.setLongField(64);

      Collection result = BUtil.getLocalHome().selectDynamic(
         "SELECT SQRT(b.longField) FROM B AS b WHERE b.id = ?1", new Object[]{pk}
      );
      assertTrue("SQRT(b.longField) = 8", ((Double)result.iterator().next()).doubleValue() == 8.0);
   }

   /* HSQL has problems with returning ABS? (returns null)
   public void testAbsInSelectClause() throws Exception
   {
      String pk = "B1";
      BLocal b = BUtil.getLocalHome().findByPrimaryKey(pk);
      b.setLongField(-5);

      Collection result = BUtil.getLocalHome().selectDynamic(
         "SELECT ABS(b.longField) FROM B AS b WHERE b.id = ?1", new Object[]{pk}
      );
      assertTrue("ABS(b.longField) = 5", ((Long)result.iterator().next()).longValue() == 5);
   }
   */

   public void testLcaseInSelectClause() throws Exception
   {
      Collection result = BUtil.getLocalHome().selectDynamic(
         "SELECT LCASE(b.name) FROM B AS b WHERE b.id = ?1", new Object[]{"B1"}
      );
      assertTrue("LCASE(b.name) = alice", "alice".equals(result.iterator().next()));
   }

   public void testUcaseInSelectClause() throws Exception
   {
      Collection result = BUtil.getLocalHome().selectDynamic(
         "SELECT UCASE(b.name) FROM B AS b", new Object[]{}
      );
      assertTrue("result.size() == 4", result.size() == 4);
      assertTrue("result.contains('ALICE')", result.contains("ALICE"));
      assertTrue("result.contains('BOB')", result.contains("BOB"));
      assertTrue("result.contains('CHARLIE')", result.contains("CHARLIE"));
      assertTrue("result.contains('DAN')", result.contains("DAN"));
   }

   public void testLengthInSelectClause() throws Exception
   {
      Collection result = BUtil.getLocalHome().selectDynamic(
         "SELECT LENGTH(b.name) FROM B AS b WHERE b.id = ?1", new Object[]{"B1"}
      );
      assertTrue("LENGTH(b.name) = 5", ((Long)result.iterator().next()).longValue() == 5);
   }

   public void testConcatInSelectClause() throws Exception
   {
      Collection result = BUtil.getLocalHome().selectDynamic(
         "SELECT CONCAT('Dear ', b.name) FROM B AS b WHERE b.id = ?1", new Object[]{"B1"}
      );
      assertTrue("CONCAT('Dear ', b.name) = Dear Alice", "Dear Alice".equals(result.iterator().next()));
   }

   public void testLocateInSelectClause() throws Exception
   {
      Collection result = BUtil.getLocalHome().selectDynamic(
         "SELECT LOCATE('ice', b.name, 1) FROM B AS b WHERE b.id = ?1", new Object[]{"B1"}
      );
      assertTrue("LOCATE('ice', b.name, 1) = 3", ((Long)result.iterator().next()).longValue() == 3);
   }

   public void testSubstringInSelectClause() throws Exception
   {
      Collection result = BUtil.getLocalHome().selectDynamic(
         "SELECT SUBSTRING(b.name, 3, 5) FROM B AS b WHERE b.id = ?1", new Object[]{"B1"}
      );
      assertTrue("SUBSTRING(b.name, 3, 5) = ice", "ice".equals(result.iterator().next()));
   }

   public void testNestedFunctionsInSelectClause() throws Exception
   {
      Collection result = BUtil.getLocalHome().selectDynamic(
         "SELECT UCASE(SUBSTRING(CONCAT(b.id, b.name), 5, 7)) FROM B AS b WHERE b.id = ?1", new Object[]{"B1"}
      );
      assertTrue("UCASE(SUBSTRING(CONCAT(b.id, b.name), 5, 7)) = ICE", "ICE".equals(result.iterator().next()));
   }

   public void testLimit() throws Exception
   {
      Collection col = BUtil.getLocalHome().selectDynamic("select object(b) from B b where b.id is not null order by b.id desc limit 1", null);
      assertEquals(1, col.size());
      BLocal b = (BLocal)col.iterator().next();
      assertEquals("B4", b.getId());
   }

   public void testOffset() throws Exception
   {
      Collection col = BUtil.getLocalHome().selectDynamic("select object(b) from B b where b.id is not null order by b.id offset 1", null);
      assertEquals(3, col.size());
      Iterator iter = col.iterator();
      int i = 2;
      while(iter.hasNext())
      {
         BLocal b = (BLocal)iter.next();
         assertEquals("B" + i++, b.getId());
      }
   }

   public void testOffsetLimit() throws Exception
   {
      Collection col = BUtil.getLocalHome().selectDynamic("select object(b) from B b where b.id is not null order by b.id offset 1 limit 2", null);
      assertEquals(2, col.size());
      Iterator iter = col.iterator();
      int i = 2;
      while(iter.hasNext())
      {
         BLocal b = (BLocal)iter.next();
         assertEquals("B" + i++, b.getId());
      }
   }

   public void testMemberOf() throws Exception
   {
      Collection col = AUtil.getLocalHome().selectDynamic(
         "select distinct object(a) from A a, B b1, B b2 where b1.name=?1 and b2.name=?2 and b1 member of a.bs and b2 member of a.bs",
         new String[]{"Alice", "Bob"});
      assertEquals(1, col.size());
      ALocal a = (ALocal)col.iterator().next();
      assertNotNull(a);
      assertEquals("A1", a.getId());
   }
}
