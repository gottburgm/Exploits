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

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Flags.Flag;

public class IMAPMailFolder extends MailFolder {

	public IMAPMailFolder(MailActivationSpec spec) {
		super(spec);
	}

	protected Message[] getMessages(Folder folder) throws MessagingException {
		if (folder.getUnreadMessageCount() > 0) {
			int newCount = folder.getUnreadMessageCount();
			int messageCount = folder.getMessageCount();
			// folder.getMessages indexes from 1 and uses an inclusive range (ffs)
			return folder.getMessages(messageCount - newCount + 1, messageCount);
		}
		else 
		{
			return new Message[0];
		}
	}

	protected Store openStore(Session session) throws NoSuchProviderException {
		return session.getStore("imap");
	}

	protected void markMessageSeen(Message message) throws MessagingException {
		message.setFlag(Flag.SEEN, true);
	}

	protected void closeStore(boolean success, Store store, Folder folder) throws MessagingException {
		try {
			if (folder != null && folder.isOpen()) {
				folder.close(success);
			}
		} finally {
			if (store != null && store.isConnected()) {
				store.close();
			}
		}
	}

}
