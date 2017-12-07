/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.resource.binding.remote;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;

/**
 * A ConnectionLeaseValidator.
 * 
 * @author <a href="weston.price@jboss.com">Weston Price</a>
 * @version $Revision: 71554 $
 */
public class ConnectionLeaseValidator
{

   private Map connectionMap;
   
   private long interval = Long.MAX_VALUE;
   
   private static final ConnectionLeaseValidator validator = new ConnectionLeaseValidator();
   
   public synchronized void registerConnections(Map connectionMap){
      
      validator.internalRegister(connectionMap);
   }

   private void internalRegister(Map connectionMap){
      
   }

   private ConnectionLeaseValidator(){
      
      AccessController.doPrivileged(new PrivilegedAction()
      {
         public Object run()
         {
            Runnable runnable = new ConnectionLeaseRunnable();
            Thread removerThread = new Thread(runnable, "ConnectionLeaseValidator");
            removerThread.setDaemon(true);
            removerThread.start();
            return null;
         }
      });
      
   }
   
   
   private class ConnectionLeaseRunnable implements Runnable{
      
      public void run()
      {
         // TODO Auto-generated method stub
         
      }
   }
   
   
}
