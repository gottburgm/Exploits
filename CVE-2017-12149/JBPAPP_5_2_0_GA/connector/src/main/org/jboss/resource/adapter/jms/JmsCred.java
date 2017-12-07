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
package org.jboss.resource.adapter.jms;

import java.util.Set;
import java.util.Iterator;
import java.security.AccessController;
import java.security.PrivilegedAction;

import javax.security.auth.Subject;

import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.SecurityException;
import javax.resource.spi.ConnectionRequestInfo;

import javax.resource.spi.security.PasswordCredential;

/**
 * Credential information
 * 
 * @author <a href="mailto:peter.antman@tim.se">Peter Antman </a>.
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 71554 $
 */
public class JmsCred
{
	public String name;

	public String pwd;

	public JmsCred()
	{
		// empty
	}

	/**
	 * Get our own simple cred
	 */
	public static JmsCred getJmsCred(ManagedConnectionFactory mcf, Subject subject, ConnectionRequestInfo info)
			throws SecurityException
	{
		JmsCred jc = new JmsCred();
		if (subject == null && info != null)
		{
			// Credentials specifyed on connection request
			jc.name = ((JmsConnectionRequestInfo) info).getUserName();
			jc.pwd = ((JmsConnectionRequestInfo) info).getPassword();
		}
		else if (subject != null)
		{
			// Credentials from appserver
			PasswordCredential pwdc = GetCredentialAction.getCredential(subject, mcf);
			if (pwdc == null)
			{
				// No hit - we do need creds
				throw new SecurityException("No Password credentials found");
			}
			jc.name = pwdc.getUserName();
			jc.pwd = new String(pwdc.getPassword());
		}
		else
		{
			throw new SecurityException("No Subject or ConnectionRequestInfo set, could not get credentials");
		}
		return jc;
	}

	public String toString()
	{
		return super.toString() + "{ username=" + name + ", password=**** }";
	}

   private static class GetCredentialAction implements PrivilegedAction
   {
      Subject subject;
      ManagedConnectionFactory mcf;
      GetCredentialAction(Subject subject, ManagedConnectionFactory mcf)
      {
         this.subject = subject;
         this.mcf = mcf;
      }
      public Object run()
      {
         Set creds = subject.getPrivateCredentials(PasswordCredential.class);
         PasswordCredential pwdc = null;
         Iterator credentials = creds.iterator();
         while (credentials.hasNext())
         {
            PasswordCredential curCred = (PasswordCredential) credentials.next();
            if (curCred.getManagedConnectionFactory().equals(mcf))
            {
               pwdc = curCred;
               break;
            }
         }
         return pwdc;
      }
      static PasswordCredential getCredential(Subject subject, ManagedConnectionFactory mcf)
      {
         GetCredentialAction action = new GetCredentialAction(subject, mcf);
         PasswordCredential pc = (PasswordCredential) AccessController.doPrivileged(action);
         return pc;
      }
	}
}
