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
package org.jboss.test.security.container.auth.config;

import java.security.Principal;

import javax.security.auth.Subject;
import javax.servlet.http.HttpServlet;

import org.jboss.security.SimplePrincipal;
import org.jboss.security.auth.container.modules.SimpleServerAuthModule;
import org.jboss.security.auth.message.GenericMessageInfo; 
import org.jboss.test.JBossTestCase;

//$Id: SimpleServerAuthModuleTestCase.java 81036 2008-11-14 13:36:39Z dimitris@jboss.org $

/**
 *  Unit Test for SimpleServerAuthModule
 *  @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 *  @since  Dec 21, 2005 
 *  @version $Revision: 81036 $
 */
public class SimpleServerAuthModuleTestCase extends JBossTestCase
{ 
   private SimpleServerAuthModule module = null;
   
   public SimpleServerAuthModuleTestCase(String name)
   {
      super(name); 
   }
   
   public void testCreation()
   {
      Class[] clazzArr = new Class[] {HttpServlet.class};
      module = new SimpleServerAuthModule(clazzArr);
      assertNotNull("Supported class types != null", module.getSupportedMessageTypes());
      assertEquals(clazzArr, module.getSupportedMessageTypes());
      module = null;
   } 
   
   public void testValidateRequest() throws Exception
   {
      Class[] clazzArr = new Class[] {HttpServlet.class};
      module = new SimpleServerAuthModule(clazzArr);
      Subject sub = createSubject();
      module.validateRequest(new GenericMessageInfo(null,null), sub, sub );
      module = null;
   }
   
   public void testSecureResponse() throws Exception
   {
      //ToDO: Enhance this test
      module = new SimpleServerAuthModule(new Class[] {HttpServlet.class});
      Subject sub = createSubject();
      module.secureResponse(new GenericMessageInfo(null,null), sub); 
      module = null;
   }
   
   private Subject createSubject()
   {
      Subject subj = new Subject();
      Principal principal = new SimplePrincipal("dummy");
      subj.getPrincipals().add(principal);
      subj.getPublicCredentials().add("password");
      return subj;
   }
}
