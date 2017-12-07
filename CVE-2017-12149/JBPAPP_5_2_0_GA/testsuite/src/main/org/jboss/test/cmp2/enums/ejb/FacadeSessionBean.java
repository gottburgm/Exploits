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
package org.jboss.test.cmp2.enums.ejb;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.CreateException;

/**
 * @ejb:bean
 *    type="Stateless"
 *    name="Facade"
 *    view-type="remote"
 * @ejb.util generate="physical"
 * @ejb:transaction type="Required"
 * @ejb:transaction-type type="Container"
 */
public class FacadeSessionBean
   implements SessionBean
{
   // Business methods

   /**
    * @ejb.interface-method
    */
   public ColorEnum getColorForId(IDClass id)
      throws Exception
   {
      ChildLocal child = ChildUtil.getLocalHome().findByPrimaryKey(id);
      return child.getColor();
   }

   /**
    * @ejb.interface-method
    */
   public AnimalEnum getAnimalForId(IDClass id)
      throws Exception
   {
      ChildLocal child = ChildUtil.getLocalHome().findByPrimaryKey(id);
      return child.getAnimal();
   }

   /**
    * @ejb.interface-method
    */
   public void setColor(IDClass id, ColorEnum color)
      throws Exception
   {
      ChildLocal child = ChildUtil.getLocalHome().findByPrimaryKey(id);
      child.setColor(color);
   }

   /**
    * @ejb.interface-method
    */
   public void setAnimal(IDClass id, AnimalEnum animal)
      throws Exception
   {
      ChildLocal child = ChildUtil.getLocalHome().findByPrimaryKey(id);
      child.setAnimal(animal);
   }

   /**
    * @ejb.interface-method
    */
   public void createChild(IDClass childId)
      throws Exception
   {
      ChildUtil.getLocalHome().create(childId);
   }

   /**
    * @ejb.interface-method
    */
   public void removeChild(IDClass childId)
      throws Exception
   {
      ChildUtil.getLocalHome().remove(childId);
   }

   /**
    * @ejb.interface-method
    */
   public IDClass findByColor(ColorEnum color)
      throws Exception
   {
      ChildLocal child = ChildUtil.getLocalHome().findByColor(color);
      return child.getId();
   }

   /**
    * @ejb.interface-method
    */
   public IDClass findAndOrderByColor(ColorEnum color)
      throws Exception
   {
      ChildLocal child = ChildUtil.getLocalHome().findAndOrderByColor(color);
      return child.getId();
   }

   /**
    * @ejb.interface-method
    */
   public IDClass findByColorDeclaredSql(ColorEnum color)
      throws Exception
   {
      ChildLocal child = ChildUtil.getLocalHome().findByColorDeclaredSql(color);
      return child.getId();
   }

   /**
    * @ejb.interface-method
    */
   public List findLowColor(ColorEnum color)
      throws Exception
   {
      Collection children = ChildUtil.getLocalHome().findLowColor(color);
      List ids = new ArrayList(children.size());
      for(Iterator i = children.iterator(); i.hasNext();)
      {
         ChildLocal child = (ChildLocal)i.next();
         ids.add(child.getId());
      }
      return ids;
   }

   public ColorEnum selectMinColor() throws Exception
   {
      return ChildUtil.getLocalHome().selectMinColor();
   }

   public ColorEnum selectMaxColor() throws Exception
   {
      return ChildUtil.getLocalHome().selectMaxColor();
   }

   public ColorEnum selectAvgColor() throws Exception
   {
      return ChildUtil.getLocalHome().selectAvgColor();
   }

   public ColorEnum selectColor(IDClass id) throws Exception
   {
      return ChildUtil.getLocalHome().selectColor(id);
   }

   // SessionBean implementation

   /**
    * @exception  CreateException Description of Exception
    * @ejb.create-method
    */
   public void ejbCreate() throws CreateException
   {
   }

   public void ejbActivate()
   {
   }

   public void ejbPassivate()
   {
   }

   public void ejbRemove()
   {
   }

   public void setSessionContext(SessionContext ctx)
   {
   }
}
