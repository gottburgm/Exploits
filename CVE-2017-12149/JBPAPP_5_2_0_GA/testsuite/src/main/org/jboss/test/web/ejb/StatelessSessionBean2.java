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
package org.jboss.test.web.ejb;

import javax.naming.InitialContext;
import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import org.jboss.test.web.interfaces.ReferenceTest;
import org.jboss.test.web.interfaces.StatelessSession;
import org.jboss.test.web.interfaces.StatelessSessionHome;
import org.jboss.test.web.interfaces.ReturnData;
import org.jboss.logging.Logger;

/** A stateless SessionBean 

 @author  Scott.Stark@jboss.org
 @version $Revision: 81036 $
 */
public class StatelessSessionBean2 implements SessionBean
{
   static Logger log = Logger.getLogger(StatelessSessionBean2.class);

   private SessionContext sessionContext;

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
      sessionContext = context;
   }

   public String echo(String arg)
   {
      log.debug("echo, arg=" + arg);
      return arg;
   }

   public String forward(String echoArg)
   {
      log.debug("forward, echoArg=" + echoArg);
      String echo = null;
      try
      {
         InitialContext ctx = new InitialContext();
         StatelessSessionHome home = (StatelessSessionHome) ctx.lookup("java:comp/env/ejb/Session");
         StatelessSession bean = home.create();
         echo = bean.echo(echoArg);
      }
      catch (Exception e)
      {
         log.debug("failed", e);
         e.fillInStackTrace();
         throw new EJBException(e);
      }
      return echo;
   }

   public void noop(ReferenceTest test, boolean optimized)
   {
      boolean wasSerialized = test.getWasSerialized();
      log.debug("noop, test.wasSerialized=" + wasSerialized + ", optimized=" + optimized);
      if (optimized && wasSerialized == true)
         throw new EJBException("Optimized call had serialized argument");
      if (optimized == false && wasSerialized == false)
         throw new EJBException("NotOptimized call had non serialized argument");
   }

   public ReturnData getData()
   {
      ReturnData data = new ReturnData();
      data.data = "TheReturnData2";
      return data;
   }

   /** A method deployed with no method permissions */
   public void unchecked()
   {
      log.debug("unchecked");
   }

   /** A method deployed with method permissions such that only a run-as
    * assignment will allow access. 
    */
   public void checkRunAs()
   {
      log.debug("checkRunAs");
   }
}
