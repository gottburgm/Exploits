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
package org.jboss.client;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.jboss.logging.Logger;
import org.jboss.naming.client.java.javaURLContextFactory;

/**
 * A AppClientLauncher implementation that simply looks for a static main
 * method on the
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 85945 $
 */
public class ReflectionLauncher
   implements AppClientLauncher
{
   private static Logger log = Logger.getLogger(ReflectionLauncher.class);

   /**
    * Launch a javaee client application.
    * 
    * @param clientClass - the class whose main(String[]) will be invoked
    * @param clientName - the client name that maps to the server side JNDI ENC.
    *    May be null indicating the name should be taken from the client jar
    *    descriptors/annotations.
    * @param args - the args to pass to main method
    * @throws Throwable
    */
   public void launch(String clientClass, String clientName, String[] args)
      throws Throwable
   {
      try
      {
         System.setProperty(javaURLContextFactory.J2EE_CLIENT_NAME_PROP, clientName);
         // invoke the client class
         Class cl = Class.forName(clientClass);
         Method main = cl.getDeclaredMethod("main", new Class[]{String[].class});
         Object[] mainArgs = {args};
         if( Modifier.isStatic(main.getModifiers()) )
         {
            main.invoke(null, mainArgs);
         }
         else
         {
            Object client = cl.newInstance();
            main.invoke(client, mainArgs);
         }
         log.debug("Client invoker success.");
      }
      catch (InvocationTargetException e)
      {
         throw e.getTargetException();
      }
   }
}
