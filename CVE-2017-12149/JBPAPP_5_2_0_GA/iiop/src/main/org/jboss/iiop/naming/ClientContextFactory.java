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
package org.jboss.iiop.naming;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import org.omg.CORBA.ORB;

/**
 * <p>
 * An implementation of {@code org.jnp.interfaces.NamingContextFactory} that is meant to be used by
 * application clients. This class sets the orb reference in the context environment, creating a
 * new ORB if needed.
 * </p>
 * 
 * @author <a href="sguilhen@redhat.com">Stefan Guilhen</a>
 */
public class ClientContextFactory extends ORBInitialContextFactory
{
   private static ORB orb;

   @Override
   protected void insertORB(Hashtable environment)
   {
      if (orb == null)
      {
         Properties orbProps = null;
         // if environment is already a Properties instance, we are good to go.
         if (environment instanceof Properties)
         {
            orbProps = (Properties) environment;
         }
         // if not, copy the environment content to a Properties instance.
         else if (environment != null)
         {
            orbProps = new Properties();
            for (Enumeration envProp = environment.keys(); envProp.hasMoreElements();)
            {
               String key = (String) envProp.nextElement();
               Object value = environment.get(key);
               if (value instanceof String)
                  orbProps.put(key, value);
            }
         }
         else
         {
            orbProps = new Properties();
         }
         // create a new ORB instance to be used by the NamingContext.
         orb = org.omg.CORBA.ORB.init(new String[0], orbProps);
      }
      // set the ORB instance if it hasn't been set yet.
      if (environment.containsKey(ORBInitialContextFactory.ORB_INSTANCE) == false)
         environment.put(ORBInitialContextFactory.ORB_INSTANCE, orb);
   }
}
