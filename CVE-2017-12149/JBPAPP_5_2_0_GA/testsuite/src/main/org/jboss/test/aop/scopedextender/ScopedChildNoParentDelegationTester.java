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
package org.jboss.test.aop.scopedextender;

/**
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 85945 $
 */
public class ScopedChildNoParentDelegationTester implements ScopedChildNoParentDelegationTesterMBean
{
   public void testLoaders() throws Exception
   {
      System.out.println("=============== ScopedChildNoParentDelegationTester - LOADERS ================");
      ClassLoader mine = this.getClass().getClassLoader();
      ClassLoader a3 = Child_A3.class.getClassLoader();
      ClassLoader base = Base_Base.class.getClassLoader();
      
      if (mine != a3) 
      {
         throw new RuntimeException("ClassLoaders for me and Child_A3 should be the same. Mine=" + mine + "; Child_A3=" + a3);
      }
      
      if (mine != base)
      {
         throw new RuntimeException("ClassLoaders for me and Base_Base should be the same. Mine=" + mine + "; Base_Base=" + base);
      }
   }
   
   public void testMethod() throws Exception
   {
      TestUtil testUtil = new TestUtil();
      System.out.println("=============== ScopedChildNoParentDelegationTester - METHOD ================");
      Child_A3 a3 = new Child_A3();
      
      clear();
      a3.a3();
      String m = "Child_A3.a3";
      testUtil.compare(m, "ChildAspect", new String[]{"a3", "a2", "a1", "base"}, ChildAspect.invoked);
      testUtil.compare(m, "BaseAspect", new String[]{"a3", "a2", "a1", "base"}, BaseAspect.invoked);
      testUtil.compare(m, "BaseParentAspect", new String[]{"a3", "a2", "a1", "base"}, BaseParentAspect.invoked);
      
      clear();
      a3.overridden();
      m = "Child_A3.overridden";
      testUtil.compare(m, "ChildAspect", new String[]{"overridden"}, ChildAspect.invoked);
      testUtil.compare(m, "BaseAspect", new String[]{"overridden"}, BaseAspect.invoked);
      testUtil.compare(m, "BaseParentAspect", new String[]{"overridden"}, BaseParentAspect.invoked);
      
      
      Child_A2 a2 = new Child_A2();
      
      clear();
      a2.a2();
      m = "Child_A2.a2";
      testUtil.compare(m, "ChildAspect", new String[]{"a2", "a1", "base"}, ChildAspect.invoked);
      testUtil.compare(m, "BaseAspect", new String[]{"a2", "a1", "base"}, BaseAspect.invoked);
      testUtil.compare(m, "BaseParentAspect", new String[]{"a2", "a1", "base"}, BaseParentAspect.invoked);
      
      clear();
      a2.overridden();
      m = "Child_A2.overridden";
      testUtil.compare(m, "ChildAspect", new String[]{"overridden"}, ChildAspect.invoked);
      testUtil.compare(m, "BaseAspect", new String[]{"overridden"}, BaseAspect.invoked);
      testUtil.compare(m, "BaseParentAspect", new String[]{"overridden"}, BaseParentAspect.invoked);
      
      //Base_Base overrides the version from parent/global ucl, so aspects deployed by us should apply to its methods
      Base_Base base = new Base_Base();
      
      clear();
      base.base();
      m = "Base_Base.base";
      testUtil.compare(m, "BaseAspect", new String[]{"base"}, BaseAspect.invoked);
      testUtil.compare(m, "ChildAspect", new String[]{}, ChildAspect.invoked);
      testUtil.compare(m, "BaseParentAspect", new String[]{"base"}, BaseParentAspect.invoked);
      
      clear();
      base.overridden();
      m = "Base_Base.overridden";
      testUtil.compare(m, "BaseAspect", new String[] {"overridden"}, BaseAspect.invoked);
      testUtil.compare(m, "ChildAspect", new String[] {}, ChildAspect.invoked);
      testUtil.compare(m, "BaseParentAspect", new String[]{"overridden"}, BaseParentAspect.invoked);
      
    
      //Base_A1 overrides the version from parent/global ucl, so aspects deployed by us should apply to its methods
      Base_A1 a1 = new Base_A1();

      clear();
      a1.a1();
      m = "Base_A1.a1";
      testUtil.compare(m, "BaseAspect", new String[]{"a1", "base"}, BaseAspect.invoked);
      testUtil.compare(m, "ChildAspect", new String[] {}, ChildAspect.invoked);
      testUtil.compare(m, "BaseParentAspect", new String[]{"a1", "base"}, BaseParentAspect.invoked);
      
      clear();
      a1.overridden();
      m = "Base_A1.overridden";
      testUtil.compare(m, "BaseAspect", new String[] {"overridden"}, BaseAspect.invoked);
      testUtil.compare(m, "ChildAspect", new String[] {}, ChildAspect.invoked);
      testUtil.compare(m, "BaseParentAspect", new String[]{"overridden"}, BaseParentAspect.invoked);
      
      Child_B1 b1 = new Child_B1();
      
      clear();
      b1.b1();
      m = "Child_B1.b1";
      testUtil.compare(m, "ChildAspect", new String[]{"b1", "base"}, ChildAspect.invoked);
      testUtil.compare(m, "BaseAspect", new String[]{"b1", "base"}, BaseAspect.invoked);
      testUtil.compare(m, "BaseParentAspect", new String[]{"b1", "base"}, BaseParentAspect.invoked);
      
      clear();
      b1.overridden();
      m = "Child_B1.overridden";
      testUtil.compare(m, "ChildAspect", new String[]{"overridden"}, ChildAspect.invoked);
      testUtil.compare(m, "BaseAspect", new String[]{"overridden"}, BaseAspect.invoked);
      testUtil.compare(m, "BaseParentAspect", new String[]{"overridden"}, BaseParentAspect.invoked);
      
      Child_B2 b2 = new Child_B2();
      
      clear();
      b2.b2();
      m = "Child_B2.b2";
      testUtil.compare(m, "ChildAspect", new String[]{"b2", "b1", "base"}, ChildAspect.invoked);
      testUtil.compare(m, "BaseAspect", new String[]{"b2", "b1", "base"}, BaseAspect.invoked);
      testUtil.compare(m, "BaseParentAspect", new String[]{"b2", "b1", "base"}, BaseParentAspect.invoked);
      
      clear();
      b2.overridden();
      m = "Child_B2.overridden";
      testUtil.compare(m, "ChildAspect", new String[]{"overridden"}, ChildAspect.invoked);
      testUtil.compare(m, "BaseAspect", new String[]{"overridden"}, BaseAspect.invoked);
      testUtil.compare(m, "BaseParentAspect", new String[]{"overridden"}, BaseParentAspect.invoked);
      
      Child_B3 b3 = new Child_B3();
      
      clear();
      b3.b3();
      m = "Child_B3.b3";
      testUtil.compare(m, "ChildAspect", new String[]{"b3", "b2", "b1", "base"}, ChildAspect.invoked);
      testUtil.compare(m, "BaseAspect", new String[]{"b3", "b2", "b1", "base"}, BaseAspect.invoked);
      testUtil.compare(m, "BaseParentAspect", new String[]{"b3", "b2", "b1", "base"}, BaseParentAspect.invoked);
      
      clear();
      b3.overridden();
      m = "Child_B3.overridden";
      testUtil.compare(m, "ChildAspect", new String[]{"overridden"}, ChildAspect.invoked);
      testUtil.compare(m, "BaseAspect", new String[]{"overridden"}, BaseAspect.invoked);
      testUtil.compare(m, "BaseParentAspect", new String[]{"overridden"}, BaseParentAspect.invoked);

      
      BaseNotBaseWoven bnbw = new BaseNotBaseWoven();
      clear();
      m = "BaseNotBaseWoven.notWovenInBase";
      bnbw.notWovenInBase();
      testUtil.compare(m, "ChildAspect", new String[]{"notWovenInBase"}, ChildAspect.invoked);
      testUtil.compare(m, "BaseAspect", new String[]{}, BaseAspect.invoked);
      testUtil.compare(m, "BaseParentAspect", new String[]{"notWovenInBase"}, BaseParentAspect.invoked);
      
      if (testUtil.getErrors() != null)
      {
         throw new RuntimeException(testUtil.getErrors());
      }
   }

