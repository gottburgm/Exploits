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
package org.jboss.test.client.test;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.CallbackHandler;

/** A test CallbackHandler that sets any NameCallback name property to the
 * j_username system property and sets any PasswordCallback password property
 * to the j_password system property.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public class SystemPropertyCallbackHandler implements CallbackHandler
{
   public SystemPropertyCallbackHandler()
   {
   }

   /** Sets any NameCallback name property to the j_username system property
    sets any PasswordCallback password property to the j_password system property.
    @exception UnsupportedCallbackException thrown if any callback of
    type other than NameCallback or PasswordCallback are seen.
    */
   public void handle(Callback[] callbacks) throws
      UnsupportedCallbackException
   {
      for (int i = 0; i < callbacks.length; i++)
      {
         Callback c = callbacks[i];
         if (c instanceof NameCallback)
         {
            NameCallback nc = (NameCallback) c;
            nc.setName(System.getProperty("j_username"));
         }
         else if (c instanceof PasswordCallback)
         {
            PasswordCallback pc = (PasswordCallback) c;
            String password = System.getProperty("j_password");
            if( password != null )
               pc.setPassword(password.toCharArray());
         }
         else
         {
            throw new UnsupportedCallbackException(callbacks[i], "Unrecognized Callback");
         }
      }
   }
}
