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
package org.jboss.test.security.servlets;

import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler; 
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import javax.security.jacc.PolicyContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.mx.util.MBeanServerLocator; 
import org.jboss.security.SimpleGroup;
import org.jboss.security.SimplePrincipal;
import org.jboss.security.SubjectSecurityManager;

//$Id: DeepCopySubjectServlet.java 81036 2008-11-14 13:36:39Z dimitris@jboss.org $

/**
 *  JBAS-2657: Add option to deep copy the authenticated subject sets
 *  Tests the Deep Copy capability for the subject sets
 *  @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 *  @since  Apr 5, 2006
 *  @version $Revision: 81036 $
 */
public class DeepCopySubjectServlet extends HttpServlet
{ 
   /** The serialVersionUID */
   private static final long serialVersionUID = -277574499645473218L; 
   
   private Principal anilPrincipal = TestPrincipal.getInstance();
   private Principal scottPrincipal = new SimplePrincipal("scott"); 
   
   protected void service(HttpServletRequest request, 
                          HttpServletResponse response) 
   throws ServletException, IOException
   { 
      boolean hashCodeShouldMatch = false; //Deep Cloning case
      
      int hashCodeOfAnilPrincipal = System.identityHashCode(anilPrincipal);
      
      InitialContext context;
      try
      {  
         String param = request.getParameter("shouldMatch");
         log("param="+param);
         if(param == null || param.length() == 0)
            param = "true"; 
         hashCodeShouldMatch = param.equals("true");
         
         log("hashCodeShouldMatch="+hashCodeShouldMatch);
         //Flush the Cache - this should not have any adverse effect on the testSubject 
         flushAuthenticationCache("deepcopy", anilPrincipal);
         context = new InitialContext();
         SubjectSecurityManager manager = (SubjectSecurityManager)context.lookup("java:comp/env/security/securityMgr");
         Subject testSubject = new Subject();
         //Do a validation so that the subject gets added to the cache for the test principal
         log("isValid="+manager.isValid(scottPrincipal,"echoman", testSubject)); 
         Subject authSubject = this.getAuthenticatedSubject(manager);
         log("AuthenticatedSubject["+authSubject+"]");  
         log("CopiedSubject["+testSubject+"]");  
         //Flush the Cache - this should not have any adverse effect on the testSubject
         flushAuthenticationCache("deepcopy", anilPrincipal);
         authSubject = this.getAuthenticatedSubject(manager);
         log("AuthenticatedSubject after flush["+authSubject+"]");  
         log("CopiedSubject after flush["+testSubject+"]"); 
         validateSubject(testSubject, hashCodeShouldMatch, hashCodeOfAnilPrincipal); 
      }
      catch (Exception e)
      { 
         throw new ServletException(e);
      }  
   } 
   
   private Subject getAuthenticatedSubject(SubjectSecurityManager mgr) 
   throws Exception
   {
      //First get the JACC Subject
      String SUBJECT_CONTEXT_KEY = "javax.security.auth.Subject.container"; 
      Subject subject = (Subject) PolicyContext.getContext(SUBJECT_CONTEXT_KEY);
         
      //Fallback
      if(subject == null && mgr != null)
      { 
         subject = mgr.getActiveSubject();
      }
      return subject;
   }
   
   /**
    * Validate that the subject contains the TestPrincipal and based on the 
    * passed parameter hashCodeShouldMatch, it will check the hashcode
    * of the object to match with the one that was originally placed in the
    * subject
    * 
    * @param ts Subject to Test
    * @param hashCodeShouldMatch Whether identityHashCode should match
    * @param hashCodeValueToCheck identity hashcode of the principal inserted in subject
    */
   private void validateSubject(Subject ts, boolean hashCodeShouldMatch,
         int hashCodeValueToCheck)
   { 
     boolean anilFound = false;  
     
     Set principalSet = ts.getPrincipals();
     if(principalSet == null || principalSet.isEmpty())
        throw new RuntimeException("Principal Set is null");
     Iterator iter = principalSet.iterator();
     while(iter.hasNext())
     {
        Principal p = (Principal)iter.next(); 
        if(p instanceof TestPrincipal)
        {
           verifyTestPrincipal(p,hashCodeShouldMatch,hashCodeValueToCheck); 
           anilFound = true;
        } 
     } 
     if(!anilFound)
        throw new RuntimeException("Test Principal not found");
   }
   
