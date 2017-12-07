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

import java.util.Properties;
import java.util.HashSet;
import java.security.Principal;
import java.net.HttpURLConnection;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.ByteArrayInputStream;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import junit.framework.Test;
import org.jboss.test.JBossTestCase;
import org.jboss.test.util.web.HttpUtils;
import org.jboss.test.security.interfaces.SecuredServiceRemoteHome;
import org.jboss.test.security.interfaces.SecuredServiceRemote;
import org.jboss.test.security.interfaces.CallerInfo;
import org.jboss.security.SecurityAssociation;
import org.jboss.security.SimplePrincipal;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.methods.PostMethod;


/** Tests of the JACC subject policy context handler state and consistency
 with the container caller principal, isCallerInRole methods.

 @author Scott.Stark@jboss.org
 @version $Revision: 81036 $
 */
public class SubjectContextUnitTestCase
   extends JBossTestCase
{
   public SubjectContextUnitTestCase(String name)
   {
      super(name);
   }

   /**
    Access an unchecked method with a valid login that calls the same method
    on another bean using a run-as role.

    @throws Exception
    */
   public void testPublicMethod() throws Exception
   {
      log.debug("+++ testPublicMethod()");
      Properties env = new Properties();
      env.setProperty(Context.INITIAL_CONTEXT_FACTORY,
         "org.jboss.security.jndi.JndiLoginInitialContextFactory");
      env.setProperty(Context.SECURITY_PRINCIPAL, "jduke");
      env.setProperty(Context.SECURITY_CREDENTIALS, "theduke");
      InitialContext ctx = new InitialContext(env);
      Object obj = ctx.lookup("jacc/Secured");
      obj = PortableRemoteObject.narrow(obj, SecuredServiceRemoteHome.class);
      SecuredServiceRemoteHome home = (SecuredServiceRemoteHome) obj;
      log.debug("Found SecuredServiceRemoteHome");
      SecuredServiceRemote bean = home.create();
      log.debug("Created SecuredServiceRemote");

      Principal callerIdentity = new SimplePrincipal("jduke");
      Principal runAsIdentity = new SimplePrincipal("runAsUser");
      HashSet expectedCallerRoles = new HashSet();
      expectedCallerRoles.add("groupMemberCaller");
      expectedCallerRoles.add("userCaller");
      expectedCallerRoles.add("allAuthCaller");
      expectedCallerRoles.add("webUser");
      HashSet expectedRunAsRoles = new HashSet();
      expectedRunAsRoles.add("identitySubstitutionCaller");
      expectedRunAsRoles.add("extraRunAsRole");
      CallerInfo info = new CallerInfo(callerIdentity, runAsIdentity,
         expectedCallerRoles, expectedRunAsRoles);
      bean.publicMethod(info);
      bean.remove();
   }
   public void testAllAuthMethod() throws Exception
   {
      log.debug("+++ testAllAuthMethod()");
      Properties env = new Properties();
      env.setProperty(Context.INITIAL_CONTEXT_FACTORY,
         "org.jboss.security.jndi.JndiLoginInitialContextFactory");
      env.setProperty(Context.SECURITY_PRINCIPAL, "jduke");
      env.setProperty(Context.SECURITY_CREDENTIALS, "theduke");
      InitialContext ctx = new InitialContext(env);
      Object obj = ctx.lookup("jacc/Secured");
      obj = PortableRemoteObject.narrow(obj, SecuredServiceRemoteHome.class);
      SecuredServiceRemoteHome home = (SecuredServiceRemoteHome) obj;
      log.debug("Found SecuredServiceRemoteHome");
      SecuredServiceRemote bean = home.create();
      log.debug("Created SecuredServiceRemote");

      Principal callerIdentity = new SimplePrincipal("jduke");
      Principal runAsIdentity = new SimplePrincipal("runAsUser");
      HashSet expectedCallerRoles = new HashSet();
      expectedCallerRoles.add("groupMemberCaller");
      expectedCallerRoles.add("userCaller");
      expectedCallerRoles.add("allAuthCaller");
      expectedCallerRoles.add("webUser");
      HashSet expectedRunAsRoles = new HashSet();
      expectedRunAsRoles.add("identitySubstitutionCaller");
      expectedRunAsRoles.add("extraRunAsRole");
      CallerInfo info = new CallerInfo(callerIdentity, runAsIdentity,
         expectedCallerRoles, expectedRunAsRoles);
      bean.allAuthMethod(info);
      bean.remove();
   }
   public void testUserMethod() throws Exception
   {
      log.debug("+++ testUserMethod()");
      Properties env = new Properties();
      env.setProperty(Context.INITIAL_CONTEXT_FACTORY,
         "org.jboss.security.jndi.JndiLoginInitialContextFactory");
      env.setProperty(Context.SECURITY_PRINCIPAL, "jduke");
      env.setProperty(Context.SECURITY_CREDENTIALS, "theduke");
      InitialContext ctx = new InitialContext(env);
      Object obj = ctx.lookup("jacc/Secured");
      obj = PortableRemoteObject.narrow(obj, SecuredServiceRemoteHome.class);
      SecuredServiceRemoteHome home = (SecuredServiceRemoteHome) obj;
      log.debug("Found SecuredServiceRemoteHome");
      SecuredServiceRemote bean = home.create();
      log.debug("Created SecuredServiceRemote");

      Principal callerIdentity = new SimplePrincipal("jduke");
      Principal runAsIdentity = new SimplePrincipal("runAsUser");
      HashSet expectedCallerRoles = new HashSet();
      expectedCallerRoles.add("groupMemberCaller");
      expectedCallerRoles.add("userCaller");
      expectedCallerRoles.add("allAuthCaller");
      expectedCallerRoles.add("webUser");
      HashSet expectedRunAsRoles = new HashSet();
      expectedRunAsRoles.add("identitySubstitutionCaller");
      expectedRunAsRoles.add("extraRunAsRole");
      CallerInfo info = new CallerInfo(callerIdentity, runAsIdentity,
         expectedCallerRoles, expectedRunAsRoles);
      bean.userMethod(info);
      bean.remove();
   }
   public void testGroupMemberMethod() throws Exception
   {
      log.debug("+++ testGroupMemberMethod()");
      Properties env = new Properties();
      env.setProperty(Context.INITIAL_CONTEXT_FACTORY,
         "org.jboss.security.jndi.JndiLoginInitialContextFactory");
      env.setProperty(Context.SECURITY_PRINCIPAL, "jduke");
      env.setProperty(Context.SECURITY_CREDENTIALS, "theduke");
      InitialContext ctx = new InitialContext(env);
      Object obj = ctx.lookup("jacc/Secured");
      obj = PortableRemoteObject.narrow(obj, SecuredServiceRemoteHome.class);
      SecuredServiceRemoteHome home = (SecuredServiceRemoteHome) obj;
      log.debug("Found SecuredServiceRemoteHome");
      SecuredServiceRemote bean = home.create();
      log.debug("Created SecuredServiceRemote");

      Principal callerIdentity = new SimplePrincipal("jduke");
      Principal runAsIdentity = new SimplePrincipal("runAsUser");
      HashSet expectedCallerRoles = new HashSet();
      expectedCallerRoles.add("groupMemberCaller");
      expectedCallerRoles.add("userCaller");
      expectedCallerRoles.add("allAuthCaller");
      expectedCallerRoles.add("webUser");
      HashSet expectedRunAsRoles = new HashSet();
      expectedRunAsRoles.add("identitySubstitutionCaller");
      expectedRunAsRoles.add("extraRunAsRole");
      CallerInfo info = new CallerInfo(callerIdentity, runAsIdentity,
         expectedCallerRoles, expectedRunAsRoles);
      bean.groupMemberMethod(info);
      bean.remove();
   }
   public void testRunAsMethod() throws Exception
   {
      log.debug("+++ testRunAsMethod()");
      Properties env = new Properties();
      env.setProperty(Context.INITIAL_CONTEXT_FACTORY,
         "org.jboss.security.jndi.JndiLoginInitialContextFactory");
      env.setProperty(Context.SECURITY_PRINCIPAL, "jduke");
      env.setProperty(Context.SECURITY_CREDENTIALS, "theduke");
      InitialContext ctx = new InitialContext(env);
      Object obj = ctx.lookup("jacc/Secured");
      obj = PortableRemoteObject.narrow(obj, SecuredServiceRemoteHome.class);
      SecuredServiceRemoteHome home = (SecuredServiceRemoteHome) obj;
      log.debug("Found SecuredServiceRemoteHome");
      SecuredServiceRemote bean = home.create();
      log.debug("Created SecuredServiceRemote");

      Principal callerIdentity = new SimplePrincipal("jduke");
      Principal runAsIdentity = new SimplePrincipal("runAsUser");
      HashSet expectedCallerRoles = new HashSet();
      expectedCallerRoles.add("groupMemberCaller");
      expectedCallerRoles.add("userCaller");
      expectedCallerRoles.add("allAuthCaller");
      expectedCallerRoles.add("webUser");
      HashSet expectedRunAsRoles = new HashSet();
      expectedRunAsRoles.add("identitySubstitutionCaller");
      expectedRunAsRoles.add("extraRunAsRole");
      CallerInfo info = new CallerInfo(callerIdentity, runAsIdentity,
         expectedCallerRoles, expectedRunAsRoles);
      bean.runAsMethod(info);
      bean.remove();
   }
   public void testUnprotectedEjbMethod() throws Exception
   {
      log.debug("+++ testUnprotectedEjbMethod()");
      SecurityAssociation.clear();
      InitialContext ctx = new InitialContext();
      Object obj = ctx.lookup("jacc/Secured");
      obj = PortableRemoteObject.narrow(obj, SecuredServiceRemoteHome.class);
      SecuredServiceRemoteHome home = (SecuredServiceRemoteHome) obj;
      log.debug("Found SecuredServiceRemoteHome");
      SecuredServiceRemote bean = home.create();
      log.debug("Created SecuredServiceRemote");

      Principal callerIdentity = new SimplePrincipal("guest");
      Principal runAsIdentity = new SimplePrincipal("runAsUser");
      HashSet expectedCallerRoles = new HashSet();
      HashSet expectedRunAsRoles = new HashSet();
      expectedRunAsRoles.add("identitySubstitutionCaller");
      expectedRunAsRoles.add("extraRunAsRole");
      CallerInfo info = new CallerInfo(callerIdentity, runAsIdentity,
         expectedCallerRoles, expectedRunAsRoles);
      bean.unprotectedEjbMethod(info);
      bean.remove();
   }

   public void testUnprotectedEjbMethodViaServlet() throws Exception
   {
      log.debug("+++ testUnprotectedEjbMethodViaServlet()");
      SecurityAssociation.clear();

      Principal callerIdentity = new SimplePrincipal("guest");
      Principal runAsIdentity = new SimplePrincipal("runAsUser");
      HashSet expectedCallerRoles = new HashSet();
      HashSet expectedRunAsRoles = new HashSet();
      expectedRunAsRoles.add("identitySubstitutionCaller");
      expectedRunAsRoles.add("extraRunAsRole");
      CallerInfo info = new CallerInfo(callerIdentity, runAsIdentity,
         expectedCallerRoles, expectedRunAsRoles);

      String baseURLNoAuth = HttpUtils.getBaseURLNoAuth();
      PostMethod formPost = new PostMethod(baseURLNoAuth+"subject-context/unrestricted/RunAsServlet");
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(baos);
      oos.writeObject("unprotectedEjbMethod");
      oos.writeObject(info);
      oos.close();
      log.info("post content length: "+baos.toByteArray().length);
      ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
      formPost.setRequestBody(bais);
      HttpClient httpConn = new HttpClient();
      int responseCode = httpConn.executeMethod(formPost);
      assertTrue("POST OK("+responseCode+")", responseCode == HttpURLConnection.HTTP_OK);
   }

   public void testUserMethodViaServlet() throws Exception
   {
      log.debug("+++ testUserMethodViaServlet()");
      SecurityAssociation.clear();

      Principal callerIdentity = new SimplePrincipal("jduke");
      Principal runAsIdentity = new SimplePrincipal("runAsUser");
      HashSet expectedCallerRoles = new HashSet();
      expectedCallerRoles.add("groupMemberCaller");
      expectedCallerRoles.add("userCaller");
      expectedCallerRoles.add("allAuthCaller");
      expectedCallerRoles.add("webUser");
      HashSet expectedRunAsRoles = new HashSet();
      expectedRunAsRoles.add("identitySubstitutionCaller");
      expectedRunAsRoles.add("extraRunAsRole");
      CallerInfo info = new CallerInfo(callerIdentity, runAsIdentity,
         expectedCallerRoles, expectedRunAsRoles);

      String baseURL = HttpUtils.getBaseURL("jduke", "theduke");
      PostMethod formPost = new PostMethod(baseURL+"subject-context/restricted/RunAsServlet");
      formPost.setDoAuthentication(true);
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(baos);
      oos.writeObject("userMethod");
      oos.writeObject(info);
      oos.close();
      log.info("post content length: "+baos.toByteArray().length);
      ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
      formPost.setRequestBody(bais);
      String host = formPost.getHostConfiguration().getHost();
      HttpClient httpConn = new HttpClient();
      HttpState state = httpConn.getState();
      state.setAuthenticationPreemptive(true);
      UsernamePasswordCredentials upc = new UsernamePasswordCredentials("jduke", "theduke");
      state.setCredentials("JBossTest Servlets", host, upc);
      
      int responseCode = httpConn.executeMethod(formPost);
      assertTrue("POST OK("+responseCode+")", responseCode == HttpURLConnection.HTTP_OK);
   }

   public static Test suite() throws Exception
   {
      return JBossTestCase.getDeploySetup(SubjectContextUnitTestCase.class, "subject-context.ear");
   }

}
