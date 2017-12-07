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
package org.jboss.crypto.digest;

import java.util.Map;
import java.security.MessageDigest;

/**
 * An interface that can be used to augment the behavior of a digest hash.
 * One example usecase is with the password based login modules to
 * modify the behavior of the hashing to introduce prefix/suffix salts.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 85945 $
 */
public interface DigestCallback
{
   /** Pass through access to the login module options. When coming from a
    * login module this includes the following keys:
    * javax.security.auth.login.name - for the username
    * javax.security.auth.login.password - for the String password
    */
   public void init(Map options);
   /**
    * Pre-hash callout to allow for content before the password. Any content
    * should be added using the MessageDigest update methods.
    * @param digest - the security digest being used for the one-way hash
    */ 
   public void preDigest(MessageDigest digest);
   /** Post-hash callout afer the password has been added to allow for content
    * after the password has been added. Any content should be added using the
    * MessageDigest update methods.
    * @param digest - the security digest being used for the one-way hash
    */
   public void postDigest(MessageDigest digest);
}
