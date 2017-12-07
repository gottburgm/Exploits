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
package org.jboss.test.cluster.rspfilter;

import java.io.Serializable;

import org.jboss.ha.framework.interfaces.ClusterNode;
import org.jboss.ha.framework.interfaces.ResponseFilter;
import org.jboss.ha.framework.server.ClusterPartition.NoHandlerForRPC;
import org.jboss.logging.Logger;

/**
 * TruthfulResponseFilter.
 * 
 * @author Galder Zamarreño
 */
public class TruthfulResponseFilter implements ResponseFilter, Serializable
{
   private static final long serialVersionUID = 2223820538160300865L;
   private static final Logger log = Logger.getLogger(TruthfulResponseFilter.class);
   private volatile boolean lookupSucceeded;

   public boolean isAcceptable(Object response, ClusterNode sender)
   {
      log.debug("isAcceptable (" + response + ") from " + sender);
      
      if (response instanceof Boolean && ((Boolean)response).booleanValue()) 
      {
         lookupSucceeded = true; 
         
         log.debug("Lookup succeded from " + sender);
         
         return true;
      }
      
      return false;
   }

   public boolean needMoreResponses()
   {
      log.debug("needMoreResponses? " + !(lookupSucceeded));
      return !(lookupSucceeded);
   }

}
