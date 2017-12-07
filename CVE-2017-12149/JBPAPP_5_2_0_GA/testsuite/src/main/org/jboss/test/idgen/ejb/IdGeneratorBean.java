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
package org.jboss.test.idgen.ejb;

import java.rmi.*;
import javax.naming.*;
import javax.ejb.*;

import org.jboss.test.util.ejb.SessionSupport;
import org.jboss.test.idgen.interfaces.*;

/**
 *      
 *   @see <related>
 *   @author $Author: dimitris@jboss.org $
 *   @version $Revision: 81036 $
 */
public class IdGeneratorBean
   extends SessionSupport
{
   IdCounterHome counterHome;
   
   static final String SIZE = "java:comp/env/size";

   public long getNewId(String beanName)
      throws RemoteException
   {
      IdCounter counter;
      
      // Acquire counter
      try {
         counter = counterHome.findByPrimaryKey(beanName);
      } 
      catch (FinderException e) {
         try {
            counter = counterHome.create(beanName);
         } catch (CreateException ex) {
            throw new EJBException("Could not find or create counter for "+beanName);
         }
      }
      
      // Get id
      return counter.getNextValue();
   }

   public void setSessionContext(SessionContext context) 
   {
      super.setSessionContext(context);
      
      try {
         counterHome = (IdCounterHome)new InitialContext().lookup("java:comp/env/ejb/IdCounter");
      } 
      catch (Exception e) {
         throw new EJBException(e);
      }
   }
}
