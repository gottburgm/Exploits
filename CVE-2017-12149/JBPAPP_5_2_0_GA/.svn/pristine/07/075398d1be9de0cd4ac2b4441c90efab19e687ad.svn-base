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
package org.jboss.test.jbossmx.compliance.standard;

import junit.framework.TestCase;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;

/**
 * @author  <a href="mailto:trevor@protocool.com">Trevor Squires</a>.
 */

public class AttributeInfoTEST 
   extends TestCase
{
   private String failureHint;
   private MBeanInfo info;
   private String attributeName;
   private String type;
   private boolean read;
   private boolean write;
   private boolean is;

   public AttributeInfoTEST(String failureHint, MBeanInfo info, String attributeName, String type, boolean read, boolean write, boolean is)
   {
      super("testValidAttribute");
      this.failureHint = failureHint;
      this.info = info;
      this.attributeName = attributeName;
      this.type = type;
      this.read= read;
      this.write= write;
      this.is= is;
   }

   public void testValidAttribute()
   {
      MBeanAttributeInfo[] attributes = info.getAttributes();
      MBeanAttributeInfo attribute = InfoUtil.findAttribute(attributes, attributeName);

      assertNotNull(failureHint + ": " + info.getClassName() + ": " + attributeName + " was not found", attribute);
      assertEquals(failureHint + ": " + info.getClassName() + ": " + attributeName + " type", type, attribute.getType());
      assertEquals(failureHint + ": " + info.getClassName() + ": " + attributeName + " readable", read, attribute.isReadable());
      assertEquals(failureHint + ": " + info.getClassName() + ": " + attributeName + " writable", write, attribute.isWritable());
      assertEquals(failureHint + ": " + info.getClassName() + ": " + attributeName + " isIS", is, attribute.isIs());
   }
}
