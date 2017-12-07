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
package org.jboss.monitor.alerts;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.management.Notification;
import javax.naming.InitialContext;

import org.jboss.monitor.JBossMonitorNotification;
import org.jboss.util.Strings;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 81038 $
 *
 **/
public class EmailAlertListener extends JBossAlertListener implements EmailAlertListenerMBean
{
   protected String messageTemplate;
   protected String subjectTemplate;
   protected String fromString;
   protected Address from;
   protected String replyToString;
   protected Address replyTo;
   protected Address[] to;
   protected HashSet toSet = new HashSet();


   public void handleNotification(Notification notification,
                                  Object handback)
   {
      if (!(notification instanceof JBossMonitorNotification)) return;
      Map substitutions = ((JBossMonitorNotification) notification).substitutionMap();
      String message = Strings.subst(messageTemplate, substitutions, "%(", ")");
      String subject = Strings.subst(subjectTemplate, substitutions,  "%(", ")");
      try
      {
         Session session = (Session) new InitialContext().lookup("java:/Mail");
         // create a message
         //
         Address replyToList[] = { replyTo };
         Message newMessage = new MimeMessage(session);
         newMessage.setFrom(from);
         newMessage.setReplyTo(replyToList);
         newMessage.setRecipients(Message.RecipientType.TO, to);
         newMessage.setSubject(subject);
         newMessage.setSentDate(new java.util.Date());
         newMessage.setText(message);

         // Send newMessage
          //
          Transport transport = session.getTransport();
          transport.connect();
          transport.sendMessage(newMessage, to);
      }
      catch (Exception ex)
      {
         ex.printStackTrace();
      }

   }

   public String getTo()
   {
      Iterator it = toSet.iterator();
      String output = "";
      while (it.hasNext())
      {
         output += it.next();
         if (it.hasNext()) output += ",";
      }
      return output;
   }

   protected void updateTo() throws AddressException
   {
      Iterator it = toSet.iterator();
      Address[] newTo = new Address[toSet.size()];
      for (int i = 0; it.hasNext(); i++)
      {
         String address = (String)it.next();
         newTo[i] = new InternetAddress(address);
      }
      to = newTo;
   }

   public void setTo(String t) throws AddressException
   {
      StringTokenizer tokenizer = new StringTokenizer(t, ",");
      while (tokenizer.hasMoreTokens())
      {
         String token = tokenizer.nextToken().trim();
         toSet.add(token);
      }
      updateTo();
   }

   public void addToAddress(String newAddress) throws AddressException
   {
      toSet.add(newAddress);
      updateTo();
   }

   public void removeToAddress(String removeAddress) throws AddressException
   {
      toSet.remove(removeAddress);
      updateTo();
   }

   public String getFrom()
   {
      return fromString;
   }

   public void setFrom(String f) throws AddressException
   {
      fromString = f;
      from = new InternetAddress(f);
   }

   public String getReplyTo()
   {
      return replyToString;
   }

   public void setReplyTo(String f) throws AddressException
   {
      replyToString = f;
      replyTo = new InternetAddress(f);
   }

   public String getMessageTemplate()
   {
      return messageTemplate;
   }

   public void setMessageTemplate(String messageTemplate)
   {
      this.messageTemplate = messageTemplate;
   }
   public String getSubjectTemplate()
   {
      return subjectTemplate;
   }

   public void setSubjectTemplate(String messageTemplate)
   {
      this.subjectTemplate = messageTemplate;
   }

}
