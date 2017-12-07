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

package org.jboss.test.naming.restart;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.test.JBossTestCase;
import org.jnp.interfaces.NamingContext;

/**
 * Base class for tests of client's ability to deal with JNDI restarting.
 * 
 * @author <a href="brian.stansberry@jboss.com">Brian Stansberry</a>
 * @version $Revision: 106766 $
 */
public abstract class NamingRestartTestBase extends JBossTestCase
{
   public NamingRestartTestBase(String name)
   {
      super(name);
   }
   
   protected void setUp() throws Exception
   {
      super.setUp();
      
      if (!isDeployed())
         deploy("naming-restart.sar");
      setDeployed(true);
   }   
   
   protected void tearDown() throws Exception
   {
      if (isDeployed())
      {
         undeploy("naming-restart.sar");
         setDeployed(false);
      }
      
      super.tearDown();
   }
   
   protected void lookupTest(Hashtable env) throws Exception
   {
      Context ctx1 = createInitialContext(env);
      assertEquals(ObjectBinder.VALUE, ctx1.lookup(ObjectBinder.NAME));
      
      // HOLD ONTO REF to ctx1 so the ref to it does not get gc'ed from
      // static map in org.jnp.interfaces.NamingContext.
      
      // Redeploy the naming service
      redeployNaming();
      
      Context ctx2 = createInitialContext(env);
      NamingException ne = null;
      try
      {
         assertEquals(ObjectBinder.VALUE, ctx2.lookup(ObjectBinder.NAME));
      }
      catch (NamingException e)
      {
         // Cache the exception to fail the test later, after
         // we check that we can recover
         log.error("Caught NamingException", e);
         ne = e;
      }
      
      // We recover from failure
      if (ne != null)
      {
         try
         {
            assertEquals(ObjectBinder.VALUE, ctx2.lookup(ObjectBinder.NAME));
         }
         catch (Exception e)
         {
            log.error("Failed to reacquire server");
         }
         // Now fail due to the earlier failure
         fail(ne.getMessage());         
      }
      
      // Confirm the original context is still good
      assertEquals(ObjectBinder.VALUE, ctx1.lookup(ObjectBinder.NAME));
   }

   protected Context createInitialContext(Hashtable env) throws NamingException
   {
      Context ctx1 = new InitialContext(env);
      if (ctx1.getEnvironment().get(NamingContext.JNP_DISCOVERY_GROUP) != null)
      {
         ctx1.removeFromEnvironment(Context.PROVIDER_URL);
      }
      return ctx1;
   }
   protected void redeployNaming() throws Exception
   {
      setDeployed(false);
      redeploy("naming-restart.sar");
      setDeployed(true);
   }
   
   protected abstract boolean isDeployed();
   
   protected abstract void setDeployed(boolean deployed);

}
