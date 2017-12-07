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
package org.jboss.test.ws.jaxws.ejb3Integration.packaging;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.jws.WebService;

import org.jboss.ejb3.annotation.RemoteBinding;
import org.jboss.test.ws.jaxws.ejb3Integration.packaging.unit.EJB3WSEndpointPackagingTestCase;

/**
 * Used in {@link EJB3WSEndpointPackagingTestCase}
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
@Stateless
@WebService(endpointInterface = "org.jboss.test.ws.jaxws.ejb3Integration.packaging.EchoEJB3WSEndpoint")
@RemoteBinding(jndiBinding = EchoWSEndpointImplInSAR.JNDI_NAME)
@Remote (EchoEJB3WSEndpoint.class)
public class EchoWSEndpointImplInSAR implements EchoEJB3WSEndpoint
{
   public static final String JNDI_NAME = "WSEndpointSLSBInSAR";

   @Override
   public String echo(String msg)
   {
      return msg;
   }
}
