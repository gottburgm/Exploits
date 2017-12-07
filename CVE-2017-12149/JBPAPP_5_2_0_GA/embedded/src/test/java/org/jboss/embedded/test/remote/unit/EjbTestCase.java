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
package org.jboss.embedded.test.remote.unit;

import junit.framework.TestCase;
import org.jboss.embedded.test.remote.Test;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.Hashtable;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 85945 $
 */
public class EjbTestCase extends TestCase
{
   public EjbTestCase()
   {
      super("BootstrapTestCase");
   }

   public void testRemoteEjb() throws Exception
   {
      Hashtable env = new Hashtable();
      env.put(Context.INITIAL_CONTEXT_FACTORY, org.jboss.naming.JBossRemotingContextFactory.class.getName());
      env.put(Context.PROVIDER_URL, "socket://172.16.83.75:3873");
      InitialContext ctx = new InitialContext(env);
      Test test = (Test)ctx.lookup("TestBean/remote");
      assertEquals(test.echo("hello world"), "hello world");
   }

}
