/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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

import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.RemoteException;

import javax.rmi.CORBA.Stub;
import javax.rmi.CORBA.Tie;
import javax.rmi.CORBA.UtilDelegate;
import javax.rmi.CORBA.ValueHandler;

import org.omg.CORBA.ORB;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;

/**
 * An implementation of {@linkplain javax.rmi.CORBA.UtilDelegate} that overrides
 * the {@link #loadClass(String, String, ClassLoader)} method to use the input
 * loader in preference to the ObjectInputStream#latestUserDefinedLoader.
 * Install by setting -Djavax.rmi.CORBA.UtilClass=org.jboss.iiop.JBossUtilDelegate
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 85945 $
 */
public class JBossUtilDelegate implements UtilDelegate
{
   /** The */
   private static UtilDelegate jdkUtilDelegate;
   private static final String jdkUtilDelegateName = 
      "com.sun.corba.se.impl.javax.rmi.CORBA.Util";

   /**
    * TODO: externalize the jdk class since com.sun.* is private
    */
   static
   {
      String delegateName = jdkUtilDelegateName;
      // Some system property to externalize...
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      try
      {
      Class<UtilDelegate> delegateClass = (Class<UtilDelegate>) loader.loadClass(delegateName);
      jdkUtilDelegate = delegateClass.newInstance();
      }
      catch(Throwable e)
      {
         throw new ExceptionInInitializerError(e);
      }
   }

   public JBossUtilDelegate()
   {
   }

   public Object copyObject(Object obj, ORB orb) throws RemoteException
   {
      return jdkUtilDelegate.copyObject(obj, orb);
   }

   public Object[] copyObjects(Object[] obj, ORB orb) throws RemoteException
   {
      return jdkUtilDelegate.copyObjects(obj, orb);
   }

   public ValueHandler createValueHandler()
   {
      return jdkUtilDelegate.createValueHandler();
   }

   public String getCodebase(Class clz)
   {
      return jdkUtilDelegate.getCodebase(clz);
   }

   public Tie getTie(Remote target)
   {
      return jdkUtilDelegate.getTie(target);
   }

   public boolean isLocal(Stub stub) throws RemoteException
   {
      return jdkUtilDelegate.isLocal(stub);
   }

   public Class loadClass(String className, String remoteCodebase,
         ClassLoader loader) throws ClassNotFoundException
   {
      Class clazz = null;
      try
      {
         if(loader != null)
            clazz = loader.loadClass(className);
      }
      catch(ClassNotFoundException e)
      {
         clazz = jdkUtilDelegate.loadClass(className, remoteCodebase, loader);
      }
      return clazz;
   }

   public RemoteException mapSystemException(SystemException ex)
   {
      return jdkUtilDelegate.mapSystemException(ex);
   }

   public Object readAny(InputStream in)
   {
      return jdkUtilDelegate.readAny(in);
   }

   public void registerTarget(Tie tie, Remote target)
   {
      jdkUtilDelegate.registerTarget(tie, target);
   }

   public void unexportObject(Remote target) throws NoSuchObjectException
   {
      jdkUtilDelegate.unexportObject(target);
   }

   public RemoteException wrapException(Throwable obj)
   {
      return jdkUtilDelegate.wrapException(obj);
   }

   public void writeAbstractObject(OutputStream out, Object obj)
   {
      jdkUtilDelegate.writeAbstractObject(out, obj);
   }

   public void writeAny(OutputStream out, Object obj)
   {
      jdkUtilDelegate.writeAny(out, obj);
   }

   public void writeRemoteObject(OutputStream out, Object obj)
   {
      jdkUtilDelegate.writeRemoteObject(out, obj);
   }

   
}
