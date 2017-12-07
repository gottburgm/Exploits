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
package test.implementation.util.support;

import javax.management.*;
import javax.management.modelmbean.*;

/**
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @version $Revision: 81022 $
 */
public class ExtendedResource implements MyInterface2 
{
   private String attr  =  null;
   private String attr2 =  null;
   private Object arg   =  null;
   
   public void setAttributeName(String attr)
   {
      this.attr = attr;   
   }
   
   public void setAttributeName2(String attr)
   {
      this.attr2 = attr;   
   }
   
   public String getAttributeName2()
   {
      return attr2;
   }
   
   public void setAttribute3(Object arg)
   {
      this.arg = arg;
   }
   
   public Object getAttribute3()
   {
      return arg.toString();
   }
   
   public Object doOperation()
   {
      return "doOperation";
   }
   
   public String executeThis(Object arg)
   {
      return arg.toString();
   }      
   
   public Object runMe(String str)
   {
      return str;
   }
   
   public ModelMBeanInfo getMBeanInfo() 
   {
      ModelMBeanAttributeInfo[] attributes = new ModelMBeanAttributeInfo[] 
      {
         new ModelMBeanAttributeInfo(
               "AttributeName", "java.lang.String", "description",
               false, true, false
         ),
         new ModelMBeanAttributeInfo(
               "AttributeName2", "java.lang.String", "description",
               true, true, false
         ),
         new ModelMBeanAttributeInfo(
               "Attribute3", "java.lang.Object", "description",
               true, true, false
         )
      };
      
      ModelMBeanOperationInfo[] operations = new ModelMBeanOperationInfo[]
      {
         new ModelMBeanOperationInfo(
               "doOperation", "description", null, "java.lang.Object", 1
         ),
         new ModelMBeanOperationInfo(
               "executeThis", "description", 
                     
                     new MBeanParameterInfo[] {
                        new MBeanParameterInfo(
                           "arg", "java.lang.Object", "description"
                        )
                     },
                     "java.lang.Object", 1
         ),
         new ModelMBeanOperationInfo(
               "runMe", "description",
               
                     new MBeanParameterInfo[] {
                        new MBeanParameterInfo(
                           "arg", "java.lang.String", "description"
                        )
                     },
                     "java.lang.Object", 1
          )
      };
      
      ModelMBeanInfoSupport info = new ModelMBeanInfoSupport(
            "test.implementation.util.support.Resource", "description",
            attributes, null, operations, null
      );
      
      return info;
   }
   
}
