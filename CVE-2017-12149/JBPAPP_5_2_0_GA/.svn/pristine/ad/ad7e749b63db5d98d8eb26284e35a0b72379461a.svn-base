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
package org.jboss.test.iiop.test;

import java.rmi.Remote;

import junit.framework.Assert;
import junit.framework.Test;

import org.jboss.test.JBossIIOPTestCase;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContext;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.NotFound;

public class NamingServiceStressTestCase extends JBossIIOPTestCase
{
   
   public NamingServiceStressTestCase(String name) 
   {
      super(name);
   }

   public void testNamingService() throws Exception 
   {
      // obtain a reference to the root context.
      final ORB orb = ORB.init(new String[0], System.getProperties());
      NamingContextExt rootContext = NamingContextExtHelper.narrow(orb.resolve_initial_references("NameService"));
      assertNotNull("Unexpected null root context", rootContext);
      
      // bind one thousand contexts to the CORBA naming service.
      StringBuilder builder = new StringBuilder();
      for (int i = 0; i < 1000; i++)
      {
         if (builder.length() != 0)
            builder.append("/");
         builder.append("ctx");
         builder.append(i);

         NameComponent[] component = rootContext.to_name(builder.toString());
         rootContext.bind_new_context(component);

         // check the binding by performing a lookup.
         NamingContext context = NamingContextExtHelper.narrow(rootContext.resolve(component));
         assertNotNull(context);
      }
      
      // unbind all contexts in reverse order.
      for (int i = 999; i >= 0; i--)
      {
         NameComponent[] component = rootContext.to_name(builder.toString());
         rootContext.unbind(component);
         try {
            org.omg.CORBA.Object result = rootContext.resolve(component);
            fail();
         }
         catch (NotFound nf) {
            // do nothing, the not found exception is expected.
         }
         if (i > 0)
            builder.delete(builder.lastIndexOf("/"), builder.length());
      }
   }
}
