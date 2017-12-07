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
package org.jboss.ha.jndi;

import org.jboss.ha.framework.interfaces.ClusterNode;
import org.jboss.ha.framework.interfaces.ResponseFilter;
import org.jboss.ha.framework.server.ClusterPartition.NoHandlerForRPC;
import org.jboss.logging.Logger;

/**
 * This is a response filter that will stop waiting for responses as soon as it has received a 
 * response that's neither null, nor Exception, nor NoHandlerForRPC. This allows for example 
 * HAJNDI calls to return as soon as cluster wide lookup has succeeded in a node.
 * 
 * @author Galder Zamarreño
 */
public class LookupSucceededFilter implements ResponseFilter
{
   private static final Logger log = Logger.getLogger(LookupSucceededFilter.class);
   private static final boolean trace = log.isTraceEnabled();
   private volatile boolean lookupSucceeded;

   public boolean isAcceptable(Object response, ClusterNode sender)
   {
      if (trace)
      {
         log.trace("isAcceptable (" + response + ") from " + sender);
      }
      
      if ((response != null) && !(response instanceof Exception) && !(response instanceof NoHandlerForRPC))
      {      
         if (trace)
         {
            log.trace("Lookup succeded from " + sender);
         }
         lookupSucceeded = true;
      }

      // NOTE: *MUST* always return true or the RPC will never complete
      // if no response is "acceptable". Returning true cause the response
      // count to be incremented, allowing the call to complete when the
      // desired numbers of responses are in
      return true;      
   }

   public boolean needMoreResponses()
   {
      return !(lookupSucceeded);
   }
}
