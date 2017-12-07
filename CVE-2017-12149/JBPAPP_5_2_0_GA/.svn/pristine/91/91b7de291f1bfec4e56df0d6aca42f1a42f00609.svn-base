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
import java.util.Properties;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import junit.framework.Test;

import org.jboss.ejb.EjbModule;
import org.jboss.ejb.plugins.cmp.ejbql.Catalog;
import org.jboss.ejb.plugins.cmp.jdbc.JDBCEJBQLCompiler;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCQueryMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCReadAheadMetaData;
import org.jboss.mx.util.MBeanServerLocator;
import org.jboss.test.JBossTestCase;
import org.jboss.test.util.ejb.EJBTestCase;
import org.jboss.util.UnreachableStatementException;
import org.jboss.util.platform.Java;

public class QueryTest extends EJBTestCase
{
   private JDBCEJBQLCompiler compiler;
   private static final Class[] NO_PARAMS = new Class[]{};

   public static Test suite() throws Exception
   {
      return JBossTestCase.getDeploySetup(QueryTest.class, "cmp2-commerce.jar");
   }

   public QueryTest(String name)
   {
      super(name);
   }

   public void setUpEJB(Properties props) throws Exception
   {
      MBeanServer server = MBeanServerLocator.locateJBoss();
      ObjectName name = new ObjectName("jboss.j2ee:jndiName=commerce/Order,service=EJB");
      EjbModule ejbModule = (EjbModule) server.getAttribute(name, "EjbModule");
      Catalog catalog = (Catalog) ejbModule.getModuleData("CATALOG");
      compiler = new JDBCEJBQLCompiler(catalog);
   }

   private String compileEJBQL(String ejbql)
   {
      return compileEJBQL(ejbql, java.util.Collection.class, NO_PARAMS);
   }

