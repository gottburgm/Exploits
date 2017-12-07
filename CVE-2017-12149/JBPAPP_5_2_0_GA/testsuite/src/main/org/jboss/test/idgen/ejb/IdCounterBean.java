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

import java.rmi.RemoteException;
import javax.ejb.*;
import javax.naming.*;

import org.jboss.test.util.ejb.EntitySupport;

/**
 *      
 *   @see <related>
 *   @author $Author: dimitris@jboss.org $
 *   @version $Revision: 81036 $
 */
public abstract class IdCounterBean
   extends EntitySupport
{
   long nextId;
   long size;
   
   public long getNextValue()
   {
      // Is sequence finished?
      // If so start a new one

      if (nextId == (getCurrentValue() + size))
      {
         setCurrentValue(nextId);
      }
      
      return nextId++;
   }
   
   public abstract long getCurrentValue();
   public abstract void setCurrentValue(long current);
	
   public abstract String getName();
   public abstract void setName(String beanName);
	
   public void ejbLoad()
      throws RemoteException
   {
      nextId = getCurrentValue();
   }
	
   public void setEntityContext(EntityContext ctx)
      throws RemoteException
   {
      super.setEntityContext(ctx);
      
      try {
         size = ((Long)new InitialContext().lookup("java:comp/env/size")).longValue();
      } 
      catch (Exception e) {
         throw new EJBException(e);
      }
   }
}
