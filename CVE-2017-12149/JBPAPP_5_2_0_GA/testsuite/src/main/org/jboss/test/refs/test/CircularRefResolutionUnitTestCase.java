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

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.naming.InitialContext;

import junit.framework.Test;

import org.jboss.ejb3.client.ClientLauncher;
import org.jboss.test.JBossTestCase;
import org.jboss.test.refs.client.Client;
import org.jboss.test.refs.common.EjbLinkIF;
import org.jboss.test.refs.ejbs2.StatelessTest;

/**
 * Tests of reference resolution. The ear consists of:
 * 
   ejb3_ejblink.ear
    + ejblink-client.jar
      - @EJB(name = "ejb/StatefulBean", beanInterface = StatefulIF.class)
        -> StatefulBean
    + one_ejb.jar
      - EjbLink1Bean
         + @Stateless(name = "EjbLink1Bean")
         + @Remote( { EjbLinkIF.class })
         + ejb-ref
           - ejb-name=ejb/EjbLink2Bean
           - ejb-link=two_ejb.jar#EjbLink2Bean
    + two_ejb.jar
       - EjbLink2Bean
         + @Stateless(name = "EjbLink2Bean")
         + @Remote( { EjbLinkIF.class })
         + ejb-ref
            - ejb-ref-name=ejb/EjbLink1Bean
            - ejb-link=one_ejb.jar#EjbLink1Bean
         + ejb-local-ref
            - ejb-ref-name=ejb/EjbLink3Bean
            - ejb-link=EjbLink3Bean         
       - EjbLink3Bean
         + @Stateless(name = "EjbLink3Bean")

 * @author Scott.Stark@jboss.org
 * @version $Revision: 85945 $
 */
public class CircularRefResolutionUnitTestCase
   extends JBossTestCase
{
   public static Test suite() throws Exception
   {
      Test t1 = getDeploySetup(CircularRefResolutionUnitTestCase.class, "refs.ear");
      return t1;
   }

   public CircularRefResolutionUnitTestCase(String name)
   {
      super(name);
   }

   public void testSessionEJBRefs()
      throws Exception
   {
      InitialContext ctx = getInitialContext();
      Object ref = ctx.lookup("refs/ejbs2/StatelessBean");
      StatelessTest test = (StatelessTest) ref;
      test.validate();
   }

   /**
    * 
    * @throws Throwable
    */
   public void testClientSessionRefByInterface()
      throws Throwable
   {
      String mainClassName = Client.class.getName();
      // must match JNDI name in jboss-client.xml or display-name in application-client.xml
      String applicationClientName = "refs-client";
      String name = new Date().toString();
      String args[] = { name };
      
      ClientLauncher launcher = new ClientLauncher();
      launcher.launch(mainClassName, applicationClientName, args);

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
}
