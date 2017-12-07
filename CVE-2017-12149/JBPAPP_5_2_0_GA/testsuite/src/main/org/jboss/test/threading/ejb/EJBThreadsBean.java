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
package org.jboss.test.threading.ejb;

import java.rmi.*;
import javax.ejb.*;

/**
*   <description> 
*
*   @see <related>
*   @author  <a href="mailto:marc@jboss.org">Marc Fleury</a>
*   @version $Revision: 81036 $
*   
*   Revisions:
*
*   20010625 marc fleury: Initial version
*/
public class EJBThreadsBean implements EntityBean
{
   org.jboss.logging.Logger log = org.jboss.logging.Logger.getLogger(getClass());
   
   // Constants -----------------------------------------------------
  
   // Attributes ----------------------------------------------------
   public String id;
   // Static --------------------------------------------------------
  
  // Constructors --------------------------------------------------
  
  // Public --------------------------------------------------------
 
	
   public String ejbCreate(String id) 
      throws RemoteException, CreateException {
      log.debug("create"+Thread.currentThread()+" id: "+id);
      this.id = id;
		
      return id;
   }
	
   public void ejbPostCreate(String id)  {}
   public void ejbRemove() throws RemoveException {log.debug("remove"+Thread.currentThread());}
   public void ejbActivate() throws RemoteException {}
   public void ejbPassivate() throws RemoteException {}
   public void ejbLoad() throws RemoteException {}
   public void ejbStore() throws RemoteException {}
	
   public void setEntityContext(EntityContext context) throws RemoteException {}
   public void unsetEntityContext() throws RemoteException{}
	
   public void test() {
		
      log.debug("test"+Thread.currentThread());
			
   }
	
   public void testBusinessException() 
      throws RemoteException
   {
      log.debug("testBusinessExcetiopn"+Thread.currentThread());
      throw new RemoteException("TestBusinessException");
   };
	
   public void testRuntimeException() 
      throws RemoteException
   {
      log.debug("testRuntimeException"+Thread.currentThread());
      throw new NullPointerException();
   }
   public void testTimeOut()
      throws RemoteException
   {
      log.debug("testTimeout"+Thread.currentThread());
      synchronized (this)
      {
         try {
            wait(5000);
         }
         catch (InterruptedException e) {
				
         }
      }
   }
   public void testNonTransactional() throws RemoteException
   {
      log.debug("testNonTransactional"+Thread.currentThread());
   }
}
