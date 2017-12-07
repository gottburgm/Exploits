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
package org.jboss.test.webservice.admindevel;

import javax.ejb.EJBException;
import javax.ejb.SessionContext;
import java.rmi.RemoteException;

/**
 * The typical Hello Session Bean this time
 * as a web-service.
 */
public class HelloEJBImpl implements javax.ejb.SessionBean
{
   public String helloString(String name)
   {
      return "Hello " + name + "!";
   }

   public HelloObj helloBean(HelloObj bean)
   {
      String msg = helloString(bean.getMsg());
      HelloObj hro = new HelloObj(msg);
      return hro;
   }

   public HelloObj[] helloArray(HelloObj[] query)
   {
      HelloObj[] reply = new HelloObj[query.length];
      for (int n = 0; n < query.length; n++)
      {
         HelloObj hello = (HelloObj)query[n];
         String msg = helloString(hello.getMsg());
         HelloObj hro = new HelloObj(msg);
         reply[n] = hro;
      }
      return reply;
   }

   public void setSessionContext(SessionContext ctx) throws EJBException, RemoteException
   {
   }

   public void ejbCreate()
   {
   }

   public void ejbRemove()
   {
   }

   public void ejbActivate()
   {
   }

   public void ejbPassivate()
   {
   }
}
