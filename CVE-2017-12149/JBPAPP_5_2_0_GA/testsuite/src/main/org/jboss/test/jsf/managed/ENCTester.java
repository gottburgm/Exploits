/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.jsf.managed;

import java.net.URL;
import java.util.Hashtable;
import java.util.Iterator;

import javax.faces.FacesException;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnectionFactory;
import javax.jms.Topic;
import javax.mail.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.jboss.logging.Logger;
import org.jboss.test.web.mock.EntityHome;
import org.jboss.test.web.mock.StatelessSessionHome;
import org.jboss.test.web.mock.StatelessSessionLocalHome;

/**
 * A common test bean used by the enc servlets to validate their enc configuration.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 85945 $
 */
public class ENCTester
{
   private Logger log;
   ENCTester(Logger log)
   {
      this.log = log;
   }

   void testENC() throws FacesException
   {
      try
      {
         // Obtain the environment naming context.
         Context initCtx = new InitialContext();
         Hashtable env = initCtx.getEnvironment();
         Iterator keys = env.keySet().iterator();
         log.info("InitialContext.env:");
         while( keys.hasNext() )
         {
            Object key = keys.next();
            log.info("Key: "+key+", value: "+env.get(key));
         }
         Context myEnv = (Context) initCtx.lookup("java:comp/env");
         testEjbRefs(initCtx, myEnv);
         testJdbcDataSource(initCtx, myEnv);
         testMail(initCtx, myEnv);
         testJMS(initCtx, myEnv);
         testURL(initCtx, myEnv);
         testEnvEntries(initCtx, myEnv);
         testMessageDestinationRefs(initCtx, myEnv);
      }
      catch (NamingException e)
      {
         log.debug("Lookup failed", e);
         throw new FacesException("Lookup failed, ENC tests failed", e);
      }
      catch (JMSException e)
      {
         log.debug("JMS access failed", e);
         throw new FacesException("JMS access failed, ENC tests failed", e);
      }
      catch (RuntimeException e)
      {
         log.debug("Runtime error", e);
         throw new FacesException("Runtime error, ENC tests failed", e);
      }
   }

   private void testEnvEntries(Context initCtx, Context myEnv) throws NamingException
   {
      // Basic env values
      Integer i = (Integer) myEnv.lookup("Ints/i0");
      log.debug("Ints/i0 = " + i);
      i = (Integer) initCtx.lookup("java:comp/env/Ints/i1");
      log.debug("Ints/i1 = " + i);
      Float f = (Float) myEnv.lookup("Floats/f0");
      log.debug("Floats/f0 = " + f);
      f = (Float) initCtx.lookup("java:comp/env/Floats/f1");
      log.debug("Floats/f1 = " + f);
      String s = (String) myEnv.lookup("Strings/s0");
      log.debug("Strings/s0 = " + s);
      s = (String) initCtx.lookup("java:comp/env/Strings/s1");
      log.debug("Strings/s1 = " + s);
      s = (String) initCtx.lookup("java:comp/env/ejb/catalog/CatalogDAOClass");
      log.debug("ejb/catalog/CatalogDAOClass = " + s);
   }

   private void testEjbRefs(Context initCtx, Context myEnv) throws NamingException
   {
      //do lookup on bean specified without ejb-link
      Object ejb = initCtx.lookup("java:comp/env/ejb/bean3");
      if ((ejb instanceof StatelessSessionHome) == false)
         throw new NamingException("ejb/bean3 is not a StatelessSessionHome");
      log.debug("ejb/bean3 = " + ejb);


      ejb = initCtx.lookup("java:comp/env/ejb/CtsBmp");
      if ((ejb instanceof EntityHome) == false)
         throw new NamingException("ejb/CtsBmp is not a EntityHome");

      //lookup of local-ejb-ref bean specified without ejb-link
      ejb = initCtx.lookup("java:comp/env/ejb/local/bean3");
      if ((ejb instanceof StatelessSessionLocalHome) == false)
         throw new NamingException("ejb/local/bean3 is not a StatelessSessionLocalHome");
      log.debug("ejb/local/bean3 = " + ejb);
   }

   private void testJdbcDataSource(Context initCtx, Context myEnv) throws NamingException
   {
      // JDBC DataSource
      DataSource ds = (DataSource) myEnv.lookup("jdbc/DefaultDS");
      log.debug("jdbc/DefaultDS = " + ds);
   }

   private void testMail(Context initCtx, Context myEnv) throws NamingException
   {
      // JavaMail Session
      Session session = (Session) myEnv.lookup("mail/DefaultMail");
      log.debug("mail/DefaultMail = " + session);
   }

   private void testJMS(Context initCtx, Context myEnv) throws NamingException
   {
      // JavaMail Session
      QueueConnectionFactory qf = (QueueConnectionFactory) myEnv.lookup("jms/QueFactory");
      log.debug("jms/QueFactory = " + qf);
   }

   private void testURL(Context initCtx, Context myEnv) throws NamingException
   {
      // URLs
      URL home1 = (URL) myEnv.lookup("url/JBossHome");
      log.debug("url/JBossHome = " + home1);
      URL home2 = (URL) initCtx.lookup("java:comp/env/url/JBossHome");
      log.debug("url/JBossHome = " + home2);
      if( home1.equals(home2) == false )
         throw new NamingException("url/JBossHome != java:comp/env/url/JBossHome");
   }

   private void testMessageDestinationRefs(Context initCtx, Context myEnv) throws NamingException, JMSException
   {
      Object obj = myEnv.lookup("mdr/ConsumesLink");
      log.debug("mdr/ConsumesLink = " + obj);
      if ((obj instanceof Queue) == false)
         throw new RuntimeException("mdr/ConsumesLink is not a javax.jms.Queue");
      Queue queue = (Queue) obj;
      if ("QUEUE.testQueue".equals(queue.getQueueName()))
         throw new RuntimeException("Excepted QUEUE.testQueue, got " + queue);
      
      obj = myEnv.lookup("mdr/ProducesLink");
      log.debug("mdr/ProducesLink = " + obj);
      if ((obj instanceof Topic) == false)
         throw new RuntimeException("mdr/ProducesLink is not a javax.jms.Topic");
      Topic topic = (Topic) obj;
      if ("TOPIC.testTopic".equals(topic.getTopicName()))
         throw new RuntimeException("Excepted TOPIC.testTopic got " + topic);

      obj = myEnv.lookup("mdr/ConsumesProducesJNDIName");
      log.debug("mdr/ConsumesProducesJNDIName = " + obj);
      if ((obj instanceof Queue) == false)
         throw new RuntimeException("mdr/ConsumesProducesJNDIName is not a javax.jms.Queue");
      queue = (Queue) obj;
      if ("QUEUE.A".equals(queue.getQueueName()))
         throw new RuntimeException("Excepted QUEUE.A, got " + queue);
   }

}
