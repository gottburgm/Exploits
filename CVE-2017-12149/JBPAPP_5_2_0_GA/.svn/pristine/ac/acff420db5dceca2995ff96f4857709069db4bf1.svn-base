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
package org.jboss.test.jmx.compliance.standard;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import junit.framework.Assert;

public class InfoUtil
{
   public static MBeanInfo getMBeanInfo(Object mbean, String name)
   {
      MBeanInfo info = null;

      try
      {
         MBeanServer server = MBeanServerFactory.newMBeanServer();

         ObjectName objectName = new ObjectName(name);
         server.registerMBean(mbean, objectName);
         info = server.getMBeanInfo(objectName);
      }
      catch (MalformedObjectNameException e)
      {
         Assert.fail("got spurious MalformedObjectNameException");
      }
      catch (InstanceAlreadyExistsException e)
      {
         Assert.fail("got spurious InstanceAlreadyExistsException");
      }
      catch (MBeanRegistrationException e)
      {
         Assert.fail("got spurious MBeanRegistrationException");
      }
      catch (NotCompliantMBeanException e)
      {
         Assert.fail("got spurious NotCompliantMBeanException");
      }
      catch (InstanceNotFoundException e)
      {
         Assert.fail("got spurious InstanceNotFoundException");
      }
      catch (IntrospectionException e)
      {
         Assert.fail("got spurious IntrospectionException");
      }
      catch (ReflectionException e)
      {
         Assert.fail("got spurious ReflectionException");
      }

      return info;
   }

   public static MBeanAttributeInfo findAttribute(MBeanAttributeInfo[] attributes, String name)
   {
      for (int i = 0; i < attributes.length; i++)
      {
         if (attributes[i].getName().equals(name))
         {
            return attributes[i];
         }
      }
      return null;
   }

   public static void dumpConstructors(MBeanConstructorInfo[] constructors)
   {
      System.out.println("");
      System.out.println("Constructors:");
      for (int i = 0; i < constructors.length; i++)
      {
         StringBuffer dump = new StringBuffer();
         MBeanConstructorInfo constructor = constructors[i];
         dump.append("name=").append(constructor.getName());
         dump.append(",signature=").append(makeSignatureString(constructor.getSignature()));

         System.out.println(dump);
      }
   }

   public static void dumpAttributes(MBeanAttributeInfo[] attributes)
   {
      System.out.println("");
      System.out.println("Attributes:");
      for (int i = 0; i < attributes.length; i++)
      {
         StringBuffer dump = new StringBuffer();
         MBeanAttributeInfo attribute = attributes[i];
         dump.append("name=").append(attribute.getName());
         dump.append(",type=").append(attribute.getType());
         dump.append(",readable=").append(attribute.isReadable());
         dump.append(",writable=").append(attribute.isWritable());
         dump.append(",isIS=").append(attribute.isIs());
         System.out.println(dump);
      }
   }

   public static void dumpOperations(MBeanOperationInfo[] operations)
   {
      System.out.println("");
      System.out.println("Operations:");
      for (int i = 0; i < operations.length; i++)
      {
         StringBuffer dump = new StringBuffer();
         MBeanOperationInfo operation = operations[i];
         dump.append("name=").append(operation.getName());
         dump.append(",impact=").append(decodeImpact(operation.getImpact()));
         dump.append(",returnType=").append(operation.getReturnType());
         dump.append(",signature=").append(makeSignatureString(operation.getSignature()));

         System.out.println(dump);
      }
   }

   public static String makeSignatureString(MBeanParameterInfo[] info)
   {
      String[] sig = new String[info.length];
      for (int i = 0; i < info.length; i++)
      {
         sig[i] = info[i].getType();
      }
      return makeSignatureString(sig);
   }

   public static String makeSignatureString(String[] sig)
   {
      StringBuffer buf = new StringBuffer("(");
      for (int i = 0; i < sig.length; i++)
      {
         buf.append(sig[i]);
         if (i != sig.length - 1)
         {
            buf.append(",");
         }
      }
      buf.append(")");
      return buf.toString();
   }

   public static String decodeImpact(int impact)
   {
      switch (impact)
      {
         case MBeanOperationInfo.ACTION:
            return "ACTION";
         case MBeanOperationInfo.ACTION_INFO:
            return "ACTION_INFO";
         case MBeanOperationInfo.INFO:
            return "INFO";
         case MBeanOperationInfo.UNKNOWN:
            return "UNKNOWN";
      }
      throw new IllegalArgumentException("unknown impact value:" + impact);
   }
}