   public void testConstructor() throws Exception
   {
      TestUtil testUtil = new TestUtil();
      System.out.println("=============== ScopedChildNoParentDelegationTester - CTOR ================");

      clear();
      Child_A3 a3 = new Child_A3();
      testUtil.compare(a3.getClass().getName(), "ChildAspect", new String[]{a3.getClass().getName()}, ChildAspect.invoked);
      testUtil.compare(a3.getClass().getName(), "BaseAspect", new String[]{a3.getClass().getName()}, BaseAspect.invoked);
      testUtil.compare(a3.getClass().getName(), "BaseParentAspect", new String[]{a3.getClass().getName()}, BaseParentAspect.invoked);
      
      clear();
      Child_A2 a2 = new Child_A2();
      testUtil.compare(a2.getClass().getName(), "ChildAspect", new String[]{a2.getClass().getName()}, ChildAspect.invoked);
      testUtil.compare(a2.getClass().getName(), "BaseAspect", new String[]{a2.getClass().getName()}, BaseAspect.invoked);
      testUtil.compare(a2.getClass().getName(), "BaseParentAspect", new String[]{a2.getClass().getName()}, BaseParentAspect.invoked);
      
      //Base_Base overrides the version from parent/global ucl, so aspects deployed by us should apply to its methods
      clear();
      Base_Base base = new Base_Base();
      testUtil.compare(base.getClass().getName(), "ChildAspect", new String[]{}, ChildAspect.invoked);
      testUtil.compare(base.getClass().getName(), "BaseAspect", new String[]{base.getClass().getName()}, BaseAspect.invoked);
      testUtil.compare(base.getClass().getName(), "BaseParentAspect", new String[]{base.getClass().getName()}, BaseParentAspect.invoked);
      
    
      //Base_A1 overrides the version from parent/global ucl, so aspects deployed by us should apply to its methods
      clear();
      Base_A1 a1 = new Base_A1();
      testUtil.compare(a1.getClass().getName(), "ChildAspect", new String[]{}, ChildAspect.invoked);
      testUtil.compare(a1.getClass().getName(), "BaseAspect", new String[]{a1.getClass().getName()}, BaseAspect.invoked);
      testUtil.compare(a1.getClass().getName(), "BaseParentAspect", new String[]{a1.getClass().getName()}, BaseParentAspect.invoked);
      
      clear();
      Child_B1 b1 = new Child_B1();
      testUtil.compare(b1.getClass().getName(), "ChildAspect", new String[]{b1.getClass().getName()}, ChildAspect.invoked);
      testUtil.compare(b1.getClass().getName(), "BaseAspect", new String[]{b1.getClass().getName()}, BaseAspect.invoked);
      testUtil.compare(b1.getClass().getName(), "BaseParentAspect", new String[]{b1.getClass().getName()}, BaseParentAspect.invoked);
      
      clear();
      Child_B2 b2 = new Child_B2();
      testUtil.compare(b2.getClass().getName(), "ChildAspect", new String[]{b2.getClass().getName()}, ChildAspect.invoked);
      testUtil.compare(b2.getClass().getName(), "BaseAspect", new String[]{b2.getClass().getName()}, BaseAspect.invoked);
      testUtil.compare(b2.getClass().getName(), "BaseParentAspect", new String[]{b2.getClass().getName()}, BaseParentAspect.invoked);
      
      clear();
      Child_B3 b3 = new Child_B3();
      testUtil.compare(b3.getClass().getName(), "ChildAspect", new String[]{b3.getClass().getName()}, ChildAspect.invoked);
      testUtil.compare(b3.getClass().getName(), "BaseAspect", new String[]{b3.getClass().getName()}, BaseAspect.invoked);
      testUtil.compare(b3.getClass().getName(), "BaseParentAspect", new String[]{b3.getClass().getName()}, BaseParentAspect.invoked);
      
      clear();
      BaseNotBaseWoven bnbw = new BaseNotBaseWoven();
      testUtil.compare(bnbw.getClass().getName(), "ChildAspect", new String[]{bnbw.getClass().getName()}, ChildAspect.invoked);
      testUtil.compare(bnbw.getClass().getName(), "BaseAspect", new String[]{}, BaseAspect.invoked);
      testUtil.compare(bnbw.getClass().getName(), "BaseParentAspect", new String[]{bnbw.getClass().getName()}, BaseParentAspect.invoked);
      
      if (testUtil.getErrors() != null)
      {
         throw new RuntimeException(testUtil.getErrors());
      }
   }

