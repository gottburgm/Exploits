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
package org.jboss.mail;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Properties;

import javax.management.ObjectName;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;

import javax.naming.InitialContext;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.naming.NamingException;

import javax.mail.Session;
import javax.mail.PasswordAuthentication;
import javax.mail.Authenticator;

import org.jboss.system.ServiceMBeanSupport;

import org.jboss.util.naming.Util;

/**
 * MBean that gives support for JavaMail. Object of class javax.mail.Session will be bound
 * in JNDI with the name provided with method {@link #setJNDIName}.
 * 
 * Portions copyright 2010 Mark Lowe (mlowe@ebilling.it) - JBAS-7883
 *
 * @jmx:mbean name="jboss:type=Service,service=Mail"
 *            extends="org.jboss.system.ServiceMBean"
 *
 * @version <tt>$Revision: 110766 $</tt>
 * @author  <a href="mailto:simone.bordet@compaq.com">Simone Bordet</a>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author Scott.Stark@jboss.org
 * @author  <a href="mailto:mlowe@ebilling.it">Mark Lowe</a>
 */
public class MailService
   extends ServiceMBeanSupport
   implements MailServiceMBean
{
   public static final String JNDI_NAME = "java:/Mail";
   
   static final String ADDRESS_TYPE = "nns";
   
   /** */
   private String user;
   /** */
   private String password;
   /** */
   private String jndiName = JNDI_NAME;
   /** Whether lookups share a single session */
   private boolean sharedSession = false;
   private Element config;

   /** Object Name of the JSR-77 representation of this service */
   ObjectName mMail;

   /** save properties here */
   Properties ourProps = null;

   /**
    * User id used to connect to a mail server
    *
    * @see #setPassword
    *
    * @jmx:managed-attribute
    */
   public void setUser(final String user)
   {
      this.user = user;
   }

   /**
    * @jmx:managed-attribute
    */
   public String getUser()
   {
      return user;
   }

   /**
    * Password used to connect to a mail server
    *
    * @see #setUser
    *
    * @jmx:managed-attribute
    */
   public void setPassword(final String password)
   {
      this.password = password;
   }

   /**
    * Password is write only.
    */
   protected String getPassword()
   {
      return password;
   }

   /**
    * Configuration for the mail service.
    *
    * @jmx:managed-attribute
    */
   public Element getConfiguration()
   {
      return config;
   }

   /**
    * Configuration for the mail service.
    *
    * @jmx:managed-attribute
    */
   public void setConfiguration(final Element element)
   {
      config = element;
   }

   /** The JNDI name under which javax.mail.Session objects are bound.
    *
    * @jmx:managed-attribute
    */
   public void setJNDIName(final String name)
   {
      jndiName = name;
   }

   /**
    * @jmx:managed-attribute
    */
   public String getJNDIName()
   {
      return jndiName;
   }

   public boolean isSharedSession()
   {
      return sharedSession;
   }
   /**
    * Set whether a single mail session should be shared across all lookups
    * (sharedSession = true) or a new session created on each lookup
    * (sharedSession = false, the default).
    * @param sharedSession
    */
   public void setSharedSession(boolean sharedSession)
   {
      this.sharedSession = sharedSession;
   }

   /**
    * @jmx:managed-attribute
    */
   public String getStoreProtocol()
   {
      if (ourProps != null)
         return ourProps.getProperty("mail.store.protocol");
      else
         return null;
   }

   /**
    * @jmx:managed-attribute
    */
   public String getTransportProtocol()
   {
      if (ourProps != null)
         return ourProps.getProperty("mail.transport.protocol");
      else
         return null;
   }

   /**
    * @jmx:managed-attribute
    */
   public String getDefaultSender()
   {
      if (ourProps != null)
         return ourProps.getProperty("mail.from");
      else
         return null;
   }


   /**
    * @jmx:managed-attribute
    */
   public String getSMTPServerHost()
   {
      if (ourProps != null)
         return ourProps.getProperty("mail.smtp.host");
      else
         return null;
   }

   /**
    * @jmx:managed-attribute
    */
   public String getPOP3ServerHost()
   {
      if (ourProps != null)
         return ourProps.getProperty("mail.pop3.host");
      else
         return null;
   }

   protected ObjectName getObjectName(MBeanServer server, ObjectName name)
      throws MalformedObjectNameException
   {
      return name == null ? OBJECT_NAME : name;
   }

   protected void startService() throws Exception
   {
      // Setup password authentication
      final PasswordAuthentication pa = new PasswordAuthentication(getUser(), getPassword());
      Authenticator a = new Authenticator()
      {
         protected PasswordAuthentication getPasswordAuthentication()
         {
            return pa;
         }
      };

      Properties props = getProperties();

      // Finally bind a mail session
      bind(props, a);
      
      // now make the properties available
      ourProps = props;
   }

   protected Properties getProperties() throws Exception
   {
      Properties props = new Properties();
      if (config == null)
      {
         log.warn("No configuration specified; using empty properties map");
         return props;
      }

      NodeList list = config.getElementsByTagName("property");
      int len = list.getLength();

      for (int i = 0; i < len; i++)
      {
         Node node = list.item(i);

         switch (node.getNodeType())
         {
            case Node.ELEMENT_NODE:
               Element child = (Element) node;
               String name, value;

               // get the name
               if (child.hasAttribute("name"))
               {
                  name = child.getAttribute("name");
               }
               else
               {
                  log.warn("Ignoring invalid element; missing 'name' attribute: " + child);
                  break;
               }

               // get the value
               if (child.hasAttribute("value"))
               {
                  value = child.getAttribute("value");
               }
               else
               {
                  log.warn("Ignoring invalid element; missing 'value' attribute: " + child);
                  break;
               }

               if (log.isTraceEnabled())
               {
                  log.trace("setting property " + name + "=" + value);
               }
               props.setProperty(name, value);
               break;

            case Node.COMMENT_NODE:
               // ignore
               break;

            default:
               log.debug("ignoring unsupported node type: " + node);
               break;
         }
      }

      log.debug("Using properties: " + props);

      return props;
   }

   protected void stopService() throws Exception
   {
      unbind();
   }

   private void bind(Properties props, Authenticator auth) throws NamingException
   {
      String bindName = getJNDIName();
      SessionObjectFactory.setSessionFactoryInfo(bindName,props, auth,sharedSession);

      InitialContext ctx = new InitialContext();
      try
      {
         StringRefAddr addr = new StringRefAddr(ADDRESS_TYPE, bindName);
         Reference ref = new Reference(Session.class.getName(),
            addr,
            SessionObjectFactory.class.getName(),
            null);
         Util.bind(ctx, bindName, ref);
      }
      finally
      {
         ctx.close();
      }

      log.info("Mail Service bound to " + bindName);
   }

   private void unbind() throws NamingException
   {
      String bindName = getJNDIName();

      if (bindName != null)
      {
         InitialContext ctx = new InitialContext();
         try
         {
            ctx.unbind(bindName);
         }
         finally
         {
            ctx.close();
         }

         SessionObjectFactory.remove(bindName);
         log.info("Mail service '" + getJNDIName() + "' removed from JNDI");
      }
   }
}
