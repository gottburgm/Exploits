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
package org.jboss.test.security.test;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Set;

import javax.management.ObjectName;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.security.NestableGroup;
import org.jboss.security.NestablePrincipal;
import org.jboss.security.RunAsIdentity;
import org.jboss.security.SimpleGroup;
import org.jboss.security.SimplePrincipal;
import org.jboss.test.JBossTestCase;
import org.jboss.test.JBossTestSetup;
import org.jboss.test.util.web.HttpUtils;

//$Id: DeepCopySubjectUnitTestCase.java 81036 2008-11-14 13:36:39Z dimitris@jboss.org $

/**
 *  JBAS-2657: Add option to deep copy the authenticated subject sets
 *  
 *  Testcase that unit tests the cloneability of various JBossSX 
 *  Principal/Groups
 *  Also does a test of the serverside Subject deep copy via a mutable
 *  Principal
 *  @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 *  @since  Apr 4, 2006
 *  @version $Revision: 81036 $
 */
public class DeepCopySubjectUnitTestCase extends JBossTestCase
{ 
   public static String REALM = "JBossTest Servlets";
   
   public DeepCopySubjectUnitTestCase(String name)
   {
      super(name); 
   }
   
   /**
    * Test the cloneability of Nestable Principal
    * 
    * @throws Exception
    */
   public void testCloneNestablePrincipal() throws Exception
   {
      SimplePrincipal sp1 = new SimplePrincipal("sp1");
      SimplePrincipal sp2 = new SimplePrincipal("sp2");
      NestablePrincipal np = new NestablePrincipal("TestStack");
      //Add principals to the NestablePrincipal
      np.addMember(sp1);
      np.addMember(sp2);
      assertTrue("np.isMember(sp2)", np.isMember(sp2)); 
      
      //Clone the NestablePrincipal
      NestablePrincipal clonedNP = (NestablePrincipal)np.clone();
      
      //Remove a principal from the orig NestablePrincipal
      np.removeMember(sp2);
      //Only the active principal is valid
      assertFalse("np.isMember(sp2) == false", np.isMember(sp2)); 
      assertTrue("np.isMember(sp1)", np.isMember(sp1));
      //Check that the cloned NestablePrincipal is not affected
      assertTrue("clonedNP.isMember(sp2)", clonedNP.isMember(sp2)); 
   }
   
   /**
    * Test the Cloneability of NestableGroup
    * 
    * @throws Exception
    */
   public void testCloneNestableGroup() throws Exception
   {
      SimplePrincipal sp1 = new SimplePrincipal("sp1");
      SimplePrincipal sp2 = new SimplePrincipal("sp2");
      
      SimpleGroup sg1 = new SimpleGroup("sg1");
      SimpleGroup sg2 = new SimpleGroup("sg1");
      sg1.addMember(sp1);
      sg2.addMember(sp2);
      NestableGroup ng = new NestableGroup("TestGroup");
      //Add principals to the NestablePrincipal
      ng.addMember(sg1);
      ng.addMember(sg2);
      assertTrue("ng.isMember(sp2)", ng.isMember(sp2)); 
      
      //Clone the NestableGroup
      NestableGroup clonedNP = (NestableGroup)ng.clone();
      
      //Remove a group from the orig NestableGroup
      ng.removeMember(sg2);
      //Only the active principal is valid
      assertFalse("ng.isMember(sp2) == false", ng.isMember(sp2)); 
      assertTrue("ng.isMember(sp1)", ng.isMember(sp1));
      //Check that the cloned NestablePrincipal is not affected
      assertTrue("clonedNP.isMember(sp2)", clonedNP.isMember(sp2)); 
   }
   
