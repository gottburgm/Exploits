/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.test.naming.test;

import java.util.Properties;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import junit.framework.Test;

import org.jboss.test.naming.restart.NamingRestartTestBase;
import org.jboss.test.naming.restart.ObjectBinder;
import org.jnp.interfaces.MarshalledValuePair;
import org.jnp.interfaces.NamingContext;

/**
 * A NamingRestartUnitTestCase.
 * 
 * @author <a href="brian.stansberry@jboss.com">Brian Stansberry</a>
 * @version $Revision: 106766 $
 */
public class NamingRestartUnitTestCase extends NamingRestartTestBase
{
   private static final String NAMING_PORT = "19099";
   private static final String HA_NAMING_PORT = "19100";
 
   private static final String BIND_NAME = "BindName";
   private static final String BIND_VALUE = "BindValue";
   
   private static final String SUBCONTEXT_NAME = "RemoteSubcontext";
   
   private static boolean deployed = false;
   
   public NamingRestartUnitTestCase(String name)
   {
      super(name);
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(NamingRestartUnitTestCase.class, null);
   }
   
   @Override
   protected boolean isDeployed()
   {
      return deployed;
   }

   @Override
   protected void setDeployed(boolean deployed)
   {
      NamingRestartUnitTestCase.deployed = deployed;
   }

   public void testBind() throws Exception
   {
      log.info("Running testBind()");
      
      Properties env = createNamingEnvironment(NAMING_PORT);
      
      Context ctx1 = new InitialContext(env);
      assertEquals(ObjectBinder.VALUE, ctx1.lookup(ObjectBinder.NAME));
      
      // HOLD ONTO REF to ctx1 so the ref to it does not get gc'ed from
      // static map in org.jnp.interfaces.NamingContext.
      
      // Redeploy the naming service
      redeployNaming();
      
      Context ctx2 = new InitialContext(env);
      try
      {
         ctx2.bind(BIND_NAME, BIND_VALUE);
      }
      catch (NamingException e)
      {
         log.error("Caught NamingException", e);
         fail(e.getMessage());
      }
      
      assertEquals(BIND_VALUE, ctx2.lookup(BIND_NAME));
      
      // Confirm the original context is still good
      assertEquals(ObjectBinder.VALUE, ctx1.lookup(ObjectBinder.NAME));
   }
   
   public void testCreateSubcontext() throws Exception
   {
      log.info("Running testCreateSubcontext()");
      
      Properties env = createNamingEnvironment(NAMING_PORT);
      
      Context ctx1 = new InitialContext(env);
      assertEquals(ObjectBinder.VALUE, ctx1.lookup(ObjectBinder.NAME));
      
      // HOLD ONTO REF to ctx1 so the ref to it does not get gc'ed from
      // static map in org.jnp.interfaces.NamingContext.
      
      // Redeploy the naming service
      redeployNaming();
      
      Context ctx2 = new InitialContext(env);
      try
      {
         Context sub = ctx2.createSubcontext(SUBCONTEXT_NAME);
         sub.bind(BIND_NAME, BIND_VALUE);
         assertEquals("Proper bind to " + SUBCONTEXT_NAME, BIND_VALUE, sub.lookup(BIND_NAME));
      }
      catch (NamingException e)
      {
         log.error("Caught NamingException", e);
         fail(e.getMessage());
      }
      
      // Confirm the original context is still good
      assertEquals(ObjectBinder.VALUE, ctx1.lookup(ObjectBinder.NAME));
   }
   
   public void testLookupAfterHANamingRestart() throws Exception
   {
      log.info("Running testLookupAfterHANamingRestart");
      
      lookupTest(createNamingEnvironment(HA_NAMING_PORT));
   }

   public void testLookupAfterNamingRestart() throws Exception
   {
      log.info("Running testLookupAfterNamingRestart");
      
      lookupTest(createNamingEnvironment(NAMING_PORT));
   }
   
   public void testList() throws Exception
   {
      log.info("Running testList()");
      
      Properties env = createNamingEnvironment(NAMING_PORT);
      
      Context ctx1 = new InitialContext(env);
      assertEquals(ObjectBinder.VALUE, ctx1.lookup(ObjectBinder.NAME));
      
      // HOLD ONTO REF to ctx1 so the ref to it does not get gc'ed from
      // static map in org.jnp.interfaces.NamingContext.
      
      // Redeploy the naming service
      redeployNaming();
      
      Context ctx2 = new InitialContext(env);
      try
      {
         NamingEnumeration list = ctx2.list(ObjectBinder.SUBCONTEXT_NAME);
         assertNotNull("NamingEnumeration returned", list);
         assertTrue("NamingEnumeration has entry", list.hasMoreElements());
         NameClassPair pair = (NameClassPair) list.next();
         assertEquals(ObjectBinder.NAME, pair.getName());
      }
      catch (NamingException e)
      {
         log.error("Caught NamingException", e);
         fail(e.getMessage());
      }
      
      // Confirm the original context is still good
      assertEquals(ObjectBinder.VALUE, ctx1.lookup(ObjectBinder.NAME));
   }
   
