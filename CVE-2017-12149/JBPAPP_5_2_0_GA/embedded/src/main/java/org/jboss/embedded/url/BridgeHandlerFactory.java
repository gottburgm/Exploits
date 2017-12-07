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
package org.jboss.embedded.url;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.List;

/**
 * comment
 *
 * @author <a href="bill@jboss.com">Bill Burke</a>
 * @version $Revision: 85945 $
 */
public class BridgeHandlerFactory implements URLStreamHandlerFactory
{
   private List<URLStreamHandlerFactory> list;

   public BridgeHandlerFactory(List<URLStreamHandlerFactory> list)
   {
      this.list = list;
   }

   public URLStreamHandler createURLStreamHandler(String protocol)
   {
      URLStreamHandler result = null;
      for (URLStreamHandlerFactory factory : list)
      {
         
         result = factory.createURLStreamHandler(protocol);
         if (result != null) return result;
      }
      return null;
   }
}
