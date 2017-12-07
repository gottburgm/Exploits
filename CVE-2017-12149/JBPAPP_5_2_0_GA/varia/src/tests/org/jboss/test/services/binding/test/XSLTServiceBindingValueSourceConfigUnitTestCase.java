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

package org.jboss.test.services.binding.test;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.jboss.services.binding.impl.XSLTServiceBindingValueSourceConfig;

/**
 * Tests of {@link XSLTServiceBindingValueSourceConfig}.
 * 
 * @author <a href="brian.stansberry@jboss.com">Brian Stansberry</a>
 * @version $Revision: 85945 $
 */
public class XSLTServiceBindingValueSourceConfigUnitTestCase extends TestCase
{

   /**
    * Create a new XSLTServiceBindingValueSourceConfigUnitTestCase.
    * 
    * @param arg0
    */
   public XSLTServiceBindingValueSourceConfigUnitTestCase(String arg0)
   {
      super(arg0);
   }

   /**
    * Test method for {@link org.jboss.services.binding.impl.XSLTServiceBindingValueSourceConfig#getAdditionalAttributes()}.
    */
   public void testGetAdditionalAttributes()
   {
      XSLTServiceBindingValueSourceConfig config = new XSLTServiceBindingValueSourceConfig("test");
      Map<String, String> addl = config.getAdditionalAttributes();
      assertNotNull(addl);
      assertEquals(0, addl.size());
      
      addl = new HashMap<String, String>();
      addl.put("a", "a");
      
      config = new XSLTServiceBindingValueSourceConfig("test", addl);
      Map<String, String> addl1 = config.getAdditionalAttributes();
      assertEquals(addl, addl1);
      
   }

}
