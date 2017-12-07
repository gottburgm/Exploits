/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ejb3.webservice;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceException;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @author Heiko.Braun <heiko.braun@jboss.com>
 * @version $Revision: $
 */
@Stateless
@WebService(endpointInterface="org.jboss.test.ejb3.webservice.WebServiceContextEndpoint")
public class WebServiceContextEndpointBean implements WebServiceContextEndpoint
{
   @Resource
   private WebServiceContext wsCtx;

   @PostConstruct
   public void init()
   {
      if(null == wsCtx)
         throw new WebServiceException("WebService context is null @PostConstruct");
   }

   public String echo(String msg)
   {
      return msg +": " +wsCtx.getMessageContext();
   }

   public String report()
   {
      return wsCtx.toString();
   }
}
