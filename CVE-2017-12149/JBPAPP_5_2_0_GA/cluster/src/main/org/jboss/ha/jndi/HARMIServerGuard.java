 /*
  * JBoss, Home of Professional Open Source
  * Copyright 2005, JBoss Inc., and individual contributors as indicated
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

package org.jboss.ha.jndi;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.rmi.server.RemoteStub;
import java.util.List;

import javax.naming.NoPermissionException;

import org.jboss.ha.framework.interfaces.HARMIResponse;
import org.jboss.ha.framework.interfaces.HARMIServer;
import org.jboss.invocation.MarshalledInvocation;
import org.jboss.logging.Logger;
import org.jnp.server.NamingServerGuard;


/**
 * Object to register instead of original object.
 * It will guard certain invocations see invoke method. 
 * 
 * @author  <a href="mailto:pskopek@redhat.com">Peter Skopek</a>
 *
 */
public class HARMIServerGuard implements HARMIServer, Serializable
{

   private static Logger log = Logger.getLogger(HARMIServerGuard.class);
   
   private HARMIServer guardedHARMIServer;
   
   public HARMIServerGuard(HARMIServer guardedHARMIServer) 
   {
      this.guardedHARMIServer = guardedHARMIServer;
   }
   
   /* (non-Javadoc)
    * @see org.jboss.ha.framework.interfaces.HARMIServer#invoke(long, org.jboss.invocation.MarshalledInvocation)
    */
   public HARMIResponse invoke(long tag, MarshalledInvocation mi)
         throws Exception
   {
      log.info("RMI local invocation =" + mi.isLocal());
      Method method = mi.getMethod();
      if (NamingServerGuard.GUARDED_JNDI_METHOD_NAMES.indexOf(method.getName()) != -1) {
         throw new NoPermissionException(method.getName() + 
               " JNDI operation not allowed when on non-local invocation.");
      }

      return guardedHARMIServer.invoke(tag, mi);
   }

   /* (non-Javadoc)
    * @see org.jboss.ha.framework.interfaces.HARMIServer#getReplicants()
    */
   public List getReplicants() throws Exception
   {
      return guardedHARMIServer.getReplicants();
   }

   /* (non-Javadoc)
    * @see org.jboss.ha.framework.interfaces.HARMIServer#getLocal()
    */
   public Object getLocal() throws Exception
   {
      return guardedHARMIServer.getLocal();
   }

}
