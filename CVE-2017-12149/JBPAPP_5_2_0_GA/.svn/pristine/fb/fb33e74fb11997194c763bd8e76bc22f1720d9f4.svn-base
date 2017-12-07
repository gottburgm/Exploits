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
package org.jboss.test.security.container.auth;
 
import java.util.Map;
 
import javax.security.auth.message.AuthException;
import javax.security.auth.message.config.AuthConfigFactory;
import javax.security.auth.message.config.AuthConfigProvider; 
import javax.security.auth.message.config.RegistrationListener;

/**
 *  Dummy Factory for unit tests for JSR-196 Implementation
 *  @author <mailto:Anil.Saldhana@jboss.org>Anil Saldhana
 *  @since  Dec 6, 2005
 */
public class TestAuthConfigFactory extends AuthConfigFactory
{ 
   public String[] detachListener(RegistrationListener listener, String layer, 
         String appContext)
   { 
      return null;
   }
 
   public AuthConfigProvider getConfigProvider(String layer, String appContext, 
         RegistrationListener listener)
   { 
      return null;
   }
 
   public RegistrationContext getRegistrationContext(String registrationID)
   { 
      return null;
   }
 
   public String[] getRegistrationIDs(AuthConfigProvider provider)
   { 
      return null;
   }
 
   public void refresh() throws AuthException, SecurityException
   { 
   }
 
   public String registerConfigProvider(String className, Map properties, 
         String layer, String appContext, String description) 
   throws AuthException, SecurityException
   { 
      return null;
   }
 
   public boolean removeRegistration(String registrationID)
   { 
      return false;
   }

   @Override
   public String registerConfigProvider(AuthConfigProvider provider, 
         String layer, String appContext, String description)
   { 
      return null;
   }  
}
