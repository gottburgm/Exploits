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
package org.jboss.test.jmx.compliance.metadata;


import javax.management.MBeanFeatureInfo;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

/**
 * Tests MBeanFeatureInfo.
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @version $Revision: 81019 $ 
 */
public class MBeanFeatureInfoTEST extends TestCase
{
   public MBeanFeatureInfoTEST(String s)
   {
      super(s);
   }

   /**
    * Tests <tt>MBeanOperationInfo(String descr, Method m)</tt> constructor.
    */
   public void testConstructor()
   {
      try 
      {
         MBeanFeatureInfo info = new MBeanFeatureInfo("Name", "This is a description.");
         
         assertTrue(info.getName().equals("Name"));
         assertTrue(info.getDescription().equals("This is a description."));
      }
      catch (AssertionFailedError e)
      {
         throw e;
      }
      catch (Throwable t)
      {
         t.printStackTrace();
         fail("Unexpected error: " + t.toString());
      }
   }

   public void testHashCode()
      throws Exception
   {
      MBeanFeatureInfo info1 = new MBeanFeatureInfo("name", "description");
      MBeanFeatureInfo info2 = new MBeanFeatureInfo("name", "description");

      assertTrue("Different instances with the same hashcode are equal", info1.hashCode() == info2.hashCode());
   }

   public void testEquals()
      throws Exception
   {
      MBeanFeatureInfo info = new MBeanFeatureInfo("name", "description");

      assertTrue("Null should not be equal", info.equals(null) == false);
      assertTrue("Only MBeanFeatureInfo should be equal", info.equals(new Object()) == false);

      MBeanFeatureInfo info2 = new MBeanFeatureInfo("name", "description");

      assertTrue("Different instances of the same data are equal", info.equals(info2));
      assertTrue("Different instances of the same data are equal", info2.equals(info));

      info2 = new MBeanFeatureInfo("name2", "description");

      assertTrue("Different instances with different names are not equal", info.equals(info2) == false);
      assertTrue("Different instances with different names are not equal", info2.equals(info) == false);

      info2 = new MBeanFeatureInfo("name", "description2");

      assertTrue("Different instances with different descriptions are not equal", info.equals(info2) == false);
      assertTrue("Different instances with different descritpions are not equal", info2.equals(info) == false);
   }
}
