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
package org.jboss.security.integration;

//$Id: SecurityConstantsBridge.java 106477 2010-07-06 21:17:57Z mmoyses $

/**
 *  Bridge between the legacy JaasSecurityManagerService
 *  and the new SecurityManagement POJOs
 *  @author Anil.Saldhana@redhat.com
 *  @since  Dec 9, 2007 
 *  @version $Revision: 106477 $
 */
public class SecurityConstantsBridge
{
   public static int defaultCacheTimeout = 30*60;
   public static int defaultCacheResolution = 60;
   /** Frequency of the thread cleaning the authentication cache of expired entries */
   public static int defaultCacheFlushPeriod = 60*60;

   private static JNDIBasedSecurityManagement securityManagement;

   public static JNDIBasedSecurityManagement getSecurityManagement()
   {
      return securityManagement;
   }

   public void setSecurityManagement(JNDIBasedSecurityManagement securityManagement)
   {
      this.securityManagement = securityManagement;
   }
}
