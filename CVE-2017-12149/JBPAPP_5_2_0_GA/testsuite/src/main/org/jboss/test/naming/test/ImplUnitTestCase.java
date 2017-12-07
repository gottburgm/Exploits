/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.test.naming.test;


import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;
import java.util.Hashtable;
import java.io.Serializable;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.Name;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.naming.spi.StateFactory;
import javax.naming.spi.ObjectFactory;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.jboss.logging.Logger;
import org.jboss.naming.ENCFactory;

/** Simple unit tests for the jndi implementation.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 77587 $
 */
public class ImplUnitTestCase extends TestCase
{
   static final Logger log = Logger.getLogger(ImplUnitTestCase.class);

   
   /**
    * Constructor for the SimpleUnitTestCase object
    *
    * @param name  Test name
    */
   public ImplUnitTestCase(String name)
   {
      super(name);
   }

   /**
    * Tests that the second time you create a subcontext you get an exception.
    *
    * @exception Exception  Description of Exception
    */
   public void testCreateSubcontext() throws Exception
   {
      log.debug("+++ testCreateSubcontext");
      InitialContext ctx = getInitialContext();
      ctx.createSubcontext("foo");
      try
      {
         ctx.createSubcontext("foo");
         fail("Second createSubcontext(foo) did NOT fail");
      }
      catch (NameAlreadyBoundException e)
      {
         log.debug("Second createSubcontext(foo) failed as expected");
      }
      ctx.createSubcontext("foo/bar");
      ctx.unbind("foo/bar");
      ctx.unbind("foo");
   }

   /** Lookup a name to test basic connectivity and lookup of a known name
    *
    * @throws Exception
    */
   public void testLookup() throws Exception
   {
      log.debug("+++ testLookup");
      InitialContext ctx = getInitialContext();
      Object obj = ctx.lookup("");
      log.debug("lookup('') = "+obj);
   }

   /**
    * Validate that bind("x", null) works
    *
    */
   public void testBindNull()
      throws Exception
   {
      log.debug("+++ testBindNull");
      InitialContext ctx = getInitialContext();
      ctx.bind("testBindNull", null);
      Object x = ctx.lookup("testBindNull");
      assertNull("testBindNull", x);
      NamingEnumeration<NameClassPair> ncps = ctx.list("");
      NameClassPair testBindNullNCP = null;
      while( ncps.hasMore() )
      {
         NameClassPair ncp = ncps.next();
         if( ncp.getName().equals("testBindNull") )
         {
            testBindNullNCP = ncp;
            break;
         }
      }
      assertTrue("testBindNull NameClassPair != null", testBindNullNCP != null);
   }

   /**
    * Validate that rebind("x", null) works
    *
    */
   public void testRebindNull()
      throws Exception
   {
      log.debug("+++ testRebindNull");
      InitialContext ctx = getInitialContext();
      ctx.bind("testRebindNull", null);
      Object x = ctx.lookup("testRebindNull");
      assertNull("testRebindNull", x);
      NamingEnumeration<NameClassPair> ncps = ctx.list("");
      NameClassPair testBindNullNCP = null;
      while( ncps.hasMore() )
      {
         NameClassPair ncp = ncps.next();
         if( ncp.getName().equals("testRebindNull") )
         {
            testBindNullNCP = ncp;
            break;
         }
      }
      assertTrue("testRebindNull NameClassPair != null", testBindNullNCP != null);
   }

   public void testEncPerf() throws Exception
   {
      int count = Integer.getInteger("jbosstest.threadcount", 10).intValue();
      int iterations = Integer.getInteger("jbosstest.iterationcount", 1000).intValue();
      log.info("Creating "+count+"threads doing "+iterations+" iterations");
      InitialContext ctx = getInitialContext();
      URL[] empty = {};
      Thread[] testThreads = new Thread[count];
      for(int t = 0; t < count; t ++)
      {
         ClassLoader encLoader = URLClassLoader.newInstance(empty);
         Thread.currentThread().setContextClassLoader(encLoader);
         Runnable test = new ENCTester(ctx, iterations);
         Thread thr = new Thread(test, "Tester#"+t);
         thr.setContextClassLoader(encLoader);
         thr.start();
         testThreads[t] = thr;
      }

      for(int t = 0; t < count; t ++)
      {
         Thread thr = testThreads[t];
         thr.join();
      }
   }

   /**
    * 
    * @throws NamingException
    */ 
   public void testFactorySupport() throws NamingException
   {
      log.info("+++ testFactorySupport");
      NotSerializableObject nso = new NotSerializableObject( "nsc" );
      Context ctx = getInitialContext();

      try 
      {
         ctx.bind("test", nso);
         fail();
      }
      catch( NamingException ex )
      {
         log.debug("bind failed as expected", ex);
      }

      Properties env = new Properties();
      env.setProperty(Context.STATE_FACTORIES, TestFactory.class.getName());
      env.setProperty(Context.OBJECT_FACTORIES, TestFactory.class.getName());
      ctx = new InitialContext(env);

      log.debug("Retest with TestFactory enabled");
      ctx.bind("test", nso);

      Object boundObject = ctx.lookup( "test" );
      assertNotNull( boundObject );
      // make sure it's of type NotSerializableObject
      NotSerializableObject nso2 = (NotSerializableObject) boundObject;
      assertEquals( nso.getId(), nso2.getId() );
   }

