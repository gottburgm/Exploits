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
package org.jboss.ejb.plugins;

import java.lang.reflect.Method;

import javax.ejb.Handle;

import org.jboss.ejb.Container;
import org.jboss.ejb.EnterpriseContext;
import org.jboss.ejb.StatefulSessionContainer;
import org.jboss.invocation.Invocation;
import org.jboss.logging.Logger;

//$Id: StatefulSessionSecurityInterceptor.java 85945 2009-03-16 19:45:12Z dimitris@jboss.org $

/**
 *  Interceptor that handles security aspects after the security checks
 *  have been made. Example: setting the principal on the EnterpriseContext
 *  
 *  This interceptor is needed because the security interceptor happens after
 *  the instance interceptor in the case of SFSB due to the reason that 
 *  security exceptions need to invalidate the session.
 *  
 *  Note: This interceptor has to follow the SFSB instance interceptor in the 
 *  call path as it obtains the enterprise context from the invocation
 *  
 *  JIRA Reference: JBAS-3976
 *  
 *  @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 *  @since  Jan 10, 2007 
 *  @version $Revision: 85945 $
 */
public class StatefulSessionSecurityInterceptor extends AbstractInterceptor 
{ 
   /** Instance logger. */
   protected Logger log = Logger.getLogger(this.getClass());
  
   protected StatefulSessionContainer container; 
   
   //Public -------------------------------------------------------
   
   public void setContainer(Container container)
   {
      this.container = (StatefulSessionContainer)container;
   }
   
   public  Container getContainer()
   {
      return container;
   }
   
   public Object invoke(Invocation mi) throws Exception
   {
      EnterpriseContext ctx = (EnterpriseContext) mi.getEnterpriseContext();
      if(ctx == null)
         throw new IllegalStateException("EJBContext is null");
        
      //Set the current security information
      ctx.setPrincipal(mi.getPrincipal());
      
      try
      {
         // Invoke through interceptors
         return getNext().invoke(mi);
      }
      finally
      { 
      }
   }
 
   public Object invokeHome(Invocation mi) throws Exception
   {
      Method getEJBObject = Handle.class.getMethod("getEJBObject", new Class[0]);
      
      //Invocation on the handle, we don't need a bean instance
      if (getEJBObject.equals(mi.getMethod()))
         return getNext().invokeHome(mi);
      
      EnterpriseContext ctx = (EnterpriseContext) mi.getEnterpriseContext();
      if(ctx == null)
         throw new IllegalStateException("EJBContext is null");
      
      //Set the current security information
      ctx.setPrincipal(mi.getPrincipal());
       
      try
      {
         // Invoke through interceptors
         return getNext().invokeHome(mi);
      }
      finally
      { 
      }
   } 
}
