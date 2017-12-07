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
package org.jboss.test.cmp2.jbas1665;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.jboss.test.JBossTestCase;
import org.jboss.test.util.ejb.EJBTestCase;
import junit.framework.Test;

/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 81036 $</tt>
 */
public class JBAS1665UnitTestCase
   extends EJBTestCase
{
   public static Test suite() throws Exception
   {
      return JBossTestCase.getDeploySetup(JBAS1665UnitTestCase.class, "cmp2-jbas1665.jar");
   }

   public JBAS1665UnitTestCase(String methodName)
   {
      super(methodName);
   }

   protected void setUp() throws Exception
   {
      getOrderLocalHome().create(new Integer(1), "order");
   }

   protected void tearDown() throws Exception
   {
      getOrderLocalHome().remove(new Integer(1));
   }

   // Tests

   public void testJBAS1665() throws Throwable
   {
      java.util.Collection all = getOrderLocalHome().findAll();
      assertEquals(1, all.size());
   }

   public void testJBAS3095() throws Exception
   {
      OrderLocalHome oh = getOrderLocalHome();
      oh.select("select object(o) from Order o where o.id > 0 order \t\n by o.id", null);
      oh.select("select object(o) from Order o order \n by o.id", null);
      oh.select("select object(byy) from Order byy order \n by byy.id", null);
   }

   // Private

   private OrderLocalHome getOrderLocalHome()
      throws NamingException
   {
      return (OrderLocalHome)lookup(OrderLocalHome.JNDI_NAME);
   }

   private Object lookup(String name) throws NamingException
   {
      InitialContext ic = new InitialContext();
      return ic.lookup(name);
   }
}
