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
package org.jboss.test.jacc.test.portal;

import java.security.Policy;
import java.security.Principal;
import java.security.ProtectionDomain;
import java.security.acl.Group;

import javax.security.auth.Subject;
import javax.security.jacc.PolicyConfiguration;
import javax.security.jacc.PolicyConfigurationFactory;
import javax.security.jacc.PolicyContext;

import org.jboss.security.SecurityAssociation;
import org.jboss.security.SecurityConstants;
import org.jboss.security.SimpleGroup;
import org.jboss.security.SimplePrincipal;
import org.jboss.security.jacc.DelegatingPolicy;
import org.jboss.security.jacc.SubjectPolicyContextHandler;
import org.jboss.test.JBossTestCase;

//$Id: BasePortalJaccTestCase.java 81036 2008-11-14 13:36:39Z dimitris@jboss.org $

/**
 *  JBPORTAL-565: Create Testcase for JACC Usage
 *  Base Class for the Portal Customized Jacc Use Cases
 *  @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 *  @since  Jan 18, 2006 
 *  @version $Revision: 81036 $
 */
public class BasePortalJaccTestCase extends JBossTestCase
{
   protected PolicyConfiguration pc = null;
   
   public BasePortalJaccTestCase(String name)
   {
      super(name); 
   }
   
   public void setUp()
   {
      Policy policy = Policy.getPolicy();
      if(policy instanceof DelegatingPolicy == false) 
      {
         //Set up the Policy 
         policy = new DelegatingPolicy();
      }
      //Set up the external permission types
      Class[] ext = new Class[] {PortalPermission.class};
      
      ((DelegatingPolicy)policy).setExternalPermissionTypes(ext);
      Policy.setPolicy(policy);
      //Have the policy load/update itself
      policy.refresh(); 
   } 
   
   protected void checkAdminAccess(Policy policy) throws Exception
   {
      //Check context access
      PortalObjectPermission callerperm = getPortalObjectPermission("/");
      ProtectionDomain pd = getProtectionDomain("testAdmin", "admin");
      assertTrue("Admin can access context?", policy.implies(pd, callerperm));
      
      //Check access to portal
      callerperm = getPortalObjectPermission("/default"); 
      assertTrue("Admin can access portal?", policy.implies(pd, callerperm));
      
      //Check access to page
      callerperm = getPortalObjectPermission("/default/default"); 
      assertTrue("Admin can access page?", policy.implies(pd, callerperm));
      
      //Check access to window
      callerperm = getPortalObjectPermission("/default/default/a"); 
      assertTrue("Admin can access window?", policy.implies(pd, callerperm)); 
   }
   
   protected void checkEmployeeAccess(Policy policy) throws Exception
   {
      //Check context access
      PortalObjectPermission callerperm = getPortalObjectPermission("/");
      ProtectionDomain pd = getProtectionDomain("testEmployee", "employee");
      assertFalse("Employee can't access context?", policy.implies(pd, callerperm));
      
      //Check access to portal
      callerperm = getPortalObjectPermission("/default"); 
      assertTrue("Employee can access portal?", policy.implies(pd, callerperm));
      
      //Check access to page
      callerperm = getPortalObjectPermission("/default/default"); 
      assertTrue("Employee can access page?", policy.implies(pd, callerperm));
      
      //Check access to window
      callerperm = getPortalObjectPermission("/default/default/a"); 
      assertTrue("Employee can access window?", policy.implies(pd, callerperm)); 
      
      callerperm = getPortalObjectPermission("/someportal"); 
      assertFalse("Employee cant access someportal?", policy.implies(pd, callerperm)); 
   }
   
   protected void checkBadEmployeeAccess(Policy policy) throws Exception
   {
      //Check context access
      PortalObjectPermission callerperm = getPortalObjectPermission("/");
      ProtectionDomain pd = getProtectionDomain("testBadEmployee", "bademployee");
      assertFalse("BadEmployee can't access context?", policy.implies(pd, callerperm));
      
      //Check access to portal
      callerperm = getPortalObjectPermission("/default"); 
      assertFalse("BadEmployee can't access portal?", policy.implies(pd, callerperm));
      
      //Check access to page
      callerperm = getPortalObjectPermission("/default/default"); 
      assertFalse("BadEmployee can't access page?", policy.implies(pd, callerperm));
      
      //Check access to window
      callerperm = getPortalObjectPermission("/default/default/a"); 
      assertFalse("BadEmployee can't access window?", policy.implies(pd, callerperm)); 
   }
   
   protected void checkJanitorAccess(Policy policy) throws Exception
   {
      //Check context access
      PortalObjectPermission callerperm = getPortalObjectPermission("/");
      ProtectionDomain pd = getProtectionDomain("testJanitor", "janitor");
      assertFalse("Janitor can't access context?", policy.implies(pd, callerperm));
      
      //Check access to portal
      callerperm = getPortalObjectPermission("/default"); 
      assertFalse("Janitor can't access portal?", policy.implies(pd, callerperm));
      
      //Check access to page
      callerperm = getPortalObjectPermission("/default/default"); 
      assertFalse("Janitor can't access page?", policy.implies(pd, callerperm));
      
      //Check access to window
      callerperm = getPortalObjectPermission("/default/default/a"); 
      assertTrue("Janitor can access window?", policy.implies(pd, callerperm)); 
   } 
   
   protected ProtectionDomain getProtectionDomain(String username, String role) throws Exception
   {
      Group gp = new SimpleGroup("Roles");
      gp.addMember(new SimplePrincipal(role));
      Principal sp = new SimplePrincipal(username);
      prepareAndSetAuthenticatedSubject(sp,gp);
      return new ProtectionDomain(null,null,null,new Principal[] { sp,gp} );
   } 
   
   protected PolicyConfiguration getPolicyConfiguration(String ctx) throws Exception 
   { 
      PolicyConfigurationFactory pcf = PolicyConfigurationFactory.getPolicyConfigurationFactory(); 
      pc = pcf.getPolicyConfiguration(ctx, true);
      assertNotNull("PolicyConfiguration is not null", pc);
      return pc;
   }
   
   protected PortalObjectPermission getPortalObjectPermission(String uri)
   {
      return new PortalObjectPermission(uri, "view");
   }
   
   /**
    * This method prepares a Subject with the principal and the group
    * passed as parameters and registers the Subject with the
    * PolicyContext
    * 
    * @param p
    * @param gp
    * @throws Exception
    */
   private void prepareAndSetAuthenticatedSubject(Principal p , Group gp) throws Exception
   {
      Subject subject = new Subject();
      subject.getPrincipals().add(p);
      subject.getPrincipals().add(gp);
      
      SecurityAssociation.setSubject(subject);
      //Register the default active Subject PolicyContextHandler
      SubjectPolicyContextHandler handler = new SubjectPolicyContextHandler();
      PolicyContext.registerHandler(SecurityConstants.SUBJECT_CONTEXT_KEY,
         handler, true);
   } 

}
