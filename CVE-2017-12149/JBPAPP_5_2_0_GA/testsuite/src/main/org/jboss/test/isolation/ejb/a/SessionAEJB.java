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
package org.jboss.test.isolation.ejb.a;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.InitialContext;

import org.jboss.logging.Logger;
import org.jboss.test.isolation.interfaces.IsolationDTO;
import org.jboss.test.isolation.interfaces.b.SessionB;
import org.jboss.test.isolation.interfaces.b.SessionBHome;
import org.jboss.test.util.Debug;

/**
 * A SessionA.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 81036 $
 */
public class SessionAEJB implements SessionBean
{
   private static final Logger log = Logger.getLogger(SessionAEJB.class);
   
   public void invokeSessionB()
   {
      try
      {
         InitialContext ctx = new InitialContext();
         Object o = ctx.lookup("java:comp/env/ejb/SessionB");

         StringBuffer buffer = new StringBuffer("SessionBHome lookup");
         Debug.displayClassInfo(o.getClass(), buffer);
         log.info(buffer.toString());
         buffer = new StringBuffer("My SessionBHome");
         Debug.displayClassInfo(SessionBHome.class, buffer);
         log.info(buffer.toString());
         
         SessionBHome home = (SessionBHome) o;
         SessionB session = home.create();
         
         IsolationDTO dto = new IsolationDTO();
         dto.payload = "hello";
         
         IsolationDTO result = session.sayHello(dto);
         if (dto == result)
            throw new EJBException("Expected pass by value");
         if ("goodbye".equals(result.payload) == false)
            throw new EJBException("Did not get expected 'goodbye'");
      }
      catch (Exception e)
      {
         throw new EJBException(e);
      }
   }
   
   public void ejbCreate() throws CreateException
   {
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
   
   public void setSessionContext(SessionContext ctx)
   {
   }
}