   public void testListBindings() throws Exception
   {
      log.info("Running testListBindings()");
      
      Properties env = createNamingEnvironment(NAMING_PORT);
      
      Context ctx1 = new InitialContext(env);
      assertEquals(ObjectBinder.VALUE, ctx1.lookup(ObjectBinder.NAME));
      
      // HOLD ONTO REF to ctx1 so the ref to it does not get gc'ed from
      // static map in org.jnp.interfaces.NamingContext.
      
      // Redeploy the naming service
      redeployNaming();
      
      Context ctx2 = new InitialContext(env);
      try
      {
         NamingEnumeration list = ctx2.listBindings(ObjectBinder.SUBCONTEXT_NAME);
         assertNotNull("NamingEnumeration returned", list);
         assertTrue("NamingEnumeration has entry", list.hasMoreElements());
         Binding binding = (Binding) list.next();
         assertEquals(ObjectBinder.NAME, binding.getName());
      }
      catch (NamingException e)
      {
         log.error("Caught NamingException", e);
         fail(e.getMessage());
      }
      
      // Confirm the original context is still good
      assertEquals(ObjectBinder.VALUE, ctx1.lookup(ObjectBinder.NAME));
   }
   
   public void testLookupLink() throws Exception
   {
      log.info("Running testLookupLink()");
      
      Properties env = createNamingEnvironment(NAMING_PORT);
      
      Context ctx1 = new InitialContext(env);
      assertEquals(ObjectBinder.VALUE, ctx1.lookup(ObjectBinder.NAME));
      
      // HOLD ONTO REF to ctx1 so the ref to it does not get gc'ed from
      // static map in org.jnp.interfaces.NamingContext.
      
      // Redeploy the naming service
      redeployNaming();
      
      Context ctx2 = new InitialContext(env);
      try
      {
         Object obj = ctx2.lookupLink(ObjectBinder.NAME);
         if (obj instanceof MarshalledValuePair)
            obj = ((MarshalledValuePair) obj).get();
         assertEquals(ObjectBinder.VALUE, obj);
      }
      catch (NamingException e)
      {         
         log.error("Caught NamingException", e);
         fail(e.getMessage());
      }
      
      // Confirm the original context is still good
      assertEquals(ObjectBinder.VALUE, ctx1.lookup(ObjectBinder.NAME));
   }
   
   public void testRebind() throws Exception
   {
      log.info("Running testRebind()");
      
      Properties env = createNamingEnvironment(NAMING_PORT);
      
      Context ctx1 = new InitialContext(env);
      assertEquals(ObjectBinder.VALUE, ctx1.lookup(ObjectBinder.NAME));
      
      // HOLD ONTO REF to ctx1 so the ref to it does not get gc'ed from
      // static map in org.jnp.interfaces.NamingContext.
      
      // Redeploy the naming service
      redeployNaming();
      
      Context ctx2 = new InitialContext(env);
      try
      {
         ctx2.rebind(ObjectBinder.NAME, ObjectBinder.VALUE);
      }
      catch (NamingException e)
      {
         log.error("Caught NamingException", e);
         fail(e.getMessage());
      }
      
      // Confirm the original context is still good
      assertEquals(ObjectBinder.VALUE, ctx1.lookup(ObjectBinder.NAME));
   }
   
   public void testUnbind() throws Exception
   {
      log.info("Running testUnbind()");
      
      Properties env = createNamingEnvironment(NAMING_PORT);
      
      Context ctx1 = new InitialContext(env);
      assertEquals(ObjectBinder.VALUE, ctx1.lookup(ObjectBinder.NAME));
      
      // HOLD ONTO REF to ctx1 so the ref to it does not get gc'ed from
      // static map in org.jnp.interfaces.NamingContext.
      
      // Redeploy the naming service
      redeployNaming();
      
      Context ctx2 = new InitialContext(env);
      try
      {
         ctx2.unbind(ObjectBinder.NAME);
      }
      catch (NamingException e)
      {
         log.error("Caught NamingException", e);
         fail(e.getMessage());
      }
      
      // Confirm the original context is still good
      ctx1.bind(BIND_NAME, BIND_VALUE);
   }
   
   public void testBadBindingHALookup() throws Exception
   {
      log.info("Running testBadBindingHALookup");
      
      badBindingLookupTest(HA_NAMING_PORT);
   }

   public void testBadBindingLookup() throws Exception
   {
      log.info("Running testBadBindingLookup");
      
      badBindingLookupTest(NAMING_PORT);
   }
   
   /**
    * Tests a lookup of an object that deliberately throws
    * java.rmi.NoSuchObjectException when deserialized. Used 
    * to check that this doesn't confuse the NamingContext.
    * 
    * @param port
    * @throws Exception
    */
   private void badBindingLookupTest(String port) throws Exception
   {
      Properties env = createNamingEnvironment(port);
      
      Context ctx = new InitialContext(env);
      try
      {
         ctx.lookup(ObjectBinder.BAD_BINDING);
         fail("Did not fail in lookup of " + ObjectBinder.BAD_BINDING);
      }
      catch (NamingException good)
      {
         log.debug("Caught NamingException as expected: " + 
                   good.getLocalizedMessage() + " -- cause: " + 
                   good.getCause());
      }
      
      // We recover from failure
      assertEquals(ObjectBinder.VALUE, ctx.lookup(ObjectBinder.NAME));
   }

   private Properties createNamingEnvironment(String port)
   {
      String namingURL = getServerHost() + ":" + port;
      Properties env = new Properties();
      env.setProperty(Context.PROVIDER_URL, namingURL);
      env.setProperty(NamingContext.JNP_DISABLE_DISCOVERY, "true");
      return env;
   }

}
