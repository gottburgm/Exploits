package org.jboss.test.ejb3.jbas7883;

import java.util.Properties;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.mail.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

/**
 * 
 * @author Darran Lofthouse darran.lofthouse@jboss.com
 * @see https://issues.jboss.org/browse/JBAS-7883
 */
@Stateless
public class TestBean implements TestRemote
{

   @Resource(name = "mail1", mappedName = "mail/Mail1")
   private Session resourceOne;

   @Resource(name = "mail2", mappedName = "mail/Mail2")
   private Session resourceTwo;

   public String testResourceOne()
   {
      return getHost(resourceOne);
   }

   public String testResourceTwo()
   {
      return getHost(resourceTwo);
   }

   @Override
   public String testCall(String contextName, String jndiName)
   {
      try
      {
         Context ctx = new InitialContext();
         if (contextName != null && contextName.length() > 0)
         {
            ctx = (Context) ctx.lookup(contextName);
         }

         Session s = (Session) PortableRemoteObject.narrow(ctx.lookup(jndiName), Session.class);
         return getHost(s);
      }
      catch (Exception e)
      {
         throw new RuntimeException("Test Failed", e);
      }
   }

   private String getHost(final Session session)
   {
      Properties props = session.getProperties();
      
      return props.getProperty("mail.smtp.host");
   }

}
