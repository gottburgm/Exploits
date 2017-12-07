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
package org.jboss.test.ws.jaxws.ejb3Integration;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.LogicalMessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import java.util.Set;
import java.util.HashSet;

/**
 * A generic jaxws soap handler
 *
 * @author Thomas.Diesler@jboss.org
 * @since 13-Aug-2006
 */
public abstract class GenericSOAPHandler<C extends LogicalMessageContext> extends GenericHandler implements SOAPHandler
{
   // The header blocks that can be processed by this Handler instance
   private Set<QName> headers = new HashSet<QName>();

   /** Gets the header blocks that can be processed by this Handler instance.
    */
   public Set<QName> getHeaders()
   {
      return headers;
   }

   /** Sets the header blocks that can be processed by this Handler instance.
    */
   public void setHeaders(Set<QName> headers)
   {
      this.headers = headers;
   }
}

