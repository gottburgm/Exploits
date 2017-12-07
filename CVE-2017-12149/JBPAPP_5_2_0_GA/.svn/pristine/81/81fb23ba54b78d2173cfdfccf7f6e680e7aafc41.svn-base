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
package org.jboss.test.ws.jaxws.samples.exception.server;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.ws.soap.SOAPFaultException;

public class EndpointImpl
{
   public void throwRuntimeException()
   {
      throw new RuntimeException("oh no, a runtime exception occured.");
   }

   public void throwSoapFaultException()
   {
      // This should be thrown as-is
      try
      {
         SOAPFactory factory = SOAPFactory.newInstance();
         SOAPFault fault = factory.createFault("this is a fault string!", new QName("http://foo", "FooCode"));
         fault.setFaultActor("mr.actor");
         fault.addDetail().addChildElement("test");
         throw new SOAPFaultException(fault);
      }
      catch (SOAPException s)
      {
         throw new RuntimeException(s);
      }
   }

   public void throwApplicationException() throws UserException
   {
      throw new UserException("validation", 123, "Some validation error");
   }
}
