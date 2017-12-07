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
package org.jboss.test.testbyvalue.bean;

import org.jboss.test.testbyvalue.interfaces.ByValueStatelessSessionHome;
import org.jboss.test.testbyvalue.interfaces.ByValueStatelessSession;
import org.jboss.test.testbyvalue.interfaces.ByReferenceStatelessSessionHome;
import org.jboss.test.testbyvalue.interfaces.ByReferenceStatelessSession;
import org.jboss.test.testbyvalue.interfaces.ByValueEntityHome;
import org.jboss.test.testbyvalue.interfaces.ByValueEntity;

import java.rmi.*;
import javax.ejb.*;
import javax.naming.InitialContext;
import javax.naming.Context;
import javax.rmi.PortableRemoteObject;

/**
 * $Id: RootStatelessSessionBean.java 81036 2008-11-14 13:36:39Z dimitris@jboss.org $
 * @author Clebert Suconic
 */
public class RootStatelessSessionBean implements SessionBean
{
   org.jboss.logging.Logger log = org.jboss.logging.Logger.getLogger(getClass());
   private SessionContext sessionContext;

   public void ejbCreate() throws RemoteException, CreateException
   {
   }

   public void ejbActivate() throws RemoteException
   {
   }

   public void ejbPassivate() throws RemoteException
   {
   }

   public void ejbRemove() throws RemoteException
   {
   }

   public void setSessionContext(SessionContext context) throws RemoteException
   {
      sessionContext = context;

      //Exception e = new Exception("in set Session context");
      //log.debug("failed", e);
   }

    public long doTestByValue(int iterations) throws Exception
    {

        Context ctx = new InitialContext();
        Object objhome =
                ctx.lookup("java:comp/env/ejb/CalledByValue");

        ByValueStatelessSessionHome home = (ByValueStatelessSessionHome) PortableRemoteObject.narrow(objhome, ByValueStatelessSessionHome.class);
        ByValueStatelessSession session = home.create();

        ClassWithProperty property = new ClassWithProperty();

        property.setX(1000);

        long initTime = System.currentTimeMillis();


        for (int i=0;i<iterations;i++)
        {
            session.doTestByValue(property);

            if (property.getX()!=1000)
            {
                throw new RuntimeException("Property was changed in a call-by-value operation");
            }
        }

        return System.currentTimeMillis() - initTime;
    }

    public long doTestByReference(int iterations) throws Exception
    {
        Context ctx = new InitialContext();
        Object objhome =
                ctx.lookup("java:comp/env/ejb/CalledByReference");

        ByReferenceStatelessSessionHome home = (ByReferenceStatelessSessionHome) PortableRemoteObject.narrow(objhome, ByReferenceStatelessSessionHome.class);
        ByReferenceStatelessSession session = home.create();

        ClassWithProperty property = new ClassWithProperty();

        property.setX(1000);

        long initTime = System.currentTimeMillis();


        for (int i=0;i<iterations;i++)
        {
            session.doTestByReference(property);

            if (property.getX()==1000)
            {
                throw new RuntimeException("Property was not changed in a call-by-reference operation");
            }

            property.setX(1000);
        }

        return System.currentTimeMillis() - initTime;
    }

    public long doTestEntity(int iterations) throws Exception
    {
        Context ctx = new InitialContext();
        Object objhome =
                ctx.lookup("java:comp/env/ejb/TestByValueEntity");

        ByValueEntityHome home = (ByValueEntityHome) PortableRemoteObject.narrow(objhome, ByValueEntityHome.class);
        ByValueEntity entity = home.create();

        ClassWithProperty property = new ClassWithProperty();

        property.setX(1000);

        long initTime = System.currentTimeMillis();


        for (int i=0;i<iterations;i++)
        {
            entity.doByValueTest(property);

            if (property.getX()!=1000)
            {
                throw new RuntimeException("Property was changed in a call-by-value operation");
            }

            property.setX(1000);
        }

        return System.currentTimeMillis() - initTime;
    }

    public long doTestEntityByReference(int iterations) throws Exception
    {
        Context ctx = new InitialContext();
        Object objhome =
                ctx.lookup("java:comp/env/ejb/TestByReferenceEntity");

        ByValueEntityHome home = (ByValueEntityHome) PortableRemoteObject.narrow(objhome, ByValueEntityHome.class);
        ByValueEntity entity = home.create();

        ClassWithProperty property = new ClassWithProperty();

        property.setX(1000);

        long initTime = System.currentTimeMillis();


        for (int i=0;i<iterations;i++)
        {
            entity.doByValueTest(property);

            if (property.getX()==1000)
            {
                throw new RuntimeException("Property was not changed in a call-by-reference operation");
            }

            property.setX(1000);
        }

        return System.currentTimeMillis() - initTime;
    }

}
