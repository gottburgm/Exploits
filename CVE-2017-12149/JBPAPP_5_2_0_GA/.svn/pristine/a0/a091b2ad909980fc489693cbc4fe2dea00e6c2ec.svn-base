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

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextHelper;

import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.InvalidName;
import org.omg.CosNaming.NamingContextPackage.NotFound;

import org.omg.PortableServer.POA;

import org.omg.PortableServer.POAPackage.ObjectNotActive;
import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.WrongAdapter;
import org.omg.PortableServer.POAPackage.WrongPolicy;

/**
 * A micro-container bean that activates and deactivates the CORBA servant used
 * as a part of the test for JBPAPP-6462.
 */
public class TestServantBean
{
   private TestServant servant;
   private org.omg.CORBA.Object reference;

   public void start()
   throws CannotProceed, InvalidName, NamingException,
          NotFound, ServantNotActive, WrongPolicy
   {
      servant = new TestServant();
      InitialContext ic = new InitialContext();
      POA poa = (POA) ic.lookup("java:JBossCorbaPOA");
      reference = poa.servant_to_reference(servant);    
      NamingContextExt nce = (NamingContextExt) ic.lookup("java:JBossCorbaNaming");
      nce.rebind(nce.to_name("jbpapp6462"), reference);
      System.out.println("Servant started...");
    }
    
   public void stop()
   throws CannotProceed, InvalidName, NamingException, NotFound,
          ObjectNotActive, WrongAdapter, WrongPolicy
   {             
      InitialContext ic = new InitialContext();
      NamingContextExt nce = (NamingContextExt) ic.lookup("java:JBossCorbaNaming");
      nce.unbind(nce.to_name("jbpapp6462"));
      POA poa = (POA) ic.lookup("java:JBossCorbaPOA");
      poa.deactivate_object(poa.reference_to_id(reference));
      reference = null;
      servant = null;
      System.out.println("Servant stopped.");
   }
}