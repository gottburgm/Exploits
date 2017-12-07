/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.cmp2.jdbc2pm.ejbstore.test;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.jboss.test.JBossTestCase;
import org.jboss.test.cmp2.jdbc2pm.ejbstore.ejb.ALocal;
import org.jboss.test.cmp2.jdbc2pm.ejbstore.ejb.ALocalHome;
import org.jboss.test.cmp2.jdbc2pm.ejbstore.ejb.BLocal;
import org.jboss.test.cmp2.jdbc2pm.ejbstore.ejb.BLocalHome;
import org.jboss.test.util.ejb.EJBTestCase;
import junit.framework.Test;

/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 44174 $</tt>
 */
public class JDBC2PmEjbStoreUnitTestCase
   extends EJBTestCase
{
   public static Test suite() throws Exception
   {
      return JBossTestCase.getDeploySetup(JDBC2PmEjbStoreUnitTestCase.class, "cmp2-jdbc2pm-ejbstoreb.jar, cmp2-jdbc2pm-ejbstore.jar");
   }

   public JDBC2PmEjbStoreUnitTestCase(String methodName)
   {
      super(methodName);
   }

   protected void setUp() throws Exception
   {
      getALocalHome().create(new Long(1), new Integer(2));
   }

   protected void tearDown() throws Exception
   {
      getALocalHome().remove(new Long(1));
   }

   // Tests

   public void testMain() throws Throwable
   {
      ALocal a = getALocalHome().findByPrimaryKey(new Long(1));
      int storeCount = a.getStoreCount().intValue();
      a.setIntField(new Integer(a.getIntField().intValue() + 1));
      assertEquals(storeCount + 1, a.getStoreCount().intValue());      
   }

   public void testCorrectView() throws Throwable
   {
      BLocal b = getBLocalHome().create(new Long(11), "test");
      ALocal a = getALocalHome().findByPrimaryKey(new Long(1));
   }

   // Private

   private ALocalHome getALocalHome()
      throws NamingException
   {
      return (ALocalHome)lookup("ALocal");
   }

   private BLocalHome getBLocalHome()
      throws NamingException
   {
      return (BLocalHome)lookup("BLocal");
   }

   private Object lookup(String name) throws NamingException
   {
      InitialContext ic = new InitialContext();
      return ic.lookup(name);
   }
}
