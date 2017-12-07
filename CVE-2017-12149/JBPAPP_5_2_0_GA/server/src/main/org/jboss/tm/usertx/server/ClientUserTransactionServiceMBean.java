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
package org.jboss.tm.usertx.server;

import javax.management.ObjectName;
import org.jboss.mx.util.ObjectNameFactory;
import org.jboss.invocation.Invocation;

/**
 * MBean for ClientUserTransaction service.
 *
 * @author <a href="mailto:osh@sparre.dk">Ole Husgaard</a>
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81030 $
 */
public interface ClientUserTransactionServiceMBean
   extends org.jboss.system.ServiceMBean
{
   /** Set the proxy factory service for the UserTransactionSessions
    * @param proxyName the proxy factory MBean name
    */ 
   public void setTxProxyName(ObjectName proxyName);

   /** The detached invoker callback operation.
    * 
    * @param invocation
    * @return the invocation result
    * @throws Exception thrown on invocation failure
    */ 
   public Object invoke(Invocation invocation) throws Exception;
}

