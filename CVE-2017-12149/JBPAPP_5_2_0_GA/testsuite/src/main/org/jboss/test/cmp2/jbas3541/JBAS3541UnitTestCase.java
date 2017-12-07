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
package org.jboss.test.cmp2.jbas3541;

import java.util.Iterator;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.jboss.test.JBossTestCase;
import org.jboss.test.util.ejb.EJBTestCase;
import junit.framework.Test;

/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 85945 $</tt>
 */
public class JBAS3541UnitTestCase
   extends EJBTestCase
{
   public static Test suite() throws Exception
   {
      return JBossTestCase.getDeploySetup(JBAS3541UnitTestCase.class, "cmp2-jbas3541.jar");
   }

   public JBAS3541UnitTestCase(String methodName)
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

   public void testJBAS3541() throws Throwable
   {
      Iterator all = getALocalHome().findByIntField(100000).iterator();
      assertTrue(all.hasNext());
      ALocal a = (ALocal)all.next();
      assertEquals(new Integer(2), a.getIntField());
   }

   // Private

   private ALocalHome getALocalHome()
      throws NamingException
   {
      return (ALocalHome)lookup("ALocal");
   }

   private Object lookup(String name) throws NamingException
   {
      InitialContext ic = new InitialContext();
      return ic.lookup(name);
   }
}
