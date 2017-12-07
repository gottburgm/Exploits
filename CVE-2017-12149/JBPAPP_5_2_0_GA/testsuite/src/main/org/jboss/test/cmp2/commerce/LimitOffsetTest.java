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
package org.jboss.test.cmp2.commerce;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.InitialContext;

import junit.framework.Test;

import org.jboss.ejb.EjbModule;
import org.jboss.ejb.plugins.cmp.ejbql.Catalog;
import org.jboss.ejb.plugins.cmp.jdbc.JDBCEJBQLCompiler;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCQueryMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCReadAheadMetaData;
import org.jboss.mx.util.MBeanServerLocator;
import org.jboss.test.JBossTestCase;
import org.jboss.test.util.ejb.EJBTestCase;

public class LimitOffsetTest extends EJBTestCase {
   private JDBCEJBQLCompiler compiler;
   private Class[] params = { int.class, int.class };
   private JDBCQueryMetaData queryMetaData;
   private OrderHome orderHome;

   public static Test suite() throws Exception {
		return JBossTestCase.getDeploySetup(LimitOffsetTest.class, "cmp2-commerce.jar");
   }


   public LimitOffsetTest(String name) {
      super(name);
   }

   public void setUpEJB(Properties props) throws Exception
   {
      MBeanServer server = MBeanServerLocator.locateJBoss();
      ObjectName name = new ObjectName("jboss.j2ee:jndiName=commerce/Order,service=EJB");
      EjbModule ejbModule = (EjbModule) server.getAttribute(name, "EjbModule");
      Catalog catalog = (Catalog) ejbModule.getModuleData("CATALOG");
      compiler = new JDBCEJBQLCompiler(catalog);

      queryMetaData = new JDBCQueryMetaData()
      {
         public Method getMethod()
         {
            throw new UnsupportedOperationException();
         }

         public boolean isResultTypeMappingLocal()
         {
            return true;
         }

         public JDBCReadAheadMetaData getReadAhead()
         {
            return new JDBCReadAheadMetaData("on-load", 100, "*");
         }

         public Class getQLCompilerClass()
         {
            throw new UnsupportedOperationException();
         }

         public boolean isLazyResultSetLoading()
         {
            return false;
         }
      };

      Context ctx = new InitialContext();
      orderHome = (OrderHome) ctx.lookup("commerce/Order");

      for (Iterator i = orderHome.findAll().iterator(); i.hasNext(); )
      {
         Order order = (Order) i.next();
         i.remove();
         order.remove();
      }

      for (int i=100; i < 110; i++)
      {
         orderHome.create(new Long(i));
      }
   }

   public void tearDownEJB(Properties props) throws Exception
   {
      for (Iterator i = orderHome.findAll().iterator(); i.hasNext(); )
      {
         Order order = (Order) i.next();
         i.remove();
         order.remove();
      }
   }

   public void testCompiler() throws Exception
   {
      compiler.compileJBossQL("SELECT OBJECT(o) FROM OrderX o", Collection.class, params, queryMetaData);
      assertEquals("SELECT t0_o.ORDER_NUMBER FROM ORDER_DATA t0_o", compiler.getSQL());
      assertEquals(0, compiler.getLimitParam());
      assertEquals(0, compiler.getOffsetParam());

      compiler.compileJBossQL("SELECT OBJECT(o) FROM OrderX o OFFSET ?2", Collection.class, params, queryMetaData);
      assertEquals("SELECT t0_o.ORDER_NUMBER FROM ORDER_DATA t0_o", compiler.getSQL());
      assertEquals(2, compiler.getOffsetParam());
      assertEquals(0, compiler.getLimitParam());

      compiler.compileJBossQL("SELECT OBJECT(o) FROM OrderX o LIMIT ?1", Collection.class, params, queryMetaData);
      assertEquals("SELECT t0_o.ORDER_NUMBER FROM ORDER_DATA t0_o", compiler.getSQL());
      assertEquals(0, compiler.getOffsetParam());
      assertEquals(1, compiler.getLimitParam());

      compiler.compileJBossQL("SELECT OBJECT(o) FROM OrderX o OFFSET ?1 LIMIT ?2", Collection.class, params, queryMetaData);
      assertEquals("SELECT t0_o.ORDER_NUMBER FROM ORDER_DATA t0_o", compiler.getSQL());
      assertEquals(1, compiler.getOffsetParam());
      assertEquals(2, compiler.getLimitParam());

      try
      {
         compiler.compileJBossQL("SELECT OBJECT(o) FROM OrderX o OFFSET ?1", Collection.class,
            new Class[] { long.class }, queryMetaData);
         fail("Expected Exception due to non-int argument");
      }
      catch (Exception e)
      {
         // OK
      }
   }

   public void testLimitOffset() throws Exception
   {
      Set result;
      result = orderHome.getStuff("SELECT OBJECT(o) FROM OrderX o", new Object[] { } );
      checkKeys(result, new long[] { 100, 101, 102, 103, 104, 105, 106, 107, 108, 109});

      result = orderHome.getStuff("SELECT OBJECT(o) FROM OrderX o LIMIT ?1", new Object[] { new Integer(3) } );
      checkKeys(result, new long[] { 100, 101, 102 });

      result = orderHome.getStuff("SELECT OBJECT(o) FROM OrderX o OFFSET ?1", new Object[] { new Integer(3) } );
      checkKeys(result, new long[] { 103, 104, 105, 106, 107, 108, 109 });

      result = orderHome.getStuff("SELECT OBJECT(o) FROM OrderX o OFFSET ?1 LIMIT ?2", new Object[] { new Integer(0), new Integer(3) } );
      checkKeys(result, new long[] { 100, 101, 102 });

      result = orderHome.getStuff("SELECT OBJECT(o) FROM OrderX o OFFSET ?1 LIMIT ?2", new Object[] { new Integer(3), new Integer(3) } );
      checkKeys(result, new long[] { 103, 104, 105 });

      result = orderHome.getStuff("SELECT OBJECT(o) FROM OrderX o OFFSET ?1 LIMIT ?2", new Object[] { new Integer(6), new Integer(3) } );
      checkKeys(result, new long[] { 106, 107, 108 });

      result = orderHome.getStuff("SELECT OBJECT(o) FROM OrderX o OFFSET ?1 LIMIT ?2", new Object[] { new Integer(9), new Integer(3) } );
      checkKeys(result, new long[] { 109 });
   }

   public void testFinderWithLimitOffset() throws Exception
   {
      Collection result;
      result = orderHome.findWithLimitOffset(6, 3);
      checkKeys(result, new long[] { 106, 107, 108 });
   }

   private void checkKeys(Collection c, long[] expected)
   {
      assertEquals(expected.length, c.size());
      Set expectedSet = new HashSet(expected.length);
      for (int i = 0; i < expected.length; i++)
      {
         long l = expected[i];
         expectedSet.add(new Long(l));
      }

      Set actualSet = new HashSet(c.size());
      for (Iterator iterator = c.iterator(); iterator.hasNext();)
      {
         Order order = (Order) iterator.next();
         actualSet.add(order.getPrimaryKey());
      }

      assertEquals(expectedSet, actualSet);
   }
}
