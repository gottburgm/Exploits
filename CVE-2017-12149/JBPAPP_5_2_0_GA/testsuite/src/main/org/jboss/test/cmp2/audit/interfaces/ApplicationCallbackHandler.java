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
package org.jboss.test.cmp2.audit.interfaces;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

/**
 * A callback handler for login with user and password.
 *   
 * @author <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>
 * @version $Revision: 81036 $
 */
public class ApplicationCallbackHandler
   implements CallbackHandler
{
   // Attributes ----------------------------------------------------

   private String user;
   private char[] password;

   // Constructor ---------------------------------------------------

   private ApplicationCallbackHandler(String user, String password)
   {
      this.user = user;
      this.password = password.toCharArray();
   }

   // Public --------------------------------------------------------

   public void handle(Callback[] callbacks)
      throws UnsupportedCallbackException
   {
      for (int i = 0; i < callbacks.length; i++)
      {
         if (callbacks[i] instanceof NameCallback)
         {
            NameCallback nameCallback = (NameCallback) callbacks[i];
            nameCallback.setName(user);
         }
         else if (callbacks[i] instanceof PasswordCallback)
         {
            PasswordCallback passwordCallback = (PasswordCallback) callbacks[i];
            passwordCallback.setPassword(password);
         }
         else
         {
            throw new UnsupportedCallbackException(callbacks[i], "Unsupported callback");
         }
      }
   }

   // Static --------------------------------------------------------

   /**
    * Login using the "client-login" config
    */
   public static LoginContext login(String user, String password)
      throws LoginException
   {
      return login("client-login", user, password);
   }

   /**
    * Login using specific login configuration
    * @param config - name of login config to use
    * @param user
    * @param password
    * @return
    * @throws LoginException
    */
   public static LoginContext login(String config, String user, String password)
      throws LoginException
   {
      ApplicationCallbackHandler handler = new ApplicationCallbackHandler(user, password);
      LoginContext result = new LoginContext(config, handler);
      result.login();
      return result;
   }
}
