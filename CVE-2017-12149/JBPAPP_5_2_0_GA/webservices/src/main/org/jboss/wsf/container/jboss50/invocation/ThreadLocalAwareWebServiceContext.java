/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.wsf.container.jboss50.invocation;

import java.io.Serializable;
import java.security.Principal;

import javax.xml.ws.EndpointReference;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import org.w3c.dom.Element;

/**
 * Web service context implementation that is thread local aware as required by JAX-WS spec.
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public final class ThreadLocalAwareWebServiceContext implements WebServiceContext, Serializable
{

   private static final long serialVersionUID = 126557512266764152L;

   private static final transient ThreadLocalAwareWebServiceContext SINGLETON = new ThreadLocalAwareWebServiceContext();

   private final transient ThreadLocal<WebServiceContext> contexts = new InheritableThreadLocal<WebServiceContext>();

   public static ThreadLocalAwareWebServiceContext getInstance()
   {
      return SINGLETON;
   }

   public void setMessageContext(final WebServiceContext ctx)
   {
      this.contexts.set(ctx);
   }

   public EndpointReference getEndpointReference(final Element... referenceParameters)
   {
      final WebServiceContext delegee = this.contexts.get();

      if (delegee == null)
      {
         throw new IllegalStateException();
      }

      return delegee.getEndpointReference(referenceParameters);
   }

   public <T extends EndpointReference> T getEndpointReference(final Class<T> clazz,
         final Element... referenceParameters)
   {
      final WebServiceContext delegee = this.contexts.get();

      if (delegee == null)
      {
         throw new IllegalStateException();
      }

      return delegee.getEndpointReference(clazz, referenceParameters);
   }

   public MessageContext getMessageContext()
   {
      final WebServiceContext delegee = this.contexts.get();

      if (delegee == null)
      {
         throw new IllegalStateException();
      }

      return delegee.getMessageContext();
   }

   public Principal getUserPrincipal()
   {
      final WebServiceContext delegee = this.contexts.get();

      if (delegee == null)
      {
         throw new IllegalStateException();
      }

      return delegee.getUserPrincipal();
   }

   public boolean isUserInRole(String role)
   {
      final WebServiceContext delegee = this.contexts.get();

      if (delegee == null)
      {
         throw new IllegalStateException();
      }

      return delegee.isUserInRole(role);
   }

}