   public void testField() throws Exception
   {
      TestUtil testUtil = new TestUtil();
      System.out.println("=============== ScopedChildNoParentDelegationTester - FIELD ================");
      Child_A3 a3 = new Child_A3();
      
      clear();
      a3.a3 = 10;
      String m = "Child_A3.a3";
      testUtil.compare(m, "ChildAspect", new String[]{"a3"}, ChildAspect.invoked);
      testUtil.compare(m, "BaseAspect", new String[]{"a3"}, BaseAspect.invoked);
      testUtil.compare(m, "BaseParentAspect", new String[]{"a3"}, BaseParentAspect.invoked);
      
      clear();
      testUtil.compare(10, a3.a3);
      testUtil.compare(m, "ChildAspect", new String[]{"a3"}, ChildAspect.invoked);
      testUtil.compare(m, "BaseAspect", new String[]{"a3"}, BaseAspect.invoked);
      testUtil.compare(m, "BaseParentAspect", new String[]{"a3"}, BaseParentAspect.invoked);
      
      clear();
      a3.a2 = 10;
      m = "Child_A3.a2";
      testUtil.compare(m, "ChildAspect", new String[]{"a2"}, ChildAspect.invoked);
      testUtil.compare(m, "BaseAspect", new String[]{"a2"}, BaseAspect.invoked);
      testUtil.compare(m, "BaseParentAspect", new String[]{"a2"}, BaseParentAspect.invoked);
      
      clear();
      testUtil.compare(10, a3.a2);
      testUtil.compare(m, "ChildAspect", new String[]{"a2"}, ChildAspect.invoked);
      testUtil.compare(m, "BaseAspect", new String[]{"a2"}, BaseAspect.invoked);
      testUtil.compare(m, "BaseParentAspect", new String[]{"a2"}, BaseParentAspect.invoked);
      
      clear();
      a3.a1 = 10;
      m = "Child_A3.a1";
      testUtil.compare(m, "ChildAspect", new String[]{}, ChildAspect.invoked);
      testUtil.compare(m, "BaseAspect", new String[]{"a1"}, BaseAspect.invoked);
      testUtil.compare(m, "BaseParentAspect", new String[]{"a1"}, BaseParentAspect.invoked);
      
      clear();
      testUtil.compare(10, a3.a1);
      testUtil.compare(m, "ChildAspect", new String[]{}, ChildAspect.invoked);
      testUtil.compare(m, "BaseAspect", new String[]{"a1"}, BaseAspect.invoked);
      testUtil.compare(m, "BaseParentAspect", new String[]{"a1"}, BaseParentAspect.invoked);
      
      clear();
      a3.base = 10;
      m = "Child_A3.base";
      testUtil.compare(m, "ChildAspect", new String[]{}, ChildAspect.invoked);
      testUtil.compare(m, "BaseAspect", new String[]{"base"}, BaseAspect.invoked);
      testUtil.compare(m, "BaseParentAspect", new String[]{"base"}, BaseParentAspect.invoked);
      
      clear();
      testUtil.compare(10, a3.base);
      testUtil.compare(m, "ChildAspect", new String[]{}, ChildAspect.invoked);
      testUtil.compare(m, "BaseAspect", new String[]{"base"}, BaseAspect.invoked);
      testUtil.compare(m, "BaseParentAspect", new String[]{"base"}, BaseParentAspect.invoked);
      
      
      //Base_A1 overrides the version from parent/global ucl, so aspects deployed by us should apply to its methods
      Base_A1 a1 = new Base_A1();

      clear();
      a1.a1 = 10;
      m = "Base_A1.a1";
      testUtil.compare(m, "ChildAspect", new String[]{}, ChildAspect.invoked);
      testUtil.compare(m, "BaseAspect", new String[]{"a1"}, BaseAspect.invoked);
      testUtil.compare(m, "BaseParentAspect", new String[]{"a1"}, BaseParentAspect.invoked);
      
      clear();
      testUtil.compare(10, a1.a1);
      testUtil.compare(m, "ChildAspect", new String[]{}, ChildAspect.invoked);
      testUtil.compare(m, "BaseAspect", new String[]{"a1"}, BaseAspect.invoked);
      testUtil.compare(m, "BaseParentAspect", new String[]{"a1"}, BaseParentAspect.invoked);
      
      clear();
      a1.base = 10;
      m = "Base_A1.base";
      testUtil.compare(m, "ChildAspect", new String[]{}, ChildAspect.invoked);
      testUtil.compare(m, "BaseAspect", new String[]{"base"}, BaseAspect.invoked);
      testUtil.compare(m, "BaseParentAspect", new String[]{"base"}, BaseParentAspect.invoked);
      
      clear();
      testUtil.compare(10, a1.base);
      testUtil.compare(m, "ChildAspect", new String[]{}, ChildAspect.invoked);
      testUtil.compare(m, "BaseAspect", new String[]{"base"}, BaseAspect.invoked);
      testUtil.compare(m, "BaseParentAspect", new String[]{"base"}, BaseParentAspect.invoked);

      //Base_A1 overrides the version from parent/global ucl, so aspects deployed by us should apply to its methods
      Base_Base base = new Base_Base();

      clear();
      base.base = 10;
      m = "Base_Base.base";
      testUtil.compare(m, "ChildAspect", new String[]{}, ChildAspect.invoked);
      testUtil.compare(m, "BaseAspect", new String[]{"base"}, BaseAspect.invoked);
      testUtil.compare(m, "BaseParentAspect", new String[]{"base"}, BaseParentAspect.invoked);
      
      clear();
      testUtil.compare(10, base.base);
      testUtil.compare(m, "ChildAspect", new String[]{}, ChildAspect.invoked);
      testUtil.compare(m, "BaseAspect", new String[]{"base"}, BaseAspect.invoked);
      testUtil.compare(m, "BaseParentAspect", new String[]{"base"}, BaseParentAspect.invoked);


      Child_B3 b3 = new Child_B3();
      
      clear();
      b3.b3 = 10;
      m = "Child_B3.b3";
      testUtil.compare(m, "ChildAspect", new String[]{"b3"}, ChildAspect.invoked);
      testUtil.compare(m, "BaseAspect", new String[]{"b3"}, BaseAspect.invoked);
      testUtil.compare(m, "BaseParentAspect", new String[]{"b3"}, BaseParentAspect.invoked);
      
      clear();
      testUtil.compare(10, b3.b3);
      testUtil.compare(m, "ChildAspect", new String[]{"b3"}, ChildAspect.invoked);
      testUtil.compare(m, "BaseAspect", new String[]{"b3"}, BaseAspect.invoked);
      testUtil.compare(m, "BaseParentAspect", new String[]{"b3"}, BaseParentAspect.invoked);
      
      clear();
      b3.b2 = 10;
      m = "Child_B3.b2";
      testUtil.compare(m, "ChildAspect", new String[]{"b2"}, ChildAspect.invoked);
      testUtil.compare(m, "BaseAspect", new String[]{"b2"}, BaseAspect.invoked);
      testUtil.compare(m, "BaseParentAspect", new String[]{"b2"}, BaseParentAspect.invoked);
      
      clear();
      testUtil.compare(10, b3.b2);
      testUtil.compare(m, "ChildAspect", new String[]{"b2"}, ChildAspect.invoked);
      testUtil.compare(m, "BaseAspect", new String[]{"b2"}, BaseAspect.invoked);
      testUtil.compare(m, "BaseParentAspect", new String[]{"b2"}, BaseParentAspect.invoked);
      
      clear();
      b3.b1 = 10;
      m = "Child_B3.b1";
      testUtil.compare(m, "ChildAspect", new String[]{"b1"}, ChildAspect.invoked);
      testUtil.compare(m, "BaseAspect", new String[]{"b1"}, BaseAspect.invoked);
      testUtil.compare(m, "BaseParentAspect", new String[]{"b1"}, BaseParentAspect.invoked);
      
      clear();
      testUtil.compare(10, b3.b1);
      testUtil.compare(m, "ChildAspect", new String[]{"b1"}, ChildAspect.invoked);
      testUtil.compare(m, "BaseAspect", new String[]{"b1"}, BaseAspect.invoked);
      testUtil.compare(m, "BaseParentAspect", new String[]{"b1"}, BaseParentAspect.invoked);
      
      clear();
      b3.base = 10;
      m = "Child_B3.base";
      testUtil.compare(m, "ChildAspect", new String[]{}, ChildAspect.invoked);
      testUtil.compare(m, "BaseAspect", new String[]{"base"}, BaseAspect.invoked);
      testUtil.compare(m, "BaseParentAspect", new String[]{"base"}, BaseParentAspect.invoked);
      
      clear();
      testUtil.compare(10, b3.base);
      testUtil.compare(m, "ChildAspect", new String[]{}, ChildAspect.invoked);
      testUtil.compare(m, "BaseAspect", new String[]{"base"}, BaseAspect.invoked);
      testUtil.compare(m, "BaseParentAspect", new String[]{"base"}, BaseParentAspect.invoked);

      BaseNotBaseWoven bnbw = new BaseNotBaseWoven();
      clear();
      m = "BaseNotBaseWoven.field";
      bnbw.field = 100;
      testUtil.compare(m, "ChildAspect", new String[]{"field"}, ChildAspect.invoked);
      testUtil.compare(m, "BaseAspect", new String[]{}, BaseAspect.invoked);
      testUtil.compare(m, "BaseParentAspect", new String[]{"field"}, BaseParentAspect.invoked);

      clear();
      testUtil.compare(100, bnbw.field);
      testUtil.compare(m, "ChildAspect", new String[]{"field"}, ChildAspect.invoked);
      testUtil.compare(m, "BaseAspect", new String[]{}, BaseAspect.invoked);
      testUtil.compare(m, "BaseParentAspect", new String[]{"field"}, BaseParentAspect.invoked);

      if (testUtil.getErrors() != null)
      {
         throw new RuntimeException(testUtil.getErrors());
      }
   }

