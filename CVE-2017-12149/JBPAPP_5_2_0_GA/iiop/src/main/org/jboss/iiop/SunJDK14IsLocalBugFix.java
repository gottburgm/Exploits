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
package org.jboss.iiop;

/**
 * There is a bug in Sun's implementation of the method javax.rmi.CORBA.Util.isLocal().
 * Stubs generated with the rmic tool call this method to check is the call is a local invocation,
 * which they can optimize. However, Sun's implementation of isLocal() tries to cast the stub to a proprietary
 * class. This is against the rules. Since the ORB is pluggable ("-Dorg.omg.CORBA.ORBClass=...), Sun's
 * implementation of a standard (javax.rmi) method should not assume that the stubs are Sun stubs.
 *
 * @author <a href="mailto:reverbel@ime.usp.br">Francisco Reverbel</a>
 * @version $Revision: 81018 $
 *
 **/
public class SunJDK14IsLocalBugFix extends com.sun.corba.se.internal.iiop.ShutdownUtilDelegate
{
   public boolean isLocal(javax.rmi.CORBA.Stub stub)
           throws java.rmi.RemoteException
   {
      try
      {
         org.omg.CORBA.portable.Delegate delegate = stub._get_delegate();
         return delegate.is_local(stub);
      }
      catch (org.omg.CORBA.SystemException e)
      {
         throw javax.rmi.CORBA.Util.mapSystemException(e);
      }
   }
}
