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
package org.jboss.test.naming.test;

import java.util.Properties;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import junit.framework.Test;
import org.jboss.test.JBossTestCase;

/** Simple unit tests for the jndi service using the pooled invoker as the
 * transport detached invoker.
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public class PooledInvokerUnitTestCase extends JBossTestCase
{
   /**
    * Constructor for the SimpleUnitTestCase object
    *
    * @param name  Test name
    */
   public PooledInvokerUnitTestCase(String name)
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
      getLog().debug("+++ testCreateSubcontext");
      Properties env = new Properties();
      env.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
      env.setProperty(Context.PROVIDER_URL, "jnp://localhost:10999/");
      env.setProperty("jnp.disableDiscovery", "true");
      InitialContext ctx = new InitialContext(env);
      ctx.createSubcontext("foo");
      try
      {
         ctx.createSubcontext("foo");
         fail("Second createSubcontext(foo) did NOT fail");
      }
      catch (NameAlreadyBoundException e)
      {
         getLog().debug("Second createSubcontext(foo) failed as expected");
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
      getLog().debug("+++ testLookup");
      Properties env = new Properties();
      env.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
      env.setProperty(Context.PROVIDER_URL, "jnp://localhost:10999/");
      env.setProperty("jnp.disableDiscovery", "true");
      InitialContext ctx = new InitialContext(env);
      Object obj = ctx.lookup("");
      getLog().debug("lookup('') = "+obj);
   }

   public void testListing() throws Exception
   {
      log.debug("+++ testListing");
      InitialContext ctx = getInitialContext();
      NamingEnumeration names = ctx.list("");
      int count = 0;
      while( names.hasMore() )
      {
         NameClassPair ncp = (NameClassPair) names.next();
         log.info(ncp);
         count ++;
      }
      log.info("list count = "+count);
      assertTrue("list count > 0 ", count > 0);
   }

   /** Lookup a name to test basic connectivity and lookup of a known name
    *
    * @throws Exception
    */
   public void testLookupFailures() throws Exception
   {
      log.debug("+++ testLookupFailures");
      // Look a name that does not exist
      Properties env = new Properties();
      env.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
      env.setProperty(Context.PROVIDER_URL, "jnp://localhost:10999/");
      env.setProperty("jnp.disableDiscovery", "true");
      InitialContext ctx = new InitialContext(env);
      try
      {
         Object obj = ctx.lookup("__bad_name__");
         fail("lookup(__bad_name__) should have thrown an exception, obj="+obj);
      }
      catch(NameNotFoundException e)
      {
         log.debug("lookup(__bad_name__) failed as expected", e);
      }

      // Do a lookup on an server port that does not exist
      env.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
      env.setProperty(Context.PROVIDER_URL, "jnp://localhost:65535/");
      env.setProperty("jnp.disableDiscovery", "true");
      log.debug("Creating InitialContext with env="+env);
      try
      {
         ctx = new InitialContext(env);
         Object obj = ctx.lookup("");
         fail("lookup('') should have thrown an exception, obj="+obj);
      }
      catch(NamingException e)
      {
         log.debug("lookup('') failed as expected", e);
      }
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(PooledInvokerUnitTestCase.class, "naming-pooled.sar");
   }

}
