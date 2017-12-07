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
package org.jboss.test.txiiop.ejb;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.rmi.RemoteException;
import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EJBObject;
import javax.ejb.Handle;
import javax.ejb.RemoveException;
import javax.ejb.SessionSynchronization;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.jboss.logging.Logger;
import org.jboss.test.cts.interfaces.BeanContextInfo;
import org.jboss.test.cts.interfaces.CtsCmpLocal;
import org.jboss.test.cts.interfaces.CtsCmpLocalHome;
import org.jboss.test.cts.interfaces.StatefulSession;
import org.jboss.test.cts.interfaces.StatefulSessionHome;
import org.jboss.test.cts.interfaces.StatelessSession;
import org.jboss.test.cts.interfaces.StatelessSessionHome;
import org.jboss.test.cts.keys.AccountPK;
import org.jboss.test.util.ejb.SessionSupport;


/** The stateful session ejb implementation
 *
 *   @author Scott.Stark@jboss.org
 *   @version $Revision: 81036 $
 */
public class StatefulSessionBean
   extends SessionSupport
   implements SessionSynchronization
{
   private static transient Logger log = Logger.getLogger(StatefulSessionBean.class);
   private transient int counterAtTxStart;
   private String testName;
   private int counter;

   public void ejbCreate(String testName)
   {
      this.testName = testName;
      log = Logger.getLogger(StatefulSessionBean.class.getName()+"#"+testName);
      log.debug("ejbCreate("+testName+"), ctx="+sessionCtx);
   }

   public void afterBegin ()
   {
      log.debug("afterBegin()..., counter="+counter);
      counterAtTxStart = counter;
   }
   public void afterCompletion (boolean isCommited)
   {
      log.debug("afterCompletion(), isCommited="+isCommited
         +", counter="+counter+", counterAtTxStart="+counterAtTxStart);
      if( isCommited == false )
      {
         counter = counterAtTxStart;
         log.debug("Rolling counter back to: "+counter);
      }
      else
      {
         log.debug("Committed updated counter: "+counter);         
      }
   }
   public void beforeCompletion ()
   {
      log.debug("beforeCompletion(), counter="+counter
         +", counterAtTxStart="+counterAtTxStart);
   }

   public void incCounter ()
   {
      counter++;
   }

   public void decCounter ()
   {
      counter--;
   }

   public int getCounter ()
   {
      return counter;
   }

   public void setCounter (int value)
   {
      counter = value;
   }

   public String txMandatoryMethod(String msg)
   {
      log.debug("txMandatoryMethod( ), msg="+msg);
      return msg;
   }

}
