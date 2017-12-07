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
package org.jboss.test.cmp2.distinct;

import javax.naming.NamingException;
import javax.naming.InitialContext;
import javax.ejb.FinderException;
import org.jboss.test.util.ejb.EJBTestCase;
import org.jboss.test.JBossTestCase;
import junit.framework.Test;

/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 85945 $</tt>
 */
public class DistinctUnitTestCase
   extends EJBTestCase
{
   public static Test suite() throws Exception
   {
      return JBossTestCase.getDeploySetup(DistinctUnitTestCase.class, "cmp2-distinct.jar");
   }

   public DistinctUnitTestCase(String methodName)
   {
      super(methodName);
   }

   public void setUpEJB(java.util.Properties props) throws Exception
   {
      ALocalHome ah = getALocalHome();
      try
      {
         ah.findByPrimaryKey(new Integer(1));
      }
      catch(FinderException fe)
      {
         ALocal a = ah.create(new Integer(1), "a1");
         MyData data = new MyData();
         data.setData("data");
         a.setMyData(data);
      }
   }

   public void tearDownEJB(java.util.Properties props) throws Exception
   {
      getALocalHome().remove(new Integer(1));
   }

   public void testDistinctAndBLOB() throws Exception
   {
      java.util.Collection col = getALocalHome().findByName("a1");
      assertEquals(1, col.size());
      ALocal a = (ALocal)col.iterator().next();
      MyData myData = a.getMyData();
      assertNotNull(myData);
      assertEquals("data", myData.getData());
   }

   private ALocalHome getALocalHome()
      throws NamingException
   {
      return (ALocalHome)lookup(ALocalHome.JNDI_NAME);
   }

   private Object lookup(String name) throws NamingException
   {
      InitialContext ic = new InitialContext();
      return ic.lookup(name);
   }
}
