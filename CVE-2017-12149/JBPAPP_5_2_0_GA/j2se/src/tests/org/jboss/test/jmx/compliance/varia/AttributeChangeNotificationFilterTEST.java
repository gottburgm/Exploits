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
package org.jboss.test.jmx.compliance.varia;

import javax.management.AttributeChangeNotificationFilter;

import junit.framework.TestCase;

/**
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 */
public class AttributeChangeNotificationFilterTEST
  extends TestCase
{

  // Constructor ---------------------------------------------------------------

  /**
   * Construct the test
   */
  public AttributeChangeNotificationFilterTEST(String s)
  {
    super(s);
  }

  // Tests ---------------------------------------------------------------------

  public void testGetEnabledAttributes()
  {
      AttributeChangeNotificationFilter filter = new AttributeChangeNotificationFilter();
      
      assertTrue(filter.getEnabledAttributes().size() == 0);
      
      filter.enableAttribute("foo");
      filter.enableAttribute("bar");
      
      assertTrue(filter.getEnabledAttributes().size() == 2);
  }

  public void testDisableAttribute()
  {
     AttributeChangeNotificationFilter filter = new AttributeChangeNotificationFilter();
     
     filter.enableAttribute("foo");
     filter.enableAttribute("bar");
     
     assertTrue(filter.getEnabledAttributes().size() == 2);
     
     filter.disableAttribute("foo");
     
     assertTrue(filter.getEnabledAttributes().size() == 1);
     assertTrue(filter.getEnabledAttributes().get(0).equals("bar"));
     
     filter.disableAllAttributes();
     
     assertTrue(filter.getEnabledAttributes().size() == 0);
     
  }

}
