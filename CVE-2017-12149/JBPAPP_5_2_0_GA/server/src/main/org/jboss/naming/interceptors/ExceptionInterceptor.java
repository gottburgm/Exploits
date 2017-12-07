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
package org.jboss.naming.interceptors;

import java.io.Externalizable;
import java.io.IOException;

import javax.naming.CommunicationException;
import javax.naming.NamingException;
import javax.naming.ServiceUnavailableException;

import org.jboss.invocation.Invocation;
import org.jboss.proxy.Interceptor;

/** A client interceptor that handles the wrapping of exceptions to
 * NamingExceptions
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81030 $
 */
public class ExceptionInterceptor extends Interceptor
   implements Externalizable
{
   /** The serialVersionUID. @since 1.1.2.1 */
   private static final long serialVersionUID = 6010013004557885014L;

   /** Handle methods locally on the client
    *
    * @param mi
    * @return
    * @throws Throwable
    */
   public Object invoke(Invocation mi) throws Throwable
   {
      Object value = null;
      try
      {
         value = getNext().invoke(mi);
      }
      catch(NamingException e)
      {
         throw e;
      }
      catch(IOException e)
      {
         CommunicationException ce = new CommunicationException("Operation failed");
         ce.setRootCause(e);
         throw ce;
      }
      catch(Throwable t)
      {
         ServiceUnavailableException sue = new ServiceUnavailableException("Unexpected failure");
         sue.setRootCause(t);
         throw sue;
      }

      return value;
   }

}
