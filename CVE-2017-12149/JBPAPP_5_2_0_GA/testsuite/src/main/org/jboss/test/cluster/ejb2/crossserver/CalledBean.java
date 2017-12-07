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
package org.jboss.test.cluster.ejb2.crossserver;

import java.rmi.RemoteException;
import java.rmi.dgc.VMID;
import java.util.Properties;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.CreateException;
import javax.naming.InitialContext;
import javax.naming.Context;
import javax.rmi.PortableRemoteObject;

import org.jboss.logging.Logger;

/** This bean is called by the test client to validate ejb to ejb calls between
 * server instances.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 85945 $
 */
public class CalledBean implements SessionBean
{
   private static Logger log = Logger.getLogger(CalledBean.class);
   private static VMID vmid = new VMID();

   public void ejbCreate() throws CreateException
   {
      log.debug("ejbCreate() called");
   }

   public void ejbActivate()
   {
      log.debug("ejbActivate() called");
   }

   public void ejbPassivate()
   {
      log.debug("ejbPassivate() called");
   }

   public void ejbRemove()
   {
      log.debug("ejbRemove() called");
   }

   public void setSessionContext(SessionContext context)
   {
   }

   /** This method calls echo on a CalleeBean
    */
   public VMID[] invokeCall(String jndiURL, String jndiName)
      throws RemoteException
   {
      log.info("invokeCall, jndiURL="+jndiURL+", jndiName="+jndiName);
      VMID[] ids = {vmid, null};
      try
      {
         Properties props = new Properties();
         props.setProperty(Context.PROVIDER_URL, jndiURL);
         InitialContext ic = new InitialContext(props);
         Object ref = ic.lookup(jndiName);
         CalleeHome localHome = (CalleeHome) PortableRemoteObject.narrow(ref,
               CalleeHome.class);
         CalleeRemote remoteBean = localHome.create();
         ids[1] = remoteBean.call("invokeCall");
        log.info("echo, CalleeRemote.call="+ids[1]+", vmid="+ids[0]);
      }
      catch(Exception e)
      {
         log.error("Failed to invoke CalleeRemote.call", e);
         throw new RemoteException("Failed to invoke CalleeRemote.call", e);
      }
      return ids;
   }
   
}
