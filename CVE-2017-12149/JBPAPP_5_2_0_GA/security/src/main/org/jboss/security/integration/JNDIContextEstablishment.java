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

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.StringRefAddr;

import org.jboss.logging.Logger;
import org.jboss.managed.api.annotation.ManagementComponent;
import org.jboss.managed.api.annotation.ManagementObject;
import org.jboss.security.SecurityConstants;

/**
 *  Establishes the legacy java:/jaas/securityDomain
 *  to provide the SubjectSecurityManager implementation
 *  for legacy integration
 *  @author Anil.Saldhana@redhat.com
 *  @since  Sep 10, 2007 
 *  @version $Revision: 85945 $
 */
@ManagementObject(name = "JNDIContextEstablishment", componentType = @ManagementComponent(
                  type = "MCBean", subtype = "Security"))
public class JNDIContextEstablishment
{
   private Logger log = Logger.getLogger(JNDIContextEstablishment.class);
   
   protected String baseContext = SecurityConstants.JAAS_CONTEXT_ROOT;
    
   private String factoryName = SecurityDomainObjectFactory.class.getName();
   
   public JNDIContextEstablishment()
   {
      try
      {
         initialize();
      }
      catch (Exception e)
      {
         log.trace("Error in initialization of JNDIContextEstablishment",e);
      }
   }

   public void setBaseContext(String ctx) throws Exception
   {
      if(ctx == null)
         throw new IllegalArgumentException("ctx is null");
      this.baseContext = ctx;
      initialize();
   }
    
   public void setFactoryName(String factoryName)
   {
      this.factoryName = factoryName;
      try
      {
         initialize();
      }
      catch (Exception e)
      {
         log.trace("Error in initialization of JNDIContextEstablishment",e);
      }
   }

   private void initialize() throws Exception
   {
      Context ctx = new InitialContext(); 
      
      /* Create a mapping from the java:/jaas context to a SecurityDomainObjectFactory
      so that any lookup against java:/jaas/domain returns an instance of our
      security manager class.
      */
     RefAddr refAddr = new StringRefAddr("nns", "JSM");
     Reference ref = new Reference("javax.naming.Context", refAddr, factoryName, null);
     ctx.rebind(this.baseContext, ref); 
   } 
}
