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
package org.jboss.test.testbean.bean;

import java.rmi.*;
import javax.ejb.*;
import org.jboss.test.testbean.interfaces.AComplexPK;

public class EntityPKBean implements EntityBean {
   org.jboss.logging.Logger log = org.jboss.logging.Logger.getLogger(getClass());
    public boolean aBoolean;
    public int anInt;
    public long aLong;
    public double aDouble;
    public String aString;

   public int otherField;

   private EntityContext entityContext;


    public AComplexPK ejbCreate(boolean aBoolean, int anInt, long aLong, double aDouble, String aString) throws RemoteException, CreateException {

        log.debug("EntityPK.ejbCreate() called");

        this.aBoolean = aBoolean;
        this.anInt = anInt;
        this.aLong = aLong;
        this.aDouble = aDouble;
        this.aString = aString;

        return new AComplexPK(aBoolean, anInt, aLong, aDouble, aString);
    }

    public AComplexPK ejbCreateMETHOD(boolean aBoolean, int anInt, long aLong, double aDouble, String aString) throws RemoteException, CreateException {

        log.debug("EntityPK.ejbCreateMETHOD() called");

        this.aBoolean = aBoolean;
        this.anInt = anInt;
        this.aLong = aLong;
        this.aDouble = aDouble;
        this.aString = aString;

        return new AComplexPK(aBoolean, anInt, aLong, aDouble, aString);
    }

    public void ejbPostCreate(boolean aBoolean, int anInt, long aLong, double aDouble, String aString) throws RemoteException, CreateException {

        log.debug("EntityPK.ejbPostCreate(pk) called");
    }

    public void ejbPostCreateMETHOD(boolean aBoolean, int anInt, long aLong, double aDouble, String aString) throws RemoteException, CreateException {

        log.debug("EntityPK.ejbPostCreateMETHOD(pk) called");
    }

    public void ejbActivate() throws RemoteException {

        log.debug("EntityPK.ejbActivate() called");
    }

    public void ejbLoad() throws RemoteException {

        log.debug("EntityPK.ejbLoad() called");
    }

    public void ejbPassivate() throws RemoteException {

        log.debug("EntityPK.ejbPassivate() called");
    }

    public void ejbRemove() throws RemoteException, RemoveException {

        log.debug("EntityPK.ejbRemove() called");
    }

    public void ejbStore() throws RemoteException {

        log.debug("EntityPK.ejbStore() called");
    }


    public void setEntityContext(EntityContext context) throws RemoteException {

        log.debug("EntityPK.setSessionContext() called");
        entityContext = context;
    }

    public void unsetEntityContext() throws RemoteException {

        log.debug("EntityBMP.unsetSessionContext() called");
        entityContext = null;
    }

    public void updateAllValues(AComplexPK aComplexPK) throws RemoteException {

        this.aBoolean = aComplexPK.aBoolean;
        this.aDouble = aComplexPK.aDouble;
        this.aLong = aComplexPK.aLong;
        this.anInt = aComplexPK.anInt;
        this.aString = aComplexPK.aString;

    };

    public AComplexPK readAllValues() throws RemoteException {

        return new AComplexPK(aBoolean, anInt, aLong, aDouble, aString);


    };

   public int getOtherField() throws RemoteException {
      return otherField;
   }

   public void setOtherField(int newValue) throws RemoteException {
      otherField = newValue;
   }

}
