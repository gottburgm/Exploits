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
package org.jboss.test.naming.ejb;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.Topic;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.logging.Logger;

/** A bean that does nothing but access resources from the ENC
 to test ENC usage.

 @author Scott.Stark@jboss.org
 @version $Revision: 81036 $
 */
public class TestENCBean implements SessionBean
{
   Logger log = Logger.getLogger(getClass());

   private SessionContext sessionContext;

   public void ejbCreate() throws CreateException
   {
   }

// --- Begin SessionBean interface methods
   public void ejbActivate()
   {
   }

   public void ejbPassivate()
   {
   }

   public void ejbRemove()
   {
   }

   public void setSessionContext(SessionContext sessionContext) throws EJBException
   {
      this.sessionContext = sessionContext;
   }

// --- End SessionBean interface methods

   public long stressENC(long iterations)
   {
      long start = System.currentTimeMillis();
      for(int i = 0; i < iterations; i ++)
         accessENC();
      long end = System.currentTimeMillis();
      return end - start;
   }

   public void accessENC()
   {
      try
      {
         // Obtain the enterprise beans environment naming context.
         Context initCtx = new InitialContext();
         Context myEnv = (Context) initCtx.lookup("java:comp/env");
         Boolean hasFullENC = (Boolean) myEnv.lookup("hasFullENC");
         log.debug("ThreadContext CL = " + Thread.currentThread().getContextClassLoader());
         log.debug("hasFullENC = " + hasFullENC);
         if (hasFullENC.equals(Boolean.TRUE))
         {
            // This bean should have the full ENC setup of the ENCBean
            testEnvEntries(initCtx, myEnv);
            testEjbRefs(initCtx, myEnv);
            testJdbcDataSource(initCtx, myEnv);
            testMail(initCtx, myEnv);
            testJMS(initCtx, myEnv);
            testURL(initCtx, myEnv);
            testResourceEnvEntries(initCtx, myEnv);
            testMessageDestinationRefs(initCtx, myEnv);
         }
         else
         {
            // This bean should only have the hasFullENC env entry
            try
            {
               Integer i = (Integer) myEnv.lookup("Ints/i0");
               throw new EJBException("Was able to find java:comp/env/Ints/i0 in bean with hasFullENC = false");
            }
            catch (NamingException e)
            {
               // This is what we expect
            }
         }
      }
      catch (NamingException e)
      {
         log.error("failed", e);
         throw new EJBException(e.toString(true));
      }
      catch (JMSException e)
      {
         log.debug("failed", e);
         throw new EJBException(e);
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
      Short s0 = (Short) myEnv.lookup("Short/s0");
      log.debug("Short/s0 = " + s0);
      Long l0 = (Long) myEnv.lookup("Long/l0");
      log.debug("Long/s0 = " + l0);
      Double d0 = (Double) myEnv.lookup("Double/d0");
      log.debug("Double/s0 = " + d0);
      Byte b0 = (Byte) myEnv.lookup("Byte/b0");
      log.debug("Byte/b0 = " + b0);
      Character c0 = (Character) myEnv.lookup("Character/c0");
      log.debug("Character/c0 = " + c0);
   }

   private void testEjbRefs(Context initCtx, Context myEnv) throws NamingException
   {
      // EJB References
      Object ejb = myEnv.lookup("ejb/bean0");
      if ((ejb instanceof javax.ejb.EJBHome) == false)
         throw new NamingException("ejb/bean0 is not a javax.ejb.EJBHome");
      log.debug("ejb/bean0 = " + ejb);
      ejb = initCtx.lookup("java:comp/env/ejb/bean1");
      log.debug("ejb/bean1 = " + ejb);
      ejb = initCtx.lookup("java:comp/env/ejb/bean2");
      log.debug("ejb/bean2 = " + ejb);
      //ejb = initCtx.lookup("java:comp/env/ejb/remote-bean");
      ejb = null;
      log.debug("ejb/remote-bean = " + ejb);
   }

   private void testJdbcDataSource(Context initCtx, Context myEnv) throws NamingException
   {
      // JDBC DataSource
      Object obj = myEnv.lookup("jdbc/DefaultDS");
      if ((obj instanceof javax.sql.DataSource) == false)
         throw new NamingException("jdbc/DefaultDS is not a javax.sql.DataSource");
      log.debug("jdbc/DefaultDS = " + obj);
   }

   private void testMail(Context initCtx, Context myEnv) throws NamingException
   {
      // JavaMail Session
      Object obj = myEnv.lookup("mail/DefaultMail");
      if ((obj instanceof javax.mail.Session) == false)
         throw new NamingException("mail/DefaultMail is not a javax.mail.Session");
      log.debug("mail/DefaultMail = " + obj);
   }

   private void testJMS(Context initCtx, Context myEnv) throws NamingException
   {
      // JavaMail Session
      Object obj = myEnv.lookup("jms/QueFactory");
      if ((obj instanceof javax.jms.QueueConnectionFactory) == false)
         throw new NamingException("mail/DefaultMail is not a javax.jms.QueueConnectionFactory");
      log.debug("jms/QueFactory = " + obj);
   }

   private void testURL(Context initCtx, Context myEnv) throws NamingException
   {
      // JavaMail Session
      Object obj = myEnv.lookup("url/JBossHomePage");
      if ((obj instanceof java.net.URL) == false)
         throw new NamingException("url/JBossHomePage is not a java.net.URL");
      log.debug("url/SourceforgeHomePage = " + obj);

      obj = myEnv.lookup("url/SourceforgeHomePage");
      if ((obj instanceof java.net.URL) == false)
         throw new NamingException("url/SourceforgeHomePage is not a java.net.URL");
      log.debug("url/SourceforgeHomePage = " + obj);

      obj = myEnv.lookup("url/IndirectURL");
      if ((obj instanceof java.net.URL) == false)
         throw new NamingException("url/IndirectURL is not a java.net.URL");
      log.debug("url/IndirectURL = " + obj);
   }

   private void testResourceEnvEntries(Context initCtx, Context myEnv) throws NamingException
   {
      Object obj = myEnv.lookup("res/aQueue");
      if ((obj instanceof javax.jms.Queue) == false)
         throw new NamingException("res/aQueue is not a javax.jms.Queue");
      log.debug("res/aQueue = " + obj);
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
