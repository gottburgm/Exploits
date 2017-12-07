package org.jboss.test.util.jms;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.naming.InitialContext;

import org.jboss.logging.Logger;
import org.jboss.test.jms.JMSTestAdmin;
import org.jboss.test.jms.TestRole;

public class JMSDestinationsUtil
{
   
   private static final Logger log = Logger.getLogger(JMSDestinationsUtil.class);
   
   
   public static void deployQueue(String queueName) throws Exception
   {
      JMSTestAdmin admin = JMSTestAdmin.getAdmin();
      admin.createQueue(queueName, new TestRole("guest", true, true, true));
   }
   
   public static void deployTopic(String topicName) throws Exception
   {
      JMSTestAdmin admin = JMSTestAdmin.getAdmin();
      admin.createTopic(topicName, new TestRole("guest", true, true, true));
   }
   
   /**
    * Historically at jboss, lots of tests will use these destinations since JBoss 3. 
    * This method is a tool to create the basic setting that most tests will use.  
    * @throws Exception
    */
   public static void setupBasicDestinations() throws Exception
   {
      JMSTestAdmin admin = JMSTestAdmin.getAdmin();
      admin.createTopic("securedTopic", new TestRole[]{
            new TestRole("publisher", true, true, false)});
      admin.createTopic("testTopic", new TestRole[]{
            new TestRole("guest", true, true, true),
            new TestRole("publisher", true, true, true),
            new TestRole("durpublisher", true, true, true)});
      admin.createTopic("testDurableTopic", new TestRole[]{
              new TestRole("guest", true, true, true),
              new TestRole("publisher", true, true, true),
              new TestRole("durpublisher", true, true, true)});

      admin.createQueue("testQueue", new TestRole[]{
            new TestRole("guest", true, true, true),
            new TestRole("publisher", true, true, true),
            new TestRole("durpublisher", true, true, true)});
      
      admin.createQueue("A", new TestRole("guest", true, true, true));
      admin.createQueue("B", new TestRole("guest", true, true, true));
      admin.createQueue("C", new TestRole("guest", true, true, true));
      admin.createQueue("D", new TestRole("guest", true, true, true));
      admin.createQueue("ex", new TestRole("guest", true, true, true));
   }
   
   /** This will remove all destinations created during the test */
   public static void destroyDestinations() throws Exception
   {
      JMSTestAdmin.getAdmin().destroyCreatedDestinations();
   }
   
   public static void destroyEveryDestination() throws Exception
   {
      destroyDestinations();
      JMSTestAdmin.getAdmin().destroyEveryDestination();
   }
   
   
   public static void purgeQueue(String jndi) throws Exception
   {
      InitialContext ctx = new InitialContext();
      Connection conn = null;
      try
      {
         Queue queue = (Queue)ctx.lookup(jndi);
         ConnectionFactory cf = (javax.jms.ConnectionFactory)ctx.lookup("ConnectionFactory");
         conn = cf.createConnection();
         Session sess = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
         conn.start();
         MessageConsumer cons = sess.createConsumer(queue);
         
         while (cons.receiveNoWait() != null);
         
         
      }
      finally
      {
         ctx.close();
         if (conn != null)
         {
            conn.close();
         }
      }
   }

   public static boolean isHornetQ()
   {
	  try
	  {
		  return JMSTestAdmin.getAdmin() instanceof org.jboss.test.jms.HornetQTestAdmin;
	  }
	  catch (Exception e)
	  {
		  log.warn(e.getMessage(), e);
		  e.printStackTrace(); // >> junit report
		  throw new RuntimeException("Can't initialize JMSTestAdmin", e);
	  }
   }
   

   public static boolean isJBM()
   {
	  try
	  {
		  return JMSTestAdmin.getAdmin() instanceof org.jboss.test.jms.LegacyJMSTestAdmin;
	  }
	  catch (Exception e)
	  {
		  log.warn(e.getMessage(), e);
		  e.printStackTrace(); // >> junit report
		  throw new RuntimeException("Can't initialize JMSTestAdmin", e);
	  }
   }
   
   

}