   public void testCloneableReference()
      throws Exception
   {
      log.info("+++ testFactorySupport");
      NotSerializableObject nso = new NotSerializableObject( "nsc" );
      CloneObjectFactory.setInstance(nso);
      Context ctx = getInitialContext();
      RefAddr refAddr = new StringRefAddr("NotSerializableObject", "Clone");
      Reference ref = new Reference(NotSerializableObject.class.getName(), refAddr, CloneObjectFactory.class.getName(), null);
      ctx.bind("NotSerializableObject", ref);

      // Validate each lookup produces a unique but equal instance
      NotSerializableObject nso1 = (NotSerializableObject) ctx.lookup("NotSerializableObject");
      NotSerializableObject nso2 = (NotSerializableObject) ctx.lookup("NotSerializableObject");
      assertTrue(nso != nso1);
      assertTrue(nso != nso2);
      assertTrue(nso1 != nso2);
   }

   static InitialContext getInitialContext() throws NamingException
   {
      InitialContext ctx = new InitialContext();
      return ctx;
   }

   private static class ENCTester implements Runnable
   {
      Context enc;
      int iterations;

      ENCTester(InitialContext ctx, int iterations) throws Exception
      {
         log.info("CL: "+Thread.currentThread().getContextClassLoader());
         this.iterations = iterations;
         enc = (Context) ctx.lookup("java:comp");
         enc = enc.createSubcontext("env");
         enc.bind("int", new Integer(1));
         enc.bind("double", new Double(1.234));
         enc.bind("string", "str");
         enc.bind("url", new URL("http://www.jboss.org"));
      }

      public void run()
      {
         try
         {
            InitialContext ctx =  new InitialContext();
            for(int i = 0; i < iterations; i ++)
            {
               Integer i1 = (Integer) enc.lookup("int");
               log.debug("int: "+i1);
               i1 = (Integer) ctx.lookup("java:comp/env/int");
               log.debug("java:comp/env/int: "+i1);
               Double d = (Double) enc.lookup("double");
               log.debug("double: "+d);
               d = (Double) ctx.lookup("java:comp/env/double");
               log.debug("java:comp/env/double: "+d);
               String s = (String) enc.lookup("string");
               log.debug("string: "+s);
               s = (String) ctx.lookup("java:comp/env/string");
               log.debug("java:comp/env/string: "+s);
               URL u = (URL) enc.lookup("url");
               log.debug("url: "+u);
               u = (URL) ctx.lookup("java:comp/env/url");
               log.debug("java:comp/env/url: "+u);
            }
         }
         catch(Exception e)
         {
            e.printStackTrace();
         }
      }
   }

   private static class NotSerializableObject
      implements Cloneable
   {
      protected String id;
      
      public NotSerializableObject() {}
      
      public NotSerializableObject( String id ) 
      {
         this.id = id;
      }
      
      public String getId() 
      {
         return id;
      }
      
      public String toString()
      {
         return "NotSerializableObject<" + getId() + ">";
      }

      @Override
      public Object clone() throws CloneNotSupportedException
      {
         return super.clone();
      }

      @Override
      public boolean equals(Object obj)
      {
         boolean equals = false;
         if(obj instanceof NotSerializableObject)
         {
            NotSerializableObject nso = (NotSerializableObject) obj;
            equals = id.equals(nso.id);
         }
         return equals;
      }

      @Override
      public int hashCode()
      {
         return id.hashCode();
      }
   }
   
   private static class SerializableObject extends NotSerializableObject
      implements Serializable
   {
      private static long serialVersionUID = 1;

      public SerializableObject () {}
      
      public SerializableObject (String id)
      {
         super( id );
      }
      
      public String toString()
      {
         return "SerializableObject<" + getId() + ">";
      }
      
      private void writeObject(ObjectOutputStream out)
         throws IOException
      {
         out.writeObject(getId());
      }
      private void readObject(ObjectInputStream in)
         throws IOException, ClassNotFoundException
      {
         id = (String) in.readObject();
      }
      
   }
   
   public static class CloneObjectFactory implements ObjectFactory
   {
      private static Object instance;
      private static Method clone;
      public static void setInstance(Object instance)
         throws Exception
      {
         CloneObjectFactory.instance = instance;
         Class<?> empty[] = {};
         if(instance != null)
            clone = instance.getClass().getDeclaredMethod("clone", empty);
      }

      public Object getObjectInstance (Object obj, Name name, Context nameCtx,
            Hashtable env) throws Exception
      {
         log.debug("CloneObjectFactory.getObjectInstance, obj:" + obj + ", name: " + name
            + ", nameCtx: " + nameCtx +", env: "+env);
         return clone.invoke(instance, null);
      }
   }

   public static class TestFactory implements StateFactory, ObjectFactory
   {
      public Object getStateToBind (Object obj, Name name, Context nameCtx,
         Hashtable environment) throws NamingException
      {
         if( obj instanceof NotSerializableObject )
         {
            String id = ((NotSerializableObject) obj).getId();
            return new SerializableObject( id );
         }
         
         return null;
      }

      public Object getObjectInstance (Object obj, Name name, Context nameCtx,
         Hashtable env) throws Exception
      {
         log.debug("TestFactory.getObjectInstance, obj:" + obj + ", name: " + name
            + ", nameCtx: " + nameCtx +", env: "+env);
         if( obj instanceof SerializableObject )
         {
            String id = ((SerializableObject) obj).getId();
            return new NotSerializableObject( id );
         }
         
         return null;
      }
   }

   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new TestSuite(ImplUnitTestCase.class));

      // Create an initializer for the test suite
      NamingServerSetup wrapper = new NamingServerSetup(suite);
      return wrapper; 
   }

   /** Used to run the testcase from the command line
    *
    * @param args  The command line arguments
    */
   public static void main(String[] args)
   {
      TestRunner.run(ImplUnitTestCase.suite());
   }
}