   private String compileEJBQL(String ejbql, Class returnType, Class[] paramClasses)
   {
      try {
         compiler.compileEJBQL(ejbql, returnType, paramClasses,
            new JDBCQueryMetaData()
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
            });
         return compiler.getSQL().trim();
      } catch (Throwable t) {
         fail(t.getMessage());
         throw new UnreachableStatementException();
      }
   }

   private String compileJBossQL(String ejbql, Class returnType, Class[] paramClasses)
   {
      return compileJBossQL(ejbql, returnType, paramClasses,
            new JDBCQueryMetaData()
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
            });
   }

   private String compileJBossQL(String ejbql, Class returnType, Class[] paramClasses, JDBCQueryMetaData metadata)
   {
      try {
         compiler.compileJBossQL(ejbql, returnType, paramClasses, metadata);
         return compiler.getSQL();
      } catch (Throwable t) {
         fail(t.getMessage());
         throw new UnreachableStatementException();
      }
   }

   public void testJBossQL() throws Exception
   {
      boolean ibmJDK = System.getProperty("java.vm.vendor").indexOf("IBM") > -1;
      assertEquals("SELECT t0_u.USER_ID FROM USER_DATA t0_u WHERE (ucase(t0_u.USER_NAME) = ?)",
                   compileJBossQL("SELECT OBJECT(u) FROM user u WHERE UCASE(u.userName) = ?1",
                                  Collection.class, new Class[]{String.class}));

      assertEquals("SELECT t0_u.USER_ID FROM USER_DATA t0_u WHERE (lcase(t0_u.USER_NAME) = ?)",
                   compileJBossQL("SELECT OBJECT(u) FROM user u WHERE LCASE(u.userName) = ?1",
                                  Collection.class, new Class[]{String.class}));

      String expected = "SELECT t0_o1.ORDER_NUMBER FROM ORDER_DATA t0_o1, ORDER_DATA t3_o2, CUSTOMEREJB t1_o1_customer, CUSTOMEREJB t2_o2_customer WHERE (( NOT (t1_o1_customer.id=t2_o2_customer.id)) AND (t0_o1.CC_TYPE=t3_o2.CC_TYPE AND t0_o1.CC_FIRST_NAME=t3_o2.CC_FIRST_NAME AND t0_o1.CC_MI=t3_o2.CC_MI AND t0_o1.CC_LAST_NAME=t3_o2.CC_LAST_NAME AND t0_o1.CC_BILLING_ZIP=t3_o2.CC_BILLING_ZIP AND t0_o1.CC_CARD_NUMBER=t3_o2.CC_CARD_NUMBER) AND t0_o1.customer=t1_o1_customer.id AND t3_o2.customer=t2_o2_customer.id)";

      String compiled = compileJBossQL(
         "SELECT OBJECT(o1) FROM OrderX o1, OrderX o2 WHERE o1.customer <> o2.customer AND o1.creditCard = o2.creditCard",
         Collection.class, NO_PARAMS);
      assertEquals(expected, compiled);

      assertEquals("SELECT t0_o.ORDER_NUMBER " +
                   "FROM ORDER_DATA t0_o " +
                   "WHERE ((t0_o.CC_TYPE=? " +
                   "AND t0_o.CC_FIRST_NAME=? " +
                   "AND t0_o.CC_MI=? " +
                   "AND t0_o.CC_LAST_NAME=? " +
                   "AND t0_o.CC_BILLING_ZIP=? " +
                   "AND t0_o.CC_CARD_NUMBER=?))",
                   compileJBossQL("SELECT OBJECT(o) FROM OrderX o WHERE o.creditCard = ?1",
                                  Collection.class, new Class[]{Card.class}));

      assertEquals("SELECT t0_o.ORDER_NUMBER " +
                   "FROM ORDER_DATA t0_o " +
                   "WHERE (( NOT (t0_o.CC_TYPE=? " +
                   "AND t0_o.CC_FIRST_NAME=? " +
                   "AND t0_o.CC_MI=? " +
                   "AND t0_o.CC_LAST_NAME=? " +
                   "AND t0_o.CC_BILLING_ZIP=? " +
                   "AND t0_o.CC_CARD_NUMBER=?)))",
                   compileJBossQL("SELECT OBJECT(o) FROM OrderX o WHERE o.creditCard <> ?1",
                                  Collection.class, new Class[]{Card.class}));

      assertEquals(
         "SELECT DISTINCT t0_u.USER_ID, t0_u.USER_NAME FROM USER_DATA t0_u ORDER BY t0_u.USER_NAME ASC",
         compileJBossQL("SELECT DISTINCT OBJECT(u) FROM user u ORDER BY u.userName", Collection.class, NO_PARAMS)
      );
      assertEquals(
         "SELECT DISTINCT t0_u.USER_ID FROM USER_DATA t0_u ORDER BY t0_u.USER_ID ASC",
         compileJBossQL("SELECT DISTINCT OBJECT(u) FROM user u ORDER BY u.userId", Collection.class, NO_PARAMS)
      );
      assertEquals(
         "SELECT DISTINCT t0_u.USER_NAME FROM USER_DATA t0_u ORDER BY t0_u.USER_NAME ASC",
         compileJBossQL("SELECT DISTINCT u.userName FROM user u ORDER BY u.userName", Collection.class, NO_PARAMS)
      );
      assertEquals(
         "SELECT DISTINCT ucase(t0_u.USER_NAME) FROM USER_DATA t0_u ORDER BY t0_u.USER_NAME ASC",
         compileJBossQL("SELECT DISTINCT UCASE(u.userName) FROM user u ORDER BY u.userName",
            Collection.class, new Class[]{String.class})
      );
      assertEquals(
         "SELECT DISTINCT t0_u.USER_NAME, t0_u.USER_ID FROM USER_DATA t0_u ORDER BY t0_u.USER_ID ASC",
         compileJBossQL("SELECT DISTINCT u.userName FROM user u ORDER BY u.userId",
            Collection.class, new Class[]{String.class})
      );

      assertEquals(
         "SELECT t0_o.ORDER_NUMBER FROM ORDER_DATA t0_o, ADDRESSEJB t1_o_shippingAddress WHERE (t1_o_shippingAddress.city = ? AND t0_o.SHIPPING_ADDRESS=t1_o_shippingAddress.id) OR (t1_o_shippingAddress.state = ? AND t0_o.SHIPPING_ADDRESS=t1_o_shippingAddress.id)",
         compileJBossQL(
            "SELECT OBJECT(o) FROM OrderX o WHERE o.shippingAddress.city=?1 OR o.shippingAddress.state=?2",
            Collection.class,
            new Class[]{String.class, String.class}
         )
      );

      assertEquals(
         "SELECT t0_o.ORDER_NUMBER, t1_o_shippingAddress.state FROM ORDER_DATA t0_o, ADDRESSEJB t1_o_shippingAddress WHERE t0_o.SHIPPING_ADDRESS=t1_o_shippingAddress.id ORDER BY t1_o_shippingAddress.state ASC",
         compileJBossQL(
            "SELECT OBJECT(o) FROM OrderX o ORDER BY o.shippingAddress.state",
            Collection.class,
            new Class[]{String.class, String.class}
         )
      );

      JDBCQueryMetaData lazyMD = new JDBCQueryMetaData()
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
            return new JDBCReadAheadMetaData("on-load", 4, "*");
         }

         public Class getQLCompilerClass()
         {
            throw new UnsupportedOperationException();
         }

         public boolean isLazyResultSetLoading()
         {
            return true;
         }

      };

      assertEquals(
         "SELECT (SELECT count(t0_o.id) FROM ADDRESSEJB t0_o WHERE (t0_o.state = 'CA')), t0_o.id " +
         "FROM ADDRESSEJB t0_o WHERE (t0_o.state = 'CA')",
         compileJBossQL("select object(o) from Address o where o.state='CA'", Collection.class, NO_PARAMS, lazyMD)
      );

      assertEquals(
         "SELECT DISTINCT (SELECT count(DISTINCT t0_o.id) FROM LINEITEMEJB t0_o WHERE (t0_o.quantity > 1000)), t0_o.id " +
         "FROM LINEITEMEJB t0_o WHERE (t0_o.quantity > 1000)",
         compileJBossQL("select object(o) from LineItem o where o.quantity > 1000 offset 1 limit 2", Set.class, NO_PARAMS, lazyMD)
      );

      assertEquals(
         "SELECT (SELECT count(t0_o.city) FROM ADDRESSEJB t0_o WHERE (t0_o.state = 'CA')), t0_o.city " +
         "FROM ADDRESSEJB t0_o WHERE (t0_o.state = 'CA')",
         compileJBossQL("select o.city from Address o where o.state='CA'", Collection.class, NO_PARAMS, lazyMD)
      );

      assertEquals(
         "SELECT DISTINCT (SELECT count(DISTINCT t0_o.city) FROM ADDRESSEJB t0_o WHERE (t0_o.state = 'CA')), t0_o.city " +
         "FROM ADDRESSEJB t0_o WHERE (t0_o.state = 'CA')",
         compileJBossQL("select distinct o.city from Address o where o.state='CA'", Collection.class, NO_PARAMS, lazyMD)
      );
   }

   public void testEJBQL() throws Exception
   {
      assertEquals("SELECT t0_o.ORDER_NUMBER FROM ORDER_DATA t0_o",
                   compileEJBQL("SELECT OBJECT(o) FROM OrderX o"));

      assertEquals(
         "SELECT t0_o.ORDER_NUMBER FROM ORDER_DATA t0_o, ADDRESSEJB t1_o_shippingAddress WHERE (t1_o_shippingAddress.city = ? AND t0_o.SHIPPING_ADDRESS=t1_o_shippingAddress.id) OR (t1_o_shippingAddress.state = ? AND t0_o.SHIPPING_ADDRESS=t1_o_shippingAddress.id)",
         compileEJBQL(
            "SELECT OBJECT(o) FROM OrderX o WHERE o.shippingAddress.city=?1 OR o.shippingAddress.state=?2",
            Collection.class,
            new Class[]{String.class, String.class}
         )
      );

      String expected =
         "SELECT t0_o.ORDER_NUMBER " +
         "FROM ORDER_DATA t0_o, LINEITEMEJB t4_l, PRODUCTCATEGORYEJB t1_pc, PRODUCT_PRODUCT_CATEGORY t5_l_product_productCategories_R, PRODUCT t6_l_product " +
         "WHERE (((t0_o.ORDER_NUMBER = ? AND t1_pc.name = ?))) " +
	     "AND t0_o.ORDER_NUMBER=t4_l.ORDER_NUMBER " +
         "AND t6_l_product.id=t5_l_product_productCategories_R.PRODUCT_ID " +
         "AND t1_pc.id=t5_l_product_productCategories_R.PRODUCT_CATEGORY_ID " +
         "AND t1_pc.subId=t5_l_product_productCategories_R.PRODUCT_CATEGORY_SUBID " +
         "AND t4_l.product=t6_l_product.id";
      String compiled = compileEJBQL("SELECT OBJECT(o) FROM OrderX o, " +
                                      "IN(o.lineItems) l, " +
                                      "IN(l.product.productCategories) pc " +
                                      "WHERE (o.ordernumber = ?1 and pc.name=?2)",
                                      Collection.class, new Class[]{Long.class, String.class});
      assertEquals(expected, compiled);

      expected = "SELECT DISTINCT t0_o.ORDER_NUMBER " +
         "FROM ORDER_DATA t0_o, LINEITEMEJB t3_l " +
         "WHERE (t0_o.ORDER_NUMBER = ?) OR (EXISTS (SELECT t2_o_lineItems.id FROM LINEITEMEJB t2_o_lineItems " +
         "WHERE t0_o.ORDER_NUMBER=t2_o_lineItems.ORDER_NUMBER AND t2_o_lineItems.id=t3_l.id))";
      compiled = compileEJBQL("SELECT OBJECT(o) FROM OrderX o, LineItem l WHERE o.ordernumber = ?1 OR l MEMBER o.lineItems",
         Set.class, new Class[]{Long.class});
      assertTrue("Expected: " + expected + " but got: " + compiled, expected.equals(compiled));

      assertEquals("SELECT DISTINCT t0_o.ORDER_NUMBER " +
                   "FROM ORDER_DATA t0_o, LINEITEMEJB t3_l " +
                   "WHERE (t0_o.ORDER_NUMBER = ?) OR ( NOT EXISTS (SELECT t2_o_lineItems.id FROM LINEITEMEJB t2_o_lineItems " +
                   "WHERE t0_o.ORDER_NUMBER=t2_o_lineItems.ORDER_NUMBER AND t2_o_lineItems.id=t3_l.id))",
                   compileEJBQL("SELECT OBJECT(o) FROM OrderX o, LineItem l WHERE o.ordernumber = ?1 OR l NOT MEMBER o.lineItems",
                                Set.class, new Class[]{Long.class}));

      assertEquals("SELECT DISTINCT t0_p.id " +
         "FROM PRODUCT t0_p, PRODUCTCATEGORYEJB t4_pc " +
         "WHERE (t0_p.id = ?) OR (EXISTS (" +
         "SELECT t3_p_productCategories_RELATION_.PRODUCT_CATEGORY_ID, t3_p_productCategories_RELATION_.PRODUCT_CATEGORY_SUBID " +
         "FROM PRODUCT_PRODUCT_CATEGORY t3_p_productCategories_RELATION_ " +
         "WHERE t0_p.id=t3_p_productCategories_RELATION_.PRODUCT_ID " +
         "AND t4_pc.id=t3_p_productCategories_RELATION_.PRODUCT_CATEGORY_ID " +
         "AND t4_pc.subId=t3_p_productCategories_RELATION_.PRODUCT_CATEGORY_SUBID))",
         compileEJBQL("SELECT OBJECT(p) FROM Product p, ProductCategory pc WHERE p.id = ?1 OR pc MEMBER p.productCategories",
            Set.class, new Class[]{Long.class}));

      assertEquals("SELECT DISTINCT t0_p.id " +
         "FROM PRODUCT t0_p, PRODUCTCATEGORYEJB t4_pc " +
         "WHERE (t0_p.id = ?) OR ( NOT EXISTS (" +
         "SELECT t3_p_productCategories_RELATION_.PRODUCT_CATEGORY_ID, t3_p_productCategories_RELATION_.PRODUCT_CATEGORY_SUBID " +
         "FROM PRODUCT_PRODUCT_CATEGORY t3_p_productCategories_RELATION_ " +
         "WHERE t0_p.id=t3_p_productCategories_RELATION_.PRODUCT_ID " +
         "AND t4_pc.id=t3_p_productCategories_RELATION_.PRODUCT_CATEGORY_ID " +
         "AND t4_pc.subId=t3_p_productCategories_RELATION_.PRODUCT_CATEGORY_SUBID))",
         compileEJBQL("SELECT OBJECT(p) FROM Product p, ProductCategory pc WHERE p.id = ?1 OR pc NOT MEMBER p.productCategories",
            Set.class, new Class[]{Long.class}));

      assertEquals("SELECT DISTINCT t0_o.ORDER_NUMBER " +
                   "FROM ORDER_DATA t0_o " +
                   "WHERE (t0_o.ORDER_NUMBER = ?) OR (EXISTS (SELECT t2_o_lineItems.id " +
                   "FROM LINEITEMEJB t2_o_lineItems " +
                   "WHERE t0_o.ORDER_NUMBER=t2_o_lineItems.ORDER_NUMBER))",
                   compileEJBQL("SELECT OBJECT(o) FROM OrderX o WHERE o.ordernumber = ?1 OR o.lineItems IS NOT EMPTY",
                                Set.class, new Class[]{Long.class}));

      assertEquals("SELECT t0_l.id FROM CUSTOMEREJB t1_c, ORDER_DATA t3_o, LINEITEMEJB t0_l WHERE ((t1_c.id = 1)) AND t1_c.id=t3_o.customer AND t3_o.ORDER_NUMBER=t0_l.ORDER_NUMBER",
        compileEJBQL("SELECT OBJECT(l) FROM Customer c, IN(c.orders) o, IN(o.lineItems) l WHERE c.id=1"));

      // customer query was SELECT OBJECT(s) FROM Service AS s, Platform AS p WHERE p.id = ?1 AND s.server MEMBER OF p.servers
      assertEquals("SELECT t0_l.id FROM LINEITEMEJB t0_l, CUSTOMEREJB t1_c, ORDER_DATA t3_l_order WHERE (t1_c.id = 1 AND EXISTS (SELECT t2_c_orders.ORDER_NUMBER FROM ORDER_DATA t2_c_orders WHERE t1_c.id=t2_c_orders.customer AND t2_c_orders.ORDER_NUMBER=t3_l_order.ORDER_NUMBER) AND t0_l.ORDER_NUMBER=t3_l_order.ORDER_NUMBER)",
                   compileEJBQL("SELECT OBJECT(l) FROM LineItem l, Customer c WHERE c.id=1 AND l.order MEMBER OF c.orders"));

      StringBuffer sql = new StringBuffer(200);
      sql.append("SELECT DISTINCT t0_li.id ")
         .append("FROM LINEITEMEJB t0_li, ORDER_DATA t1_li_order, ADDRESSEJB t2_li_order_billingAddress ")
         .append("WHERE (t1_li_order.BILLING_ADDRESS IS  NOT NULL AND t0_li.ORDER_NUMBER=t1_li_order.ORDER_NUMBER AND t1_li_order.BILLING_ADDRESS=t2_li_order_billingAddress.id)");
      assertEquals(
         sql.toString(),
         compileEJBQL("SELECT DISTINCT OBJECT(li) FROM LineItem AS li WHERE li.order.billingAddress IS NOT NULL")
      );
   }
}
