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
package org.jboss.mx.remoting;

import java.io.Serializable;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import org.jboss.remoting.ConnectionFailedException;
import org.jboss.remoting.ident.Identity;
import org.jboss.remoting.loading.ClassUtil;

/**
 * MBeanLocator is used to uniquely indentify and locate a specific MBean on the JMX remoting
 * network.
 *
 * @author <a href="mailto:jhaynie@vocalocity.net">Jeff Haynie</a>
 * @version $Revision: 81023 $
 */
public class MBeanLocator implements Serializable
{
   static final long serialVersionUID = -95280512054710509L;

   private final Identity identity;
   private final ObjectName objectName;
   private final MBeanServerLocator locator;

   public MBeanLocator(MBeanServerLocator sl, ObjectName obj)
   {
      this.identity = sl.getIdentity();
      this.locator = sl;
      this.objectName = obj;
   }

   public boolean equals(Object o)
   {
      if(this == o)
      {
         return true;
      }
      if(!(o instanceof MBeanLocator))
      {
         return false;
      }

      final MBeanLocator mBeanLocator = (MBeanLocator) o;

      if(identity != null ? !identity.equals(mBeanLocator.identity) : mBeanLocator.identity != null)
      {
         return false;
      }
      if(locator != null ? !locator.equals(mBeanLocator.locator) : mBeanLocator.locator != null)
      {
         return false;
      }
      if(objectName != null ? !objectName.equals(mBeanLocator.objectName) : mBeanLocator.objectName != null)
      {
         return false;
      }

      return true;
   }

   public int hashCode()
   {
      int result;
      result = (identity != null ? identity.hashCode() : 0);
      result = 29 * result + (objectName != null ? objectName.hashCode() : 0);
      result = 29 * result + (locator != null ? locator.hashCode() : 0);
      return result;
   }

   /**
    * return the server locator for this mbean
    *
    * @return
    */
   public MBeanServerLocator getServerLocator()
   {
      return this.locator;
   }

   /**
    * return the identity of the mbean server
    *
    * @return
    */
   public final Identity getIdentity()
   {
      return this.identity;
   }

   /**
    * return the ObjectName that identifies the MBean
    *
    * @return
    */
   public final ObjectName getObjectName()
   {
      return objectName;
   }

   /**
    * stringify
    *
    * @return
    */
   public String toString()
   {
      return "MBeanLocator [server:" + locator + ",mbean:" + objectName + "]";
   }

   /**
    * returns true if the MBeanLocator is the same JVM as this locator
    *
    * @param locator
    * @return
    */
   public boolean isSameJVM(MBeanLocator locator)
   {
      return locator != null && locator.locator.equals(this.locator);
   }


   /**
    * narrow this locator to an interface class that the MBean locator implements
    *
    * @param interfaceCl
    * @return
    */
   public Object narrow(Class interfaceCl)
   {
      Class cl[] = ClassUtil.getInterfacesFor(interfaceCl);
      return narrow(cl);
   }

   /**
    * return a dynamic proxy to the remote mbean server where this locator lives ...
    *
    * @return
    * @throws ConnectionFailedException
    */
   public MBeanServer getMBeanServer()
         throws ConnectionFailedException
   {
      return locator.getMBeanServer();
   }

   /**
    * narrow the locator to a specific Class interface that the MBean locator implements
    *
    * @param interfaces
    * @return
    */
   public Object narrow(Class interfaces[])
   {
      return MoveableMBean.create(this, interfaces);
   }
}
