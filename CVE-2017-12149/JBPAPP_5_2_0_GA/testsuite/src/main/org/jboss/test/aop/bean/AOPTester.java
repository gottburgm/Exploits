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
package org.jboss.test.aop.bean;

import org.jboss.aop.Advised;
import org.jboss.aop.InstanceAdvisor;
import org.jboss.aop.metadata.ThreadMetaData;
import org.jboss.logging.Logger;
import org.jboss.system.ServiceMBeanSupport;

import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.ObjectName;

/**
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 81036 $
 */
public class AOPTester
        extends ServiceMBeanSupport
        implements AOPTesterMBean, MBeanRegistration
{
   // Constants ----------------------------------------------------
   // Attributes ---------------------------------------------------
   static Logger log = Logger.getLogger(AOPTester.class);
   MBeanServer m_mbeanServer;

   // Static -------------------------------------------------------

   // Constructors -------------------------------------------------
   public AOPTester()
   {
   }

   // Public -------------------------------------------------------

   // MBeanRegistration implementation -----------------------------------
   public ObjectName preRegister(MBeanServer server, ObjectName name)
           throws Exception
   {
      m_mbeanServer = server;
      return name;
   }

   public void postRegister(Boolean registrationDone)
   {
   }

   public void preDeregister() throws Exception
   {
   }

   public void postDeregister()
   {
   }

   protected void startService()
           throws Exception
   {
      // this is to load up the management console so that we can view everything
      try
      {
         testBasic();
         testInheritance();
         testMetadata();
         testDynamicInterceptors();
         testFieldInterception();
         testMethodInterception();
         testConstructorInterception();
         testExceptions();
         testMixin();
         testCallerPointcut();
      }
      catch (Exception ignored)
      {
      }

   }

   protected void stopService()
   {
   }

   public void testBasic()
   {
      log.info("RUNNING TEST BASIC");
      try
      {
         POJO pojo = new POJO();
         if (!(pojo instanceof org.jboss.aop.Advised)) throw new RuntimeException("POJO is not instanceof Advised");
         SimpleInterceptor.lastIntercepted = null;
         SimpleInterceptor.lastTransAttributeAccessed = null;
         pojo.someMethod();
         if (!"someMethod".equals(SimpleInterceptor.lastIntercepted)) throw new RuntimeException("Failed on interception test");
         if (!"RequiresNew".equals(SimpleInterceptor.lastTransAttributeAccessed)) throw new RuntimeException("Failed on metadata test");

         InstanceOfInterceptor.intercepted = false;
         Implements1 impl1 = new Implements1();
         if (InstanceOfInterceptor.intercepted == false) throw new RuntimeException("failed all(instanceof) constructor interception");
         InstanceOfInterceptor.intercepted = false;
         impl1.foo = 1;
         if (InstanceOfInterceptor.intercepted == false) throw new RuntimeException("failed all(instanceof) field interception");
         InstanceOfInterceptor.intercepted = false;
         impl1.someMethod();
         if (InstanceOfInterceptor.intercepted == false) throw new RuntimeException("failed all(instanceof) method interception");

         InstanceOfInterceptor.intercepted = false;
         Implements2 impl2 = new Implements2();
         if (InstanceOfInterceptor.intercepted == true) throw new RuntimeException("failed method only (instanceof) constructor interception");
         InstanceOfInterceptor.intercepted = false;
         impl2.someMethod();
         if (InstanceOfInterceptor.intercepted == false) throw new RuntimeException("failed method only(instanceof) method interception");
         InstanceOfInterceptor.intercepted = false;

         CFlowedPOJO cflow = new CFlowedPOJO();
         InterceptorCounter.count = 0;
         cflow.method3();
         if (InterceptorCounter.count > 0) throw new RuntimeException("method3 count should be null");
         InterceptorCounter.count = 0;
         cflow.method1();
         if (InterceptorCounter.count != 1) throw new RuntimeException("method1 count should be 1");
         InterceptorCounter.count = 0;
         cflow.recursive(1);
         if (InterceptorCounter.count == 0) throw new RuntimeException("recursive never get intercepted");
         if (InterceptorCounter.count > 1) throw new RuntimeException("recursive too many interceptions");
      }
      catch (Throwable ex)
      {
         log.error("failed", ex);
         ex.printStackTrace();
         throw new RuntimeException(ex.getMessage());
      }
   }

   public void testInheritance()
   {
      log.info("RUNNING TEST INHERITANCE");
      try
      {
         SimpleInterceptor.lastIntercepted = null;
         SimpleInterceptor.lastTransAttributeAccessed = null;
         POJOChild pojo = new POJOChild();
         pojo.someMethod2();
         if (!"someMethod2".equals(SimpleInterceptor.lastIntercepted))
            throw new RuntimeException("Failed on interception test");
         if (!"RequiresNew".equals(SimpleInterceptor.lastTransAttributeAccessed))
            throw new RuntimeException("Failed on metadata test");

         SimpleInterceptor.lastIntercepted = null;
         SimpleInterceptor.lastTransAttributeAccessed = null;
         pojo.someMethod();
         if (!"someMethod".equals(SimpleInterceptor.lastIntercepted))
            throw new RuntimeException("Failed on interception test");
         if (!"RequiresNew".equals(SimpleInterceptor.lastTransAttributeAccessed))
            throw new RuntimeException("Failed on metadata test");

      }
      catch (Throwable ex)
      {
         log.error("failed", ex);
         throw new RuntimeException(ex);
      }
   }

   public void testMetadata()
   {
      log.info("RUNNING TEST METADATA");

      try
      {
         POJOChild pojo = new POJOChild();
         SimpleInterceptor.lastIntercepted = null;
         SimpleInterceptor.lastTransAttributeAccessed = null;
         pojo.someMethod();
         if (!"someMethod".equals(SimpleInterceptor.lastIntercepted))
            throw new RuntimeException("Failed on interception test");
         if (!"RequiresNew".equals(SimpleInterceptor.lastTransAttributeAccessed))
            throw new RuntimeException("Failed on metadata test");

         SimpleInterceptor.lastIntercepted = null;
         SimpleInterceptor.lastTransAttributeAccessed = null;
         pojo.anotherMethod();
         if (!"anotherMethod".equals(SimpleInterceptor.lastIntercepted))
            throw new RuntimeException("Failed on interception test");
         if (!"Required".equals(SimpleInterceptor.lastTransAttributeAccessed))
            throw new RuntimeException("Failed on metadata test");


         SimpleInterceptor.lastIntercepted = null;
         SimpleInterceptor.lastTransAttributeAccessed = null;
         pojo.someMethod2();
         if (!"someMethod2".equals(SimpleInterceptor.lastIntercepted))
            throw new RuntimeException("Failed on interception test");
         if (!"RequiresNew".equals(SimpleInterceptor.lastTransAttributeAccessed))
            throw new RuntimeException("Failed on metadata test");


         SimpleInterceptor.lastIntercepted = null;
         SimpleInterceptor.lastTransAttributeAccessed = null;
         pojo.someMethod3();
         if (!"someMethod3".equals(SimpleInterceptor.lastIntercepted))
            throw new RuntimeException("Failed on interception test");
         if (!"Supports".equals(SimpleInterceptor.lastTransAttributeAccessed))
            throw new RuntimeException("Failed on metadata test");

         SimpleInterceptor.lastIntercepted = null;
         SimpleInterceptor.lastTransAttributeAccessed = null;
         org.jboss.aop.metadata.ThreadMetaData.instance().addMetaData("transaction", "trans-attribute", "Never");
         pojo.someMethod3();
         if (!"someMethod3".equals(SimpleInterceptor.lastIntercepted))
            throw new RuntimeException("Failed on interception test");
         if (!"Never".equals(SimpleInterceptor.lastTransAttributeAccessed))
            throw new RuntimeException("Failed on metadata test");
         org.jboss.aop.metadata.ThreadMetaData.instance().clear();

         SimpleInterceptor.lastIntercepted = null;
         SimpleInterceptor.lastTransAttributeAccessed = null;
         InstanceAdvisor instanceAdvisor = ((Advised) pojo)._getInstanceAdvisor();
         instanceAdvisor.getMetaData().addMetaData("transaction", "trans-attribute", "NotSupported");
         pojo.someMethod3();
         if (!"someMethod3".equals(SimpleInterceptor.lastIntercepted))
            throw new RuntimeException("Failed on interception test");
         if (!"NotSupported".equals(SimpleInterceptor.lastTransAttributeAccessed))
            throw new RuntimeException("Failed on metadata test");
         org.jboss.aop.metadata.ThreadMetaData.instance().clear();

      }
      catch (Throwable ex)
      {
         log.error("failed", ex);
         throw new RuntimeException(ex);
      }

   }


   public void testDynamicInterceptors()
   {
      log.info("RUNNING TEST DYNAMIC INTERCEPTORS");
      try
      {
         POJOChild pojo = new POJOChild();
         SimpleInterceptor.lastIntercepted = null;
         SimpleInterceptor.lastTransAttributeAccessed = null;
         BeforeInterceptor.lastIntercepted = null;
         BeforeInterceptor.lastTransAttributeAccessed = null;
         ((Advised) pojo)._getInstanceAdvisor().insertInterceptor(new BeforeInterceptor());
         pojo.someMethod();
         if (!"someMethod".equals(SimpleInterceptor.lastIntercepted))
            throw new RuntimeException("Failed on interception test");
         if (!"RequiresNew".equals(SimpleInterceptor.lastTransAttributeAccessed))
            throw new RuntimeException("Failed on metadata test");
         if (!"someMethod".equals(BeforeInterceptor.lastIntercepted))
            throw new RuntimeException("Failed on interception test");
         if (!"RequiresNew".equals(BeforeInterceptor.lastTransAttributeAccessed))
            throw new RuntimeException("Failed on metadata test");


         SimpleInterceptor.lastIntercepted = null;
         SimpleInterceptor.lastTransAttributeAccessed = null;
         BeforeInterceptor.lastIntercepted = null;
         BeforeInterceptor.lastTransAttributeAccessed = null;
         AfterInterceptor.lastIntercepted = null;
         AfterInterceptor.lastTransAttributeAccessed = null;
         ((Advised) pojo)._getInstanceAdvisor().appendInterceptor(new AfterInterceptor());
         pojo.someMethod();
         if (!"someMethod".equals(BeforeInterceptor.lastIntercepted))
            throw new RuntimeException("Failed on interception test");
         if (!"RequiresNew".equals(BeforeInterceptor.lastTransAttributeAccessed))
            throw new RuntimeException("Failed on metadata test");
         if (!"someMethod".equals(AfterInterceptor.lastIntercepted))
            throw new RuntimeException("Failed on interception test");
         if (!"RequiresNew".equals(AfterInterceptor.lastTransAttributeAccessed))
            throw new RuntimeException("Failed on metadata test");


      }
      catch (Throwable ex)
      {
         log.error("failed", ex);
         throw new RuntimeException(ex);
      }


   }

   public void testFieldInterception()
   {
      log.info("RUNNING TEST FIELD INTERCEPTION");
      try
      {


         POJO pojo = new POJO();
         SimpleInterceptor.lastFieldIntercepted = null;
         SimpleInterceptor.lastFieldTransAttributeAccessed = null;
         pojo.accessField();

         if (!"privateField".equals(SimpleInterceptor.lastFieldIntercepted)) throw new RuntimeException("Failed on interception test");
         if (!"NotSupported".equals(SimpleInterceptor.lastFieldTransAttributeAccessed)) throw new RuntimeException("Failed on metadata test");


         POJOChild child = new POJOChild();
         SimpleInterceptor.lastFieldIntercepted = null;
         SimpleInterceptor.lastFieldTransAttributeAccessed = null;
         child.accessField();
         if (!"privateField".equals(SimpleInterceptor.lastFieldIntercepted)) throw new RuntimeException("Failed on interception test");
         if (!"NotSupported".equals(SimpleInterceptor.lastFieldTransAttributeAccessed)) throw new RuntimeException("Failed on metadata test");

         SimpleInterceptor.lastFieldIntercepted = null;
         SimpleInterceptor.lastFieldTransAttributeAccessed = null;
         child.accessProtectedField();
         if (!"protectedField".equals(SimpleInterceptor.lastFieldIntercepted)) throw new RuntimeException("Failed on interception test");
         if (!"Supports".equals(SimpleInterceptor.lastFieldTransAttributeAccessed)) throw new RuntimeException("Failed on metadata test");

         POJORef ref = new POJORef();
         SimpleInterceptor.lastFieldIntercepted = null;
         SimpleInterceptor.lastFieldTransAttributeAccessed = null;
         ref.refPOJO();


         if (!"protectedField".equals(SimpleInterceptor.lastFieldIntercepted)) throw new RuntimeException("Failed on interception test");
         if (!"Supports".equals(SimpleInterceptor.lastFieldTransAttributeAccessed)) throw new RuntimeException("Failed on metadata test");

         pojo.accessStaticField();


      }
      catch (Throwable ex)
      {
         log.error("failed", ex);
         throw new RuntimeException(ex);
      }
   }

   public void testMethodInterception()
   {
      System.out.println("RUNNING METHOD INTERCEPTION");
      try
      {
         POJO.staticMethod();
         POJOConstructorTest vanilla;
         vanilla = new POJOConstructorTest();

         vanilla.data = "error";
         vanilla.someMethod();
         if (!vanilla.data.equals("someMethod")) throw new RuntimeException("someMethod() didn't get correct method metadata");

         vanilla.data = "error";
         vanilla.another();
         if (!vanilla.data.equals("another()")) throw new RuntimeException("another() didn't get correct method metadata: " + vanilla.data);

         vanilla.data = "nothing";
         POJOMethodInterceptor.wasHit = false;
         vanilla.another(1);
         if (POJOMethodInterceptor.wasHit) throw new RuntimeException("interceptor should not have been called");
         if (!vanilla.data.equals("nothing")) throw new RuntimeException("another(int) shouldn't get intercepted: " + vanilla.data);

         vanilla.data = "nothing";
         vanilla.another(1, 1);
         if (!vanilla.data.equals("another(int, int)")) throw new RuntimeException("another(int, int) didn't get intercepted: " + vanilla.data);
      }
      catch (Throwable ex)
      {
         ex.printStackTrace();
         throw new RuntimeException(ex.getMessage());
      }
   }

   public void testAspect()
   {
      System.out.println("RUNNING ASPECT TEST");
      try
      {
         POJO.staticMethod();
         POJOAspectTester vanilla;
         vanilla = new POJOAspectTester();
         if (!vanilla.marker.equals("interceptConstructor")) throw new RuntimeException("vanilla constructor didn't get intercepted");

         vanilla.marker = "error";
         vanilla.someMethod();
         if (!vanilla.marker.equals("interceptMethod")) throw new RuntimeException("vanilla.someMethod() didn't get intercepted");

         vanilla.marker = "error";
         vanilla.field = 5;
         if (!vanilla.marker.equals("interceptField")) throw new RuntimeException("vanilla.field  didn't get intercepted");

      }
      catch (Throwable ex)
      {
         ex.printStackTrace();
         throw new RuntimeException(ex);
      }
   }

   public void testConstructorInterception()
   {
      System.out.println("RUNNING CONSTRUCTOR INTERCEPTION");
      try
      {

         POJO pojo = new POJO();
         POJOChild child = new POJOChild();

         POJORef ref = new POJORef();
         ref.constructPOJO();

         POJOWildCardConstructorTest wild;
         wild = new POJOWildCardConstructorTest();
         if (wild == null) throw new RuntimeException("wild was null!");
         if (wild.data.equals("error")) throw new RuntimeException("wild() didn't intercept");
         wild = new POJOWildCardConstructorTest(1);
         if (wild.data.equals("error")) throw new RuntimeException("wild(int) didn't intercept");

         POJOConstructorTest vanilla;
         vanilla = new POJOConstructorTest();
         if (vanilla == null) throw new RuntimeException("vanilla was null!");
         if (vanilla.data.equals("error")) throw new RuntimeException("vanilla() didn't intercept");
         if (!vanilla.data.equals("empty")) throw new RuntimeException("vanilla() didn't get correct constructor metadata");
         vanilla = new POJOConstructorTest(1, 1);
         if (vanilla.data.equals("error")) throw new RuntimeException("vanilla(int, int) didn't intercept");
         if (!vanilla.data.equals("int, int")) throw new RuntimeException("vanilla(int, int) didn't get correct constructor metadata");
         vanilla = new POJOConstructorTest(1);
         if (!vanilla.data.equals("error")) throw new RuntimeException("vanilla(int) did intercept when it shouldn't have");

      }
      catch (Throwable ex)
      {
         ex.printStackTrace();
         throw new RuntimeException(ex.getMessage());
      }
   }

   public void testExceptions()
   {
      log.info("TEST AOP EXCEPTIONS");
      try
      {
         NoInterceptorsPOJO pojo = new NoInterceptorsPOJO();

         pojo.throwException();

      }
      catch (SomeException ignored)
      {
         log.info("caught SomeException successfully");
      }
      try
      {
         POJO pojo = new POJO();

         pojo.throwException();
      }
      catch (SomeException ignored)
      {
         log.info("caught SomeException successfully");
      }
   }

   public void testMixin()
   {
      try
      {
         log.info("TEST MIXIN");
         POJO pojo = new POJO();
         log.info("TEST Introduction");
         Introduction intro = (Introduction) pojo;
         log.info(intro.helloWorld("world"));
         log.info("TEST Introduction2");
         Introduction2 intro2 = (Introduction2) pojo;
         log.info(intro2.goodbye("world"));
         log.info("TEST InterfaceMixin");
         InterfaceMixin mixin = (InterfaceMixin) pojo;
         log.info(mixin.whazup());

         POJOChild child = new POJOChild();
         log.info("TEST child Introduction");
         intro = (Introduction) child;
         log.info(intro.helloWorld("world"));
         log.info("TEST child Introduction2");
         intro2 = (Introduction2) child;
         log.info(intro2.goodbye("world"));
         log.info("TEST child AnotherIntroduction");
         SubclassIntroduction sub = (SubclassIntroduction) child;
         log.info(sub.subclassHelloWorld("world"));
         log.info("TEST metadata introduction pointcut");
         NoInterceptorsPOJO nopojo = new NoInterceptorsPOJO();
         intro = (Introduction) nopojo;

      }
      catch (Exception ex)
      {
         ex.printStackTrace();
         throw new RuntimeException(ex);
      }
   }

   public void testCallerPointcut()
   {
      log.info("TEST CALLER");
      CallingPOJO callingPOJO = new CallingPOJO();
      callingPOJO.callSomeMethod();
      callingPOJO.nocallSomeMethod();
      callingPOJO.callUnadvised();
   }

   public void testIntroducedAnnotation()
   {
      OverriddenAnnotationInterceptor.intercepted = false;
      OverriddenAnnotationInterceptor.overriddenAnnotation = null;
      
      POJO pojo = new POJO();
      pojo.overriddenAnnotatedMethod();
      if (!OverriddenAnnotationInterceptor.intercepted) throw new RuntimeException("!IntroducedAnnotationInterceptor.intercepted");
      if (OverriddenAnnotationInterceptor.overriddenAnnotation == null) throw new RuntimeException("IntroducedAnnotationInterceptor.overriddenAnnotation == null");
   }
   // Inner classes -------------------------------------------------
}

