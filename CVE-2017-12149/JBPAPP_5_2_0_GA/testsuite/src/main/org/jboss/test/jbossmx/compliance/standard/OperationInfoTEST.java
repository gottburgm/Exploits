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

import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;

/**
 * @author  <a href="mailto:trevor@protocool.com">Trevor Squires</a>.
 */

public class OperationInfoTEST extends TestCase
{
   private String failureHint;
   private MBeanInfo info;
   private String operationName;
   private int impact;
   private String returnType;
   private String signatureString;

   public OperationInfoTEST(String failureHint, MBeanInfo info, String operationName, int impact, String returnType, String[] signature)
   {
      super("testValidOperation");
      this.failureHint = failureHint;
      this.info = info;
      this.operationName = operationName;
      this.impact = impact;
      this.returnType = returnType;
      this.signatureString = InfoUtil.makeSignatureString(signature);
   }

   public void testValidOperation()
   {
      MBeanOperationInfo[] operations = info.getOperations();

      MBeanOperationInfo foundOperation = null;

      for (int i = 0; i < operations.length; i++)
      {
         if (operations[i].getName().equals(operationName))
         {
            if (signatureString.equals(InfoUtil.makeSignatureString(operations[i].getSignature())))
            {
               foundOperation = operations[i];
               break;
            }
         }
      }

      assertNotNull(failureHint + ": " + info.getClassName() + "." + operationName + signatureString + " was not found", foundOperation);
      assertEquals(failureHint + ": " + info.getClassName() + "." + operationName + signatureString + " impact", impact, foundOperation.getImpact());
      assertEquals(failureHint + ": " + info.getClassName() + "." + operationName + signatureString + " returnType", returnType, foundOperation.getReturnType());
   }
}
