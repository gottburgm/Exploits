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
package org.jboss.ha.framework.interfaces;


import java.util.Hashtable;
import org.jboss.invocation.MarshalledInvocation;

/** 
 *   When using HA-RMI, the RMI communication end-point on the server-side is
 *   an instance of this class. All invocations are sent through this servant
 *   that will route the call to the appropriate object and call the appropriate
 *   Java method.
 *
 *   @author <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>
 *   @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 *   @version $Revision: 81001 $
 *
 * <p><b>Revisions:</b><br>
 */

public interface HARMIServer extends java.rmi.Remote
{
   public static Hashtable rmiServers = new Hashtable();

   /**
    * Performs an invocation through this HA-RMI for the target object hidden behind it.
    */   
   public HARMIResponse invoke (long tag, MarshalledInvocation mi) throws Exception;
   
   /**
    * Returns a list of node stubs that are current replica of this service.
    */   
   public java.util.List getReplicants () throws Exception;
   
   /**
    * Get local stub for this service.
    */   
   public Object getLocal() throws Exception;
}
