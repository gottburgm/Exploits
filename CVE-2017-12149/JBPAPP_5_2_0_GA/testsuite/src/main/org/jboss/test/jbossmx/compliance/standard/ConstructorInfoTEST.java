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

import javax.management.MBeanConstructorInfo;
import javax.management.MBeanInfo;

/**
 * @author  <a href="mailto:trevor@protocool.com">Trevor Squires</a>.
 */

public class ConstructorInfoTEST extends TestCase
{
   private String failureHint;
   private MBeanInfo info;
   private String constructorName;
   private String signatureString;

   public ConstructorInfoTEST(String failureHint, MBeanInfo info, String constructorName, String[] signature)
   {
      super("testValidConstructor");
      this.failureHint = failureHint;
      this.info = info;
      this.constructorName = constructorName;
      this.signatureString = InfoUtil.makeSignatureString(signature);
   }

   public void testValidConstructor()
   {
      MBeanConstructorInfo[] constructors = info.getConstructors();

      MBeanConstructorInfo foundConstructor= null;

      for (int i = 0; i < constructors.length; i++)
      {
            if (signatureString.equals(InfoUtil.makeSignatureString(constructors[i].getSignature())))
            {
               foundConstructor = constructors[i];
               break;
            }
      }

      assertNotNull(failureHint + ": " + info.getClassName() + "." + constructorName + signatureString + " was not found", foundConstructor);
   }
}