   /**
    * Test the cloneability of Simple Group
    * 
    * @throws Exception
    */
   public void testCloneSimpleGroup() throws Exception
   {
      SimplePrincipal sp1 = new SimplePrincipal("sp1");
      SimplePrincipal sp2 = new SimplePrincipal("sp2");
      
      SimpleGroup sg = new SimpleGroup("sg1");
      sg.addMember(sp1);
      sg.addMember(sp2);
      assertTrue("sg.isMember(sp1)", sg.isMember(sp1));
      assertTrue("sg.isMember(sp2)", sg.isMember(sp2));
      
      //Clone
      SimpleGroup clonedSP = (SimpleGroup)sg.clone(); 
      sg.removeMember(sp2);
      
      //Only the active principal is valid
      assertFalse("sg.isMember(sp2) == false", sg.isMember(sp2)); 
      assertTrue("sg.isMember(sp1)", sg.isMember(sp1));
      //Check that the cloned SimpleGroup is not affected
      assertTrue("clonedSP.isMember(sp2)", clonedSP.isMember(sp2));  
   } 
   
   /**
    * Test the cloneability of RunAsIdentity
    * 
    * @throws Exception
    */
   public void testCloneRunAsIdentity() throws Exception
   { 
      SimplePrincipal sp1 = new SimplePrincipal("sp1");
      SimplePrincipal sp2 = new SimplePrincipal("sp2");
      RunAsIdentity ras = new RunAsIdentity("testRole", "testUser");
      //There is no need to test the set of run-as roles
      //as each time, a new HashSet is returned
      Set principalSet = ras.getPrincipalsSet();
      principalSet.add(sp1);
      principalSet.add(sp2);
      //Clone
      RunAsIdentity rasClone = (RunAsIdentity)ras.clone();
      principalSet.remove(sp1);
      assertFalse("principalSet.contains(sp1)==false",
            principalSet.contains(sp1));

      Set clonedPrincipalSet = rasClone.getPrincipalsSet();
      assertTrue("clonedPrincipalSet.contains(sp1)",
                     clonedPrincipalSet.contains(sp1));
      assertTrue("clonedPrincipalSet.contains(sp2)",
                     clonedPrincipalSet.contains(sp2)); 
   } 
   
   /**
    * Test the Deep Copy of Subjects by the JaasSecurityManager
    * via a test servlet deployed
    * 
    * @throws Exception
    */
   public void testSubjectCloning() throws Exception
   {
      flagDeepCopy(Boolean.FALSE);
      accessWeb(true);
      flagDeepCopy(Boolean.TRUE); 
      this.redeploy("deepcopy.ear");
      accessWeb(false);
      flagDeepCopy(Boolean.FALSE);
      this.redeploy("deepcopy.ear");
      accessWeb(true);
   }
   
   /**
    * Turn the deep copy of subjects on the JaasSecurityManagerService
    * ON or OFF based on the flag
    * 
    * @param flag Boolean.TRUE or Boolean.FALSE
    * @throws Exception
    */
   private void flagDeepCopy(Boolean flag) throws Exception
   { 
      this.getServer().invoke(new ObjectName("jboss.security:service=JaasSecurityManager"),
            "setDeepCopySubjectMode",new Object[]{flag}, new String[]{Boolean.TYPE.getName()});     
   }
   
   /**
    * Utility method that accesses the secured servlet 
    * @param shouldMatch Parameter to be passed to the web app
    * @throws Exception
    */
   private void accessWeb(boolean shouldMatch) throws Exception
   {
      //Access the SecureServletSecureEJB servlet
      String baseURL = HttpUtils.getBaseURL("scott", "echoman");
      //Test the Restricted servlet
      URL url = new URL(baseURL+"deepcopy/DeepCopyServlet?shouldMatch="+shouldMatch);  
      HttpUtils.accessURL(url, REALM, HttpURLConnection.HTTP_OK); 
   }
    
   public static Test suite() throws Exception
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new TestSuite(DeepCopySubjectUnitTestCase.class));

      // Create an initializer for the test suite
      Test wrapper = new JBossTestSetup(suite)
      {
         protected void setUp() throws Exception
         {
            super.setUp();
            redeploy("deepcopy.ear");
            // Make sure the security cache is clear
            flushAuthCache();
         }
         protected void tearDown() throws Exception
         {
            undeploy("deepcopy.ear");
            super.tearDown();
         }
      };
      return wrapper;
   }
}
