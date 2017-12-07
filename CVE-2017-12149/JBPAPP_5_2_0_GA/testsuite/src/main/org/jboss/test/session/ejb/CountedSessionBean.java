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
package org.jboss.test.session.ejb;

import javax.ejb.CreateException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81036 $
 */
public class CountedSessionBean implements SessionBean
{
   private static final Logger log = Logger.getLogger(CountedSessionBean.class);

   public CountedSessionBean()
   {
      log.info("CTOR");
   }
   
   // Business Methods ----------------------------------------------
   
   public void doSomething(long delay)
   {
      log.info("doSomething(" + delay + ")");
      if (delay > 0)
      {
         try
         {
            Thread.sleep(delay);
         }
         catch (InterruptedException e)
         {
            Thread.currentThread().interrupt();
         }
      }
   }
   
   // Container callbacks -------------------------------------------
   
   public void setSessionContext(SessionContext ctx)
   {
      log.info("setSessionContext");
   }
   
   public void ejbCreate() throws CreateException
   {
      log.info("ejbCreate - " + CounterSessionBean.increaseCreateCounter());
   }
	
   public void ejbRemove()
   {
      log.info("ejbRemove - " + CounterSessionBean.increaseRemoveCounter());
   }

   public void ejbActivate()
   {
      log.info("ejbActivate");
   }
   
   public void ejbPassivate()
   {
      log.info("ejbPassivate");
   }
}
