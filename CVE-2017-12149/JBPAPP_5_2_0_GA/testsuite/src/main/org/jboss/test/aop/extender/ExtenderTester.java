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
package org.jboss.test.aop.extender;

/**
 * 
 * @author <a href="stalep@conduct.no">Stale W. Pedersen</a>
 * @version $Revision: 
 */
public class ExtenderTester implements ExtenderTesterMBean
{
   
   public void testMethod() throws Exception
   {
      
      System.out.println("--------------------------- TESTING EXTENDER ------------------");
      System.out.println("MY CLASSLOADER " + getClass().getClassLoader());
      System.out.println("EXTENDER INTERCEPTOR CLASSLOADER " + ExtenderInterceptor.class.getClassLoader());
      
      
      ChildBase childB = new ChildBase();
      childB.updateBase();
      if(!ExtenderInterceptor.method)
         throw new RuntimeException("Expected ExtenderInterceptor.method to be true, it was: "+ExtenderInterceptor.method);
      
      ExtenderInterceptor.method = false;      
      Base base = new SubBase();
      base.setBase(1);
      if(!ExtenderInterceptor.method)
         throw new RuntimeException("Expected ExtenderInterceptor.method to be true, it was: "+ExtenderInterceptor.method);
      
      ExtenderInterceptor.method = false;
      ChildExtender ext = new ChildExtender();
      ext.updateExtender();
      if(!ExtenderInterceptor.method)
         throw new RuntimeException("Expected ExtenderInterceptor.method to be true, it was: "+ExtenderInterceptor.method);
      
      ExtenderInterceptor.method = false;
      InfantBase infant = new InfantBase();
      infant.infantize(3);
      if(!ExtenderInterceptor.method)
         throw new RuntimeException("Expected ExtenderInterceptor.method to be true, it was: "+ExtenderInterceptor.method);
      
      
   }
   
}