   /**
    * Validate theTestPrincipal based on the 
    * passed parameters hashCodeShouldMatch 
    * @see #validateSubject(Subject, boolean, int)
    * @param p Principal to Test
    * @param hashCodeShouldMatch Whether identityHashCode should match
    * @param hashCodeValueToCheck identity hashcode of the principal inserted in subject
    */
   private void verifyTestPrincipal(Principal p, boolean hashCodeShouldMatch,
         int hashCodeValueToCheck)
   {
      TestPrincipal tp = (TestPrincipal)p;
      int newHashCode = System.identityHashCode(tp);
      log("[hashCodeShouldMatch="+hashCodeShouldMatch+"::hashCodeValueToCheck="+ hashCodeValueToCheck
            + "::HashCode of TestPrincipal from copied subject="+ newHashCode+"]");
      if(hashCodeShouldMatch)
      {
         if(hashCodeValueToCheck != newHashCode)
            throw new RuntimeException("HashCodes of the TestPrincipal do not match");
      }else
      {
         if(hashCodeValueToCheck == newHashCode)
            throw new RuntimeException("HashCodes of the TestPrincipal are matching");
      }
      Map map = tp.getMap();
      if(map == null || map.isEmpty())
         throw new RuntimeException("Map is null");
      String value = (String)map.get("testKey");
      if(value == null)
         throw new RuntimeException("Value is null");
      if(!value.equals("testValue"))
         throw new RuntimeException("Value is not equal to testValue");
   }
   
   /**
    * Given the security domain and the Principal,
    * flush the authentication cache
    * 
    * @param principal
    * @throws JMException
    */
   private void flushAuthenticationCache(String domain, Principal principal) throws JMException
   {
      MBeanServer server = MBeanServerLocator.locateJBoss();
      ObjectName on = new ObjectName("jboss.security:service=JaasSecurityManager");
      Object[] obj = new Object[] {domain, principal};
      String[] sig = new String[]{"java.lang.String", "java.security.Principal"}; 
      
      //Flush the Authentication Cache
      server.invoke(on,"flushAuthenticationCache", obj, sig); 
   } 
   
   /**
    * 
    * A TestLoginModule.
    * All it does is it inserts a TestPrincipal that is mutable into the
    * subject
    * @author <a href="anil.saldhana@jboss.com">Anil Saldhana</a>
    * @version $Revision: 81036 $
    */
   public static class TestLoginModule implements LoginModule
   {   
      public TestLoginModule()
      {
         super(); 
      }

      public void initialize(Subject subject, CallbackHandler callbackHandler, 
            Map sharedState, Map options)
      {  
         TestPrincipal tp = TestPrincipal.getInstance();
         if(subject != null)
         {
            subject.getPrincipals().add(tp); 
         }
            
         SimpleGroup sg = new SimpleGroup("Roles");
         sg.addMember(new SimplePrincipal("Echo"));
         subject.getPrincipals().add(sg);
         SimpleGroup cg = new SimpleGroup("CallerPrincipal");
         cg.addMember(tp);
         subject.getPrincipals().add(cg);
      }

      public boolean login() throws LoginException
      {
          return true;
      }

      public boolean commit() throws LoginException
      {
          return true;
      }

      public boolean abort() throws LoginException
      {
          return true;
      }

      public boolean logout() throws LoginException
      {
         return true;
      } 
   }
   
   /**
    * 
    * A Mutable TestPrincipal.
    * 
    * @author <a href="anil.saldhana@jboss.com">Anil Saldhana</a>
    * @version $Revision: 81036 $
    */
   public static class TestPrincipal extends SimplePrincipal implements Cloneable
   {
      /** The serialVersionUID */
      private static final long serialVersionUID = -6160570085301760185L;
      
      private HashMap map = new HashMap();
 
      private static TestPrincipal _instance = new TestPrincipal("anil");
      
      public static TestPrincipal getInstance()
      {
         return _instance;
      }
      
      public TestPrincipal(String name)
      {
         super(name); 
         map.put("testKey","testValue");
      }
      
      public Map getMap()
      {
         return map;
      }
      
      public Object clone() throws CloneNotSupportedException 
      {
         TestPrincipal tp = (TestPrincipal)super.clone();
         tp.map = (HashMap)this.map.clone();
         return tp;
      }
 
      public boolean equals(Object another)
      { 
         return super.equals(another);
      } 
      
      public String toString()
      {
         return this.getName();
      }
   } 
}
