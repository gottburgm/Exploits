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
package org.jboss.test.cmp2.relationship.oneToOneUnidirectional;

import java.util.Iterator;
import java.util.Properties;
import javax.naming.InitialContext;
import junit.framework.Test;
import org.jboss.test.JBossTestCase;
import org.jboss.test.util.ejb.EJBTestCase;

public class ABTest extends EJBTestCase {
    static org.jboss.logging.Logger log =
       org.jboss.logging.Logger.getLogger(ABTest.class);

	public static Test suite() throws Exception {
		return JBossTestCase.getDeploySetup(ABTest.class, "cmp2-relationship.jar");
   }

   public ABTest(String name) {
      super(name);
   }

   private AHome getTableAHome() {
      try {
         InitialContext jndiContext = new InitialContext();

         return (AHome) 
               jndiContext.lookup("relation/oneToOne/unidirectional/table/A");
      } catch(Exception e) {
         log.debug("failed", e);
         fail("Exception in getTableAHome: " + e.getMessage());
      }
      return null;
   }

   private BHome getTableBHome() {
      try {
         InitialContext jndiContext = new InitialContext();

         return (BHome) 
               jndiContext.lookup("relation/oneToOne/unidirectional/table/B");
      } catch(Exception e) {
         log.debug("failed", e);
         fail("Exception in getTableBHome: " + e.getMessage());
      }
      return null;
   }

   private AHome getFKAHome() {
      try {
         InitialContext jndiContext = new InitialContext();

         return (AHome) 
               jndiContext.lookup("relation/oneToOne/unidirectional/fk/A");
      } catch(Exception e) {
         log.debug("failed", e);
         fail("Exception in getFKAHome: " + e.getMessage());
      }
      return null;
   }

   private BHome getFKBHome() {
      try {
         InitialContext jndiContext = new InitialContext();

         return (BHome) 
               jndiContext.lookup("relation/oneToOne/unidirectional/fk/B");
      } catch(Exception e) {
         log.debug("failed", e);
         fail("Exception in getFKBHome: " + e.getMessage());
      }
      return null;
   }

   private A a1;
   private A a2;
   private B b1;
   private B b2;

   protected void beforeChange(AHome aHome, BHome bHome) throws Exception {
      a1 = aHome.create(new Integer(1));
      a2 = aHome.create(new Integer(2));
      b1 = bHome.create(new Integer(10));
      b2 = bHome.create(new Integer(20));
      a1.setB(b1);
      a2.setB(b2);

      assertTrue(b1.isIdentical(a1.getB()));
      assertTrue(b2.isIdentical(a2.getB()));
   }

   // a1.setB(a2.getB());
   public void test_a1SetB_a2GetB_Table() throws Exception {
      AHome aHome = getTableAHome();
      BHome bHome = getTableBHome();

      beforeChange(aHome, bHome);
      a1SetB_a2GetB(aHome, bHome);
   }

   // a1.setB(a2.getB());
   public void test_a1SetB_a2GetB_FK() throws Exception {
      AHome aHome = getFKAHome();
      BHome bHome = getFKBHome();
      beforeChange(aHome, bHome);
      a1SetB_a2GetB(aHome, bHome);
   }

   // a1.setB(a2.getB());
   protected void a1SetB_a2GetB(AHome aHome, BHome bHome) throws Exception {
      // Change:
      a1.setB(a2.getB());

      // Expected result:

      // b2.isIdentical(a1.getB())
      assertTrue(b2.isIdentical(a1.getB()));

      // a2.getB() == null
      assertNull(a2.getB());
   }

   public void setUpEJB(Properties props) throws Exception {
      AHome aHome;
      BHome bHome;

      aHome = getTableAHome();
      bHome = getTableBHome();
      deleteAllAsAndBs(aHome, bHome);

      aHome = getFKAHome();
      bHome = getFKBHome();
      deleteAllAsAndBs(aHome, bHome);
   }

   public void deleteAllAsAndBs(AHome aHome, BHome bHome) throws Exception {
      // delete all As
      Iterator currentAs = aHome.findAll().iterator();
      while(currentAs.hasNext()) {
         A a = (A)currentAs.next();
         a.remove();
      }

      // delete all Bs
      Iterator currentBs = bHome.findAll().iterator();
      while(currentBs.hasNext()) {
         B b = (B)currentBs.next();
         b.remove();
      }
   }
}



