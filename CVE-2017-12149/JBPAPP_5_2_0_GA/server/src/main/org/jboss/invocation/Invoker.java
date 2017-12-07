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

import java.rmi.Remote;

import org.jboss.proxy.Interceptor;

import org.jboss.util.id.GUID;

/**
 * This invoker carries Invocation in the JMX target node.
 * 
 * <p>
 * The interface in the current JBoss can be implemented with Remote/local switches or
 * with clustered invokers, this interface just masks the network details and the topology
 * of the JMX nodes for the client proxies. 
 *
 * @author  <a href="mailto:marc@jboss.org">Marc Fleury</a>
 * @version $Revision: 81030 $
 * 
 * <p><b>Revisions:</b>
 *
 * <p><b>20011114 marc fleury:</b>
 * <ul>
 *   <li>Initial check-in
 * </ul>
 */
public interface Invoker
   extends Remote
{    
   /**
    * A globaly unique identifier use to determine if an instance is local
    * to the invoker.
    */
   GUID ID = new GUID();
   
   /**
    * A free form String identifier for this delegate invoker, can be clustered or target node
    * This should evolve in a more advanced meta-inf object
    */
   String getServerHostName() throws Exception;
   
   /**
    * The invoke with an Invocation Object.
    * 
    * <p>
    * the delegate can handle network protocols on behalf of proxies (proxies delegate to these 
    * puppies). We provide default implemenations with JRMP/Local/Clustered invokers.
    * The delegates are not tied to a type of invocation (EJB or generic RMI).
    *
    * @param invocation    A pointer to the invocation object
    * @return              Return value of method invocation.
    * 
    * @throws Exception    Failed to invoke method.
    */
   Object invoke(Invocation invocation) throws Exception;
}
