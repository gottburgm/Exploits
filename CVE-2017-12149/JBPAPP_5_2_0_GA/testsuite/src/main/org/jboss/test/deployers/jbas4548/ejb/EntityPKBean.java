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
package org.jboss.test.deployers.jbas4548.ejb;


import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.CreateException;
import javax.ejb.RemoveException;

import org.jboss.logging.Logger;

/** Tests of the cluster cache invalidation framework.
 * @author Scott.Stark@jboss.org
 * @version $Revision: 85526 $
 */ 
public abstract class EntityPKBean implements EntityBean
{
   private static Logger log = Logger.getLogger(EntityPKBean.class);

   private EntityContext entityContext;

   public AComplexPK ejbCreate(boolean aBoolean, int anInt, long aLong,
      double aDouble, String aString)
      throws CreateException
   {
      log.debug("ejbCreate() called");
      updateAllValues(new AComplexPK(aBoolean, anInt, aLong, aDouble, aString));
      return null;
   }

   public AComplexPK ejbCreateMETHOD(boolean aBoolean, int anInt, long aLong,
      double aDouble, String aString)
      throws CreateException
   {
      log.debug("ejbCreateMETHOD() called");
      updateAllValues(new AComplexPK(aBoolean, anInt, aLong, aDouble, aString));
      return null;
   }

   public void ejbPostCreate(boolean aBoolean, int anInt, long aLong,
      double aDouble, String aString)
      throws CreateException
   {
      log.debug("ejbPostCreate(pk) called");
   }

   public void ejbPostCreateMETHOD(boolean aBoolean, int anInt, long aLong,
      double aDouble, String aString)
      throws CreateException
   {
      log.debug("ejbPostCreateMETHOD(pk) called");
   }

   public void ejbActivate()
   {
      log.debug("ejbActivate() called");
   }

   public void ejbLoad()
   {
      log.debug("ejbLoad() called");
   }

   public void ejbPassivate()
   {
      log.debug("ejbPassivate() called");
   }

   public void ejbRemove() throws RemoveException
   {

      log.debug("EntityPK.ejbRemove() called");
   }
   public void ejbStore()
   {
      log.debug("ejbStore() called");
   }

   public void setEntityContext(EntityContext context)
   {
      log.debug("setSessionContext() called");
      entityContext = context;
   }

   public void unsetEntityContext()
   {
      log.debug("unsetSessionContext() called");
      entityContext = null;
   }

   public void updateAllValues(AComplexPK aComplexPK)
   {
      setABoolean(aComplexPK.aBoolean);
      setADouble(aComplexPK.aDouble);
      setALong(aComplexPK.aLong);
      setAnInt(aComplexPK.anInt);
      setAString(aComplexPK.aString);
   }

   public AComplexPK readAllValues()
   {
      return new AComplexPK(getABoolean(), getAnInt(), getALong(), getADouble(),
         getAString());
   }

   public abstract boolean getABoolean();
   public abstract void setABoolean(boolean value);

   public abstract double getADouble();
   public abstract void setADouble(double value);

   public abstract long getALong();
   public abstract void setALong(long value);

   public abstract int getAnInt();
   public abstract void setAnInt(int value);

   public abstract String getAString();
   public abstract void setAString(String value);

   public abstract int getOtherField();
   public abstract void setOtherField(int newValue);

}
