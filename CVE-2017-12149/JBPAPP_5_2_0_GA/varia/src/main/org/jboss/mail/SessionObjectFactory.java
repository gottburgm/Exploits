/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.mail;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Session;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;

/**
 * A jndi ObjectFactory implementation that creates a new Session from the
 * static class information on each getObjectInstance call.
 *
 * Portions copyright 2010 Mark Lowe (mlowe@ebilling.it) - JBAS-7883
 * 
 * @author Scott.Stark@jboss.org
 * @author  <a href="mailto:mlowe@ebilling.it">Mark Lowe</a>
 * @author  <a href="mailto:darran.lofthouse@jboss.com">Darran Lofthouse</a>
 * @version $Revision: 110766 $
 */
public class SessionObjectFactory implements ObjectFactory
{

   private static final Map<String, MailSessionConfig> mailSessions = new HashMap<String, MailSessionConfig>();

   static void setSessionFactoryInfo(String bindName, Properties props, Authenticator auth, boolean shareSessionInstance)
   {
      mailSessions.put(bindName, new MailSessionConfig(props, auth, shareSessionInstance));
   }

   static void remove(String bindName)
   {
      mailSessions.remove(bindName);
   }

   public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment)
         throws Exception
   {
      String nameStr = null;

      if (obj != null && obj instanceof Reference)
      {
         Reference ref = (Reference) obj;
         RefAddr address = ref.get(MailService.ADDRESS_TYPE);
         if (address != null)
         {
            String addressValue = address.getContent().toString();
            if (addressValue != null)
            {
               nameStr = addressValue;
            }
         }
      }

      if (nameStr == null && name != null)
      {
         nameStr = name.toString();
      }

      if (nameStr == null)
      {
         if (mailSessions.containsKey(MailService.JNDI_NAME))
         {
            nameStr = MailService.JNDI_NAME;
         }
         else
         {
            throw new IllegalStateException("No default mail session found and no alternative jndi name provided.");
         }
      }

      MailSessionConfig config = mailSessions.get(nameStr);

      return config.getSession();
   }

   /**
    * 
    */
   private static class MailSessionConfig
   {

      private Properties properties;

      private Authenticator auth;

      private boolean shareSessionInstance = false;

      private Session session;

      protected MailSessionConfig(Properties properties, Authenticator auth, boolean shareSessionInstance)
      {
         this.properties = properties;
         this.auth = auth;
         this.shareSessionInstance = shareSessionInstance;
      }

      public Authenticator getAuth()
      {
         return auth;
      }

      public Properties getProperties()
      {
         return properties;
      }

      public Session getSession()
      {
         //when share session instance is set return a cached session
         if (isShareSessionInstance())
         {
            if (session == null)
            {
               session = Session.getInstance(getProperties(), getAuth());
            }
            return session;
         }

         return Session.getInstance(getProperties(), getAuth());
      }

      public boolean isShareSessionInstance()
      {
         return shareSessionInstance;
      }

   }

}
