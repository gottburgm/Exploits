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
package org.jboss.proxy.ejb;

import java.util.Hashtable;
import javax.ejb.EJBHome;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;
import javax.rmi.PortableRemoteObject;

import org.jboss.iiop.CorbaORB;

/**
 * An <code>ObjectFactory</code> implementation that translates
 * <code>Reference</code>s to <code>EJBHome</code>s back into CORBA
 * object references. The IIOP proxy factory (IORFactory) binds these 
 * <code>Reference</code>s in the JRMP/JNDI namespace.
 *
 * @author  <a href="mailto:reverbel@ime.usp.br">Francisco Reverbel</a>
 * @version $Revision: 81018 $
 */
public class IIOPHomeFactory implements ObjectFactory 
{

   public IIOPHomeFactory()
   {
   }
   
   // Implementation of the interface ObjectFactory ------------------------
   
   /** Lookup the IOR from the Reference and convert into the CORBA
    * object value using the ORB.string_to_object method.
    * 
    * @param obj a javax.naming.Reference with a string IOR under the
    *    address type IOR.
    * @param name not used
    * @param nameCtx not used
    * @param environment not used
    * @return The EJBHome proxy for the IOR
    * @throws Exception
    */ 
   public Object getObjectInstance(Object obj, Name name,
                                   Context nameCtx, Hashtable environment)
      throws Exception
   {
      Reference ref = (Reference) obj;
      String ior = (String) ref.get("IOR").getContent();
      org.omg.CORBA.Object corbaObj = CorbaORB.getInstance().string_to_object(ior);
      return (EJBHome) PortableRemoteObject.narrow(corbaObj, EJBHome.class);
   }
}

