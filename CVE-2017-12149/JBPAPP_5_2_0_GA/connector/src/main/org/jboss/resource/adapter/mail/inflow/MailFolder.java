/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.resource.adapter.mail.inflow;

import java.util.Iterator;
import java.util.Properties;

import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Message;

/** An encapsulation of a mail store folder used by the MailActivation.run to
 * poll and retrieve new messages.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 73611 $
 */
public abstract class MailFolder implements Iterator
{
   private Session session;
   private Store store;
   private Folder folder;
   private String mailServer;
   private String folderName;
   private String userName;
   private String password;
   private int port;
   private boolean debug;
   private boolean starttls;
   private Properties sessionProps;
   
   private Message[] msgs = {};
   private int messagePosition;

   public MailFolder(MailActivationSpec spec)
   {
      mailServer = spec.getMailServer();
      folderName = spec.getMailFolder();
      userName = spec.getUserName();
      password = spec.getPassword();
      debug = spec.isDebug();
      starttls = spec.isStarttls();
      port = spec.getPort();

      sessionProps = new Properties();
      sessionProps.setProperty("mail.transport.protocol", "smtp");
      sessionProps.setProperty("mail.smtp.host", mailServer);
      sessionProps.setProperty("mail.debug", debug + "");
      
      // JavaMail doesn't implement POP3 STARTTLS
      sessionProps.setProperty("mail.imap.starttls.enable", starttls + "");
   }      

   public void open()
      throws Exception
   {
      // Get a session object
      session = Session.getInstance(sessionProps);
      session.setDebug(debug);
      // Get a store object
      store = openStore(session);
      if (port == 0) 
      {
    	  store.connect(mailServer, userName, password);
      }
      else 
      {
    	  store.connect(mailServer, port, userName, password);
      }
      folder = store.getFolder(folderName);

      if (folder == null || (!this.folder.exists()))
      {
         MessagingException e = new MessagingException("Failed to find folder: " + folderName);
         throw e;
      }

      folder.open(Folder.READ_WRITE);
      msgs = getMessages(folder);
   }
   
   protected abstract Store openStore(Session session) throws NoSuchProviderException;
   
   protected abstract void closeStore(boolean success, Store store, Folder folder) throws MessagingException;
   
   protected abstract Message[] getMessages(Folder folder) throws MessagingException;
   
   protected abstract void markMessageSeen(Message message) throws MessagingException;

   public void close() throws MessagingException
   {
      close(true);
   }

	public boolean hasNext() {
		return messagePosition < msgs.length; 
	}
	
	public Object next() {
		try {
			Message m = msgs[messagePosition++];
			markMessageSeen(m);
			return m;
		} catch (MessagingException e) {
			close(false);
			throw new RuntimeException(e);
		}
	}
	
	public void remove() {
		throw new UnsupportedOperationException();
	}
	
	protected void close(boolean checkSuccessful) {
		try {
			closeStore(checkSuccessful, store, folder);
		} catch (MessagingException e) {
			throw new RuntimeException("Error closing mail store", e);
		}
	}
	
	public static MailFolder getInstance(MailActivationSpec mailActivationSpec) {
		if ("pop3".equals(mailActivationSpec.getStoreProtocol())) {
			return new POP3MailFolder(mailActivationSpec);
		} else if ("imap".equals(mailActivationSpec.getStoreProtocol())) {
			return new IMAPMailFolder(mailActivationSpec);
        } else if ("pop3s".equals(mailActivationSpec.getStoreProtocol())) {
           return new POP3sMailFolder(mailActivationSpec);
        } else if ("imaps".equals(mailActivationSpec.getStoreProtocol())) {
           return new IMAPsMailFolder(mailActivationSpec);
        } else {
			return null;
		}
	}

}