   public void testOverriddenInterceptors() throws Exception
   {
      //This makes sure that we get the correct overridden aspect instances loaded
      System.out.println("=============== ScopedChildNoParentDelegationTester - TEST OVERRIDDEN INTERCEPTORS ================");
      TestUtil testUtil = new TestUtil();
      
      Base_Base base = new Base_Base();
      
      clear();
      base.differentScopes();
      String m = "differentScopes";
      testUtil.compare(m, "BaseAspect", new String[]{"differentScopes"}, BaseAspect.invoked);
      testUtil.invoked(BasePerClassInterceptor.class);
      testUtil.invoked(BasePerInstanceInterceptor.class);
      testUtil.invoked(BasePerJoinPointInterceptor.class);
      testUtil.invoked(BasePerClassJoinPointInterceptor.class);
      
      if (testUtil.getErrors() != null)
      {
         throw new RuntimeException(testUtil.getErrors());
      }
   }

   public String readName()
   {
      return "ScopedChildNoParentDelegationTester";
   }

   private void clear()
   {
      BaseAspect.invoked.clear();
      ChildAspect.invoked.clear();
      BaseParentAspect.invoked.clear();
      BasePerClassInterceptor.invoked = false;
      BasePerInstanceInterceptor.invoked = false;
      BasePerJoinPointInterceptor.invoked = false;
      BasePerClassJoinPointInterceptor.invoked = false;
   }

}
