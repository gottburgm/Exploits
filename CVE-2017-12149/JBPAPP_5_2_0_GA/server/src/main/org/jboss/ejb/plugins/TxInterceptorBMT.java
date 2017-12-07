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
package org.jboss.ejb.plugins;

import java.util.Map;

import org.jboss.invocation.Invocation;

import org.jboss.metadata.SessionMetaData;

/**
 *   This interceptor handles transactions for session BMT beans.
 *
 *   @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 *   @author <a href="mailto:sebastien.alborini@m4x.org">Sebastien Alborini</a>
 *   @author <a href="mailto:peter.antman@tim.se">Peter Antman</a>.
 *   @author <a href="mailto:akkerman@cs.nyu.edu">Anatoly Akkerman</a>
 *   @author <a href="mailto:osh@sparre.dk">Ole Husgaard</a>
 *   @version $Revision: 81030 $
 */
public class TxInterceptorBMT
   extends AbstractTxInterceptorBMT
{

   // Attributes ----------------------------------------------------

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------

   // Interceptor implementation --------------------------------------

   public void create()
      throws Exception
   {
      // Do initialization in superclass.
      super.create();
 
      // Set the atateless attribute
      stateless = ((SessionMetaData)container.getBeanMetaData()).isStateless();
   }

   public Object invokeHome(Invocation mi)
      throws Exception
   {
      // stateless: no context, no transaction, no call to the instance
      if (stateless || mi.getEnterpriseContext() == null)
         return getNext().invokeHome(mi);
      else
         return invokeNext(mi);
   }

   public Object invoke(Invocation mi)
      throws Exception
   {
      return invokeNext(mi);
   }

  // Monitorable implementation ------------------------------------
  public void sample(Object s)
  {
    // Just here to because Monitorable request it but will be removed soon
  }
  public Map retrieveStatistic()
  {
    return null;
  }
  public void resetStatistic()
  {
  }

   // Protected  ----------------------------------------------------

   // Inner classes -------------------------------------------------

}

