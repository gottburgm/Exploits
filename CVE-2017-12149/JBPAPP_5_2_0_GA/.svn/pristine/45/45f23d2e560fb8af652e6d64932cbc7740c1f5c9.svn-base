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
package org.jboss.security.integration;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

/**
 * Bind into JNDI a bean
 * @author Anil.Saldhana@redhat.com
 * @since Apr 17, 2009
 */
public class JNDIBindingBean
{ 
   private Logger log = Logger.getLogger(JNDIBindingBean.class);
   
   private String ctx = null;
   private Object beanObject = null;
   
   public void setBean(Object bean)
   {
      this.beanObject = bean;
   }
   
   public void setJNDIContext(String ctx)
   {
      this.ctx = ctx;
   }
   
   public void start()
   {
      if(beanObject == null)
         throw new RuntimeException("Bean is null");
      if(ctx == null)
         throw new RuntimeException("JNDI Ctx name is null");
      try
      {
         InitialContext ic = new InitialContext();
         ic.bind(ctx, this.beanObject);
         log.debug("Bound in JNDI:" + this.beanObject.getClass().getCanonicalName() 
               + " in JNDI at " +ctx); 
      }
      catch (NamingException e)
      {
         throw new RuntimeException(e);
      }
   }

   public void create()
   { 
   }

   public void destroy()
   { 
   }

   public void stop()
   {  
      try
      {
         InitialContext ic = new InitialContext();
         ic.unbind(ctx);
         log.debug("Unbound in JNDI:" + this.beanObject.getClass().getCanonicalName() 
               + " in JNDI at " +ctx); 
      }
      catch (NamingException e)
      {
         throw new RuntimeException(e);
      }
   }
}