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

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;

import junit.framework.Test;

import org.jboss.ejb3.client.ClientLauncher;
import org.jboss.test.JBossTestCase;
import org.jboss.test.JBossTestSetup;
import org.jboss.test.refs.clientorb.Client;

/**
 * Tests of reference resolution. The ear consists of:
   refs.ear
    + refs-clientorb.jar
      - @EJB(name = "ResourceOnMethodBean")
        -> ResourceOnMethodBean
      - @EJB(name = "ResourceOnFieldBean")
        -> ResourceOnFieldBean
      - @EJB(name = "ResourceOnFieldBean")
        -> ResourceOnFieldBean
     <!-- EJB3s with various @Resources -->
     + refs-resources-ejb.jar
      - @Stateless(ResourceOnMethodBean) (jndi-name=refs/resources/ResourceOnMethodBean)
      - @Remote( { ResourceIF.class })
        + @Resource(name = "sessionContext", description = "session context", type = SessionContext.class)
        + @Resource(name = "dataSource")
        + @Resource(name = "myDataSource2", type = DataSource.class, shareable = true, authenticationType = AuthenticationType.CONTAINER)
        + @Resource(name = "mailSession")
        + @Resource(name = "url")
        + @Resource(name = "queueConnectionFactory")
        + @Resource(name = "topicConnectionFactory")
        + @Resource(name = "connectionFactoryT")
        + @Resource(name = "connectionFactoryQ")
        + @Resource(name = "topic")
        + @Resource(name = "queue")
        + @Resource(description = "user transaction", name = "myUserTransaction", type = UserTransaction.class)
        + @Resource(name = "myOrb", type = ORB.class, description = "corba orb", shareable = false)
      - @Stateless(name = "ResourceOnFieldBean")
      - @Remote( { ResourceIF.class })
        + @Resource(name = "session", description = "session context", type = SessionContext.class)
        + @Resource(description = "user transaction", name = "myUserTransaction", type = UserTransaction.class)
        + @Resource(name = "dataSource", description = "<resource-ref>")
        + @Resource(name = "myDataSource2", type = DataSource.class, shareable = true, authenticationType = AuthenticationType.CONTAINER)
        + @Resource(name = "mailSession")
        + @Resource(name = "url")
        + @Resource(name = "queueConnectionFactory")
        + @Resource(name = "topicConnectionFactory")
        + @Resource(name = "connectionFactoryQ")
        + @Resource(name = "connectionFactoryT")
        + @Resource(name = "topic")
        + @Resource(name = "queue")
        + @Resource(name = "myOrb", type = ORB.class, description = "corba orb", shareable = false)
      - @Stateless(name = "ResourcesOnClassBean")
      - @Remote( { ResourceIF.class })
      - @Resources( {
         @Resource(description = "user transaction", name = "myUserTransaction", type = UserTransaction.class),
         @Resource(name = "dataSource", type = DataSource.class, shareable = true, authenticationType = AuthenticationType.CONTAINER, description = "<resource-ref>"),
         @Resource(name = "myDataSource2", type = DataSource.class, authenticationType = AuthenticationType.CONTAINER),
         @Resource(name = "mailSession", type = Session.class),
         @Resource(name = "url", type = URL.class),
         @Resource(name = "queueConnectionFactory", type = QueueConnectionFactory.class),
         @Resource(name = "topicConnectionFactory", type = TopicConnectionFactory.class),
         @Resource(name = "connectionFactoryQ", type = ConnectionFactory.class),
         @Resource(name = "connectionFactoryT", type = ConnectionFactory.class),
         @Resource(name = "queue", type = Queue.class),
         @Resource(name = "topic", type = Topic.class),
         @Resource(name = "myOrb", type = ORB.class, description = "corba orb", shareable = false)
      })
 * @author Scott.Stark@jboss.org
 * @version $Revision: 105321 $
 */
public class ResourceResolutionUnitTestCase
   extends JBossTestCase
{
   public static Test suite() throws Exception
   {
      return new JBossTestSetup(getDeploySetup(ResourceResolutionUnitTestCase.class, "refs-resources.ear"))
      {
         protected void setUp() throws Exception
         {
            super.setUp();
         }

          protected void tearDown() throws Exception
          {
             super.tearDown();
          }
      };
   }

   public ResourceResolutionUnitTestCase(String name)
   {
      super(name);
   }

   public void testClientORBResources()
      throws Throwable
   {
      String mainClassName = Client.class.getName();
      // JNDI name in jboss-client.xml or display-name in application-client.xml, or client jar simple name
      String applicationClientName = "refs-clientorb";
      String args[] = {};
      
      ClientLauncher launcher = new ClientLauncher();
      Properties env = getENCProps(applicationClientName);
      launcher.launch(mainClassName, applicationClientName, args, env);
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
