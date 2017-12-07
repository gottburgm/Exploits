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
package org.jboss.embedded.tomcat.jndi;

import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.StringRefAddr;

import org.apache.naming.java.javaURLContextFactory;

/**
 * We look to see if a threadlocal tag is set, if so, just use JBoss java:comp resolving.
 * If it is not set, then obtain a reference to "java:" and lookup "comp"
 *
 * @author <a href="bill@jboss.com">Bill Burke</a>
 * @version $Revision: 85945 $
 */
public class ENCFactory extends org.jboss.naming.ENCFactory
{
   private static boolean initialized;

   private javaURLContextFactory apache = new javaURLContextFactory();

   /**
    * Convience function.  Rebinds java:comp to this class just in case an existing
    * jboss configuration already bound java:comp.  This is so we don't have to have
    * multiple copies of jboss configuration files.
    *
    * @param jbossContext
    * @throws NamingException
    */
   public static synchronized void rebindComp(Context jbossContext) throws NamingException
   {
      if (initialized)
         return;

      initialized = true;
      RefAddr refAddr = new StringRefAddr("nns", "ENC");
      Reference envRef = new Reference("javax.naming.Context", refAddr, ENCFactory.class.getName(), null);
      Context ctx = (Context)jbossContext.lookup("java:");
      ctx.rebind("comp", envRef);
   }

   @Override
   public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable environment) throws Exception
   {
      if (getCurrentId() != null)
      {
         return super.getObjectInstance(obj, name, nameCtx, environment);
      }
      return apache.getInitialContext(environment).lookup("java:comp");
   }
}
