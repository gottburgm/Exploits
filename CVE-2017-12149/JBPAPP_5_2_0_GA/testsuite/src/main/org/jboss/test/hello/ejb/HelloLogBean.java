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
package org.jboss.test.hello.ejb;

import javax.ejb.EJBException;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.CreateException;
import org.jboss.logging.Logger;

/**
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public abstract class HelloLogBean implements EntityBean
{
   private static Logger log = Logger.getLogger(HelloLogBean.class);

   public HelloLogBean()
   {
   }

   public String ejbCreate(String msg) throws CreateException
   {
      setHelloArg(msg);
      log.info("ejbCreate, msg=" + msg);
      return null;
   }

   public void ejbPostCreate(String msg)
   {
   }

   public abstract String getHelloArg();
   public abstract void setHelloArg(String echoArg);

   public abstract long getStartTime();
   public abstract void setStartTime(long startTime);

   public abstract long getEndTime();
   public abstract void setEndTime(long endTime);

   public long getElapsedTime()
   {
      long start = getStartTime();
      long end = getEndTime();
      return end - start;
   }

   public void setEntityContext(EntityContext ctx) throws EJBException
   {
   }

   public void unsetEntityContext() throws EJBException
   {
   }

   public void ejbActivate()
   {
   }

   public void ejbPassivate()
   {
   }

   public void ejbLoad()
   {
   }

   public void ejbStore()
   {
   }

   public void ejbRemove()
   {
   }
}
