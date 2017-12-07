/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.test.refs.test;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.Properties;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;

import junit.framework.Test;

import org.jboss.ejb3.client.ClientLauncher;
import org.jboss.test.JBossTestCase;
import org.jboss.test.refs.client.Client;
import org.jboss.test.refs.ejbs2.StatelessTest;

/**
 * Tests of reference resolution. The ear consists of:
 * 
   refs.ear
    + refs-client.jar
      - @EJB(name = "ejb/StatefulBean", beanInterface = StatefulIF.class)
        -> StatefulBean
    + refs-ejb.jar
      - StatefulBean
         + @Stateful(name="StatefulBean", mappedName="refs/ejbs/StatefulBean")
         + @Remote({StatefulIF.class})
       - StatelessBean
         + @Stateless(name="StatelessBean", mappedName="refs/ejbs/StatelessBean")
         + @Remote({StatelessIF.class})
    + refs-ejb2.jar
      - StatelessBean2
        + @Stateless(mappedName="refs/ejbs2/StatelessBean")
        + @Remote(StatelessTest.class)
        + @EJB
          -> refs-ejb.jar#StatelessBean
        + @EJB(beanName="StatelessBean")
          -> refs-ejb.jar#StatelessBean
        + @EJB(beanInterface=StatelessIFExt.class)
          -> refs-ejb.jar#StatelessBean
     <!-- A collection of ejb2.1 beans with inter-jar refs -->
     + refs-ejb2x.jar
      - TargetSession (jndi-name=refs/TargetSession)
      - TargetSessionReferencer (jndi-name=refs/TargetSessionReferencer)
        + ejb-ref(ejb/TargetSessionViaEjbLink)
          -> TargetSession

 * @author Scott.Stark@jboss.org
 * @version $Revision: 85945 $
 */
public class RefResolutionUnitTestCase
   extends JBossTestCase
{
   public static Test suite() throws Exception
   {
      Test t1 = getDeploySetup(RefResolutionUnitTestCase.class, "refs.ear");
      return t1;
   }

   public RefResolutionUnitTestCase(String name)
   {
      super(name);
   }

   public void testSessionEJBRefs()
      throws Exception
   {
      InitialContext ctx = getInitialContext();
      try
      {
         Object ref = ctx.lookup("refs/ejbs2/StatelessBean");
         StatelessTest test = (StatelessTest) ref;
         test.validate();
      }
      catch(NameNotFoundException e)
      {
         getLog().info("+++ dump of global JNDI contents: ");
         dumpGlobalJndi("refs", 0);
         throw e;
      }
   }

   /**
    * 
    * @throws Throwable
    */
   public void testClientSessionRefByInterface()
      throws Throwable
   {
      String mainClassName = Client.class.getName();
      // JNDI name in jboss-client.xml or display-name in application-client.xml, or client jar simple name
      String applicationClientName = "refs-client";
      String name = new Date().toString();
      String args[] = { name };
      
      ClientLauncher launcher = new ClientLauncher();
      Properties env = getENCProps(applicationClientName);
      launcher.launch(mainClassName, applicationClientName, args, env);

      // Need to use the client class obtained from the launcher
      Class<?> clientClass = ClientLauncher.getTheMainClass();
      Class<?> empty[] = {};
      {
         Method getStatefulBean = clientClass.getDeclaredMethod("getStatefulBean", empty);
         Object bean = getStatefulBean.invoke(null, null);
         assertNotNull("StatefulBean was set", bean);
      }

      {
         Method getStatelessBean = clientClass.getDeclaredMethod("getStatelessBean", empty);
         Object bean = getStatelessBean.invoke(null, null);
         assertNotNull("StatelessBean was set", bean);
      }
   }

   private Properties getENCProps(String applicationClientName)
   {
      Properties env = new Properties();
      env.setProperty(Context.INITIAL_CONTEXT_FACTORY,
         "org.jnp.interfaces.NamingContextFactory");
      env.setProperty(Context.URL_PKG_PREFIXES, "org.jboss.naming.client");
      env.setProperty(Context.PROVIDER_URL, "jnp://" + getServerHost() + ":1099");
      env.setProperty("j2ee.clientName", applicationClientName);
      return env;
   }
   protected void dumpGlobalJndi(String name, int depth)
      throws Exception
   {
      InitialContext ctx = new InitialContext();
      StringBuilder tmp = new StringBuilder();
      for(int n = 0; n < depth; n ++)
         tmp.append('+');
      tmp.append(name);
      getLog().info(tmp.toString());
      NamingEnumeration<NameClassPair> iter = ctx.list(name);
      while(iter.hasMore())
      {
         NameClassPair ncp = iter.next();
         tmp.setLength(0);
         for(int n = 0; n <= depth; n ++)
            tmp.append('+');
         tmp.append(ncp.getName());
         getLog().info(tmp.toString());
         if(ncp.getClassName().equals("javax.naming.Context"))
            dumpGlobalJndi(name+"/"+ncp.getName(), depth+1);
      }
   }
}
