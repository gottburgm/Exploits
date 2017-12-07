/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.profileservice.aop;

import java.util.Collection;

import org.jboss.aop.joinpoint.MethodInvocation;
import org.jboss.deployers.structure.spi.DeploymentContext;
import org.jboss.logging.Logger;

/**
 * @author Scott.Stark@jboss.org
 * @author adrian@jboss.org
 * @version $Revision: 105321 $
 */
public class MainDeployerAdvice
{
   public static Logger log = Logger.getLogger(MainDeployerAdvice.class);

   public MainDeployerAdvice()
   {
      log.trace("ctor");
   }

   public Object process(MethodInvocation invocation) throws Throwable
   {
      log.info("process(MethodInvocation), "+invocation);
      Object value = invocation.invokeNext();
      if( value instanceof Collection )
      {
         Collection<DeploymentContext> ctxs = (Collection<DeploymentContext>) value;
         log.info("ctxs: "+ctxs);
      }
      return value;
   }
}

