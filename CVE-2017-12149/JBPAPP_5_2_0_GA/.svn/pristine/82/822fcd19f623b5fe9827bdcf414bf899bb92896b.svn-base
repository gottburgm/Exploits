/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.iiop.jbpapp6462.servant;

import java.util.Hashtable;

import javax.naming.InitialContext;

import javax.rmi.PortableRemoteObject;

import org.jboss.test.iiop.jbpapp6462.ejb.TestEJB;
import org.jboss.test.iiop.jbpapp6462.ejb.TestEJBHome;

import org.jboss.test.iiop.jbpapp6462.generated.TestServantPOA;

/**
 * The CORBA servant used for testing JBPAPP-6462.  This servant calls an
 * in EAR EJB component.
 */
public class TestServant extends TestServantPOA
{
   public void testServantMethod()
   {
      try
      {
         // Lookup and call the in-EAR EJB.  Specifying java.naming.provider.url
         // is required to recreate the problem described in JBPAPP-6462 even
         // though in this case we're actually making an in-VM call.
         Hashtable properties = new Hashtable();
         properties.put("java.naming.provider.url", getJNDIURL());
         InitialContext ic = new InitialContext(properties);
         Object obj = ic.lookup("jbpapp6462");

         // This method call is what causes the CNFE.
         TestEJBHome home = (TestEJBHome) PortableRemoteObject.narrow(obj, TestEJBHome.class);
         TestEJB test = home.create();
         test.testEJBMethod();
         System.out.println("TestEJB.testEJBMethod successfully invoked.");
      }
      catch (Throwable t)
      {
         throw new RuntimeException("Unexpected exception: ", t);
      }
   }

   /**
    * Get the JNDI URL of the JBoss instance in which this servant is running.
    */
   private String getJNDIURL()
   {
      String bindAddress = System.getProperty("jboss.bind.address");
      return "jnp://" + bindAddress + ":1099";
   }
}