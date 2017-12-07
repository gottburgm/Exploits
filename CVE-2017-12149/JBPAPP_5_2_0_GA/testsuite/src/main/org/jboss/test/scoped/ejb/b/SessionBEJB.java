/*
* JBoss, a division of Red Hat
* Copyright 2006, Red Hat Middleware, LLC, and individual contributors as indicated
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
package org.jboss.test.scoped.ejb.b;

import org.jboss.test.scoped.interfaces.dto.SimpleResponseDTO;
import org.jboss.test.scoped.interfaces.dto.SimpleRequestDTO;
import org.jboss.logging.Logger;

import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.EJBException;
import java.rmi.RemoteException;

/**
 * @author <a href="mailto:tom@jboss.org">Tom Elrod</a>
 */
public class SessionBEJB implements SessionBean
{
   private static final long serialVersionUID = 2L;

   public SessionBEJB()
   {
       super();
   }


   public SimpleResponseDTO runSimpleTest(SimpleRequestDTO requestDTO)
   {

       try
       {
          SimpleResponseDTO rsDTO = new SimpleResponseDTO();
          rsDTO.setFirstName(requestDTO.getFirstName().toUpperCase());
          rsDTO.setLastName(requestDTO.getLastName().toUpperCase());
          System.out.println("Got firstname: " + requestDTO.getFirstName());
          System.out.println("Got lastname:" + requestDTO.getLastName());
           return rsDTO;
       }
       catch (Throwable t)
       {
           Logger logger=Logger.getLogger(this.getClass());
           logger.error(t);

           SimpleResponseDTO rsDTO =  new SimpleResponseDTO();

           return rsDTO;
       }
   }

   public String stringTest(String request) throws java.rmi.RemoteException
   {
       return request.toUpperCase();
   }

   public void setSessionContext(SessionContext arg0) throws EJBException, RemoteException
   {
   }

   public void ejbCreate() throws EJBException, RemoteException
   {
   }

   public void ejbRemove() throws EJBException, RemoteException
   {
   }

   public void ejbActivate() throws EJBException, RemoteException
   {
   }

   public void ejbPassivate() throws EJBException, RemoteException
   {
   }
 }
