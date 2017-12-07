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

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.InitialContextFactoryBuilder;
import javax.naming.spi.NamingManager;

import junit.extensions.TestSetup;
import junit.framework.Test;

import org.jboss.naming.ENCFactory;
import org.jnp.interfaces.NamingContext;
import org.jnp.server.Main;
import org.jnp.server.NamingBeanImpl;

/** 
 * Create a naming server instance in setUp and destroy it in tearDown.
 * @author Scott.Stark@jboss.org
 * @author Dimitris.Andreadis@jboss.org
 * @version $Revision: 60383 $
 */
public class NamingServerSetup extends TestSetup
   implements InitialContextFactoryBuilder
{
   private Main namingServer;
   private NamingBeanImpl namingBean;
   
   public NamingServerSetup(Test test)
   {
      super(test);
   }

   public InitialContextFactory createInitialContextFactory(Hashtable environment)
      throws NamingException
   {
      return new InVMInitialContextFactory();
   }

   protected void setUp() throws Exception
   {
      super.setUp();
      namingBean = new NamingBeanImpl();
      namingServer = new Main();
      namingServer.setPort(10099);
      namingServer.setNamingInfo(namingBean);
      namingBean.start();
      namingServer.start();

      NamingManager.setInitialContextFactoryBuilder(this);
      /* Bind an ObjectFactory to "java:comp" so that "java:comp/env" lookups
         produce a unique context for each thread contexxt ClassLoader that
         performs the lookup.
      */
      InitialContext iniCtx = new InitialContext();
      ClassLoader topLoader = Thread.currentThread().getContextClassLoader();
      ENCFactory.setTopClassLoader(topLoader);
      RefAddr refAddr = new StringRefAddr("nns", "ENC");
      Reference envRef = new Reference("javax.naming.Context", refAddr, ENCFactory.class.getName(), null);
      Context ctx = (Context)iniCtx.lookup("java:");
      ctx.rebind("comp", envRef);
   }

   protected void tearDown() throws Exception
   {
      namingServer.stop();
      namingBean.stop();
      super.tearDown();
   }

   static class InVMInitialContextFactory implements InitialContextFactory
   {
      public Context getInitialContext(Hashtable env)
         throws NamingException
      {
         Hashtable env2 = (Hashtable) env.clone();
         env2.remove(Context.PROVIDER_URL);
         return new NamingContext(env2, null, null);
      }
   }
}
