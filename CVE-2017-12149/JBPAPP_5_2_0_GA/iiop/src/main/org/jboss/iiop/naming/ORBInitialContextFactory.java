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

import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import org.jnp.interfaces.NamingContextFactory;
import org.omg.CORBA.ORB;

/**
 * An ORBInitialContextFactory, that includes the orb
 * in the environment.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 81018 $
 */
public class ORBInitialContextFactory extends NamingContextFactory
{
   public static final String ORB_INSTANCE = "java.naming.corba.orb";
   
   /** The orb to include in the naming environment */
   private static ORB orb;
   
   /**
    * Get the orb
    * 
    * @return the orb
    */
   public static ORB getORB()
   {
      return orb;
   }
   
   /**
    * Set the orb
    * 
    * @param orb the orb to use
    */
   public static void setORB(ORB orb)
   {
      if (orb == null)
         ORBInitialContextFactory.orb = null;
      else
         ORBInitialContextFactory.orb = new SerializableORB(orb);
   }

   public Context getInitialContext(Hashtable env) throws NamingException
   {
      insertORB(env);
      return super.getInitialContext(env);
   }
   
   public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable environment) throws Exception
   {
      insertORB(environment);
      return super.getObjectInstance(obj, name, nameCtx, environment);
   }
   
   protected void insertORB(Hashtable environment)
   {
      if (orb != null && environment.containsKey(ORB_INSTANCE) == false)
         environment.put(ORB_INSTANCE, orb);
   }
}
