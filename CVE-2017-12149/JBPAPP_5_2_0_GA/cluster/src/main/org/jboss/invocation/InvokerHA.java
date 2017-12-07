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
package org.jboss.invocation;

import java.io.Serializable;
import javax.management.ObjectName;

import org.jboss.ha.framework.interfaces.LoadBalancePolicy;
import org.jboss.ha.framework.server.HATarget;

/** An administrative interface used by server side proxy factories during
 * the creation of HA capable invokers. Note that this does NOT extend the
 * Invoker interface because these methods are not for use by an invoker
 * client.
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81001 $
 */
public interface InvokerHA
{
   public Invoker createProxy(ObjectName targetName, LoadBalancePolicy policy,
      String proxyFamilyName)
      throws Exception;
   public Serializable getStub();
   public void registerBean(ObjectName targetName, HATarget target)
      throws Exception;
   public void unregisterBean(ObjectName targetName)
      throws Exception;
}
