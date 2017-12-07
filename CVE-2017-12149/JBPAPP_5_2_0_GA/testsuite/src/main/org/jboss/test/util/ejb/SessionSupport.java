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
package org.jboss.test.util.ejb;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.ejb.CreateException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import org.jboss.logging.Logger;

/**
 * Simple SessionBean support base class
 * 
 * @author Rickard Oberg
 * @version $Revision: 81036 $
 */
public abstract class SessionSupport implements SessionBean
{
   protected transient Logger log = Logger.getLogger(getClass());

   protected SessionContext sessionCtx;
   
   public void ejbCreate() throws CreateException
   {
   }
   
   public void setSessionContext(SessionContext ctx) 
   {
      sessionCtx = ctx;
   }
	
   public void ejbActivate() 
   {
   }
	
   public void ejbPassivate() 
   {
   }
	
   public void ejbRemove() 
   {
   }

   private void writeObject(ObjectOutputStream stream) throws IOException
   {
      // nothing
   }
   
   private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException
   {
      // reset logging
      log = Logger.getLogger(getClass());
   }
}
