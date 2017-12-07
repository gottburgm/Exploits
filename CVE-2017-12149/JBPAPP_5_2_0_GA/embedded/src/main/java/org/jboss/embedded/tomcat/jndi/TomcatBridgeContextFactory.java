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
import javax.naming.NamingException;

import org.apache.naming.java.javaURLContextFactory;
import org.jboss.embedded.jndi.BridgeContext;
import org.jboss.embedded.jndi.DelegatingContextFactory;

/**
 * comment
 *
 * @author <a href="bill@jboss.com">Bill Burke</a>
 * @version $Revision: 85945 $
 */
public class TomcatBridgeContextFactory extends DelegatingContextFactory
{
   private javaURLContextFactory apache = new javaURLContextFactory();

   /**
    * Get a new (writable) initial context.
    */
   @Override
   public Context getInitialContext(Hashtable environment) throws NamingException
   {
      Context jbossContext = super.getInitialContext(environment);
      Context apacheContext = apache.getInitialContext(environment);
      return BridgeContext.createBridge(jbossContext, apacheContext);
   }
}
