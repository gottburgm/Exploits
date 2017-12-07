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
package org.jboss.test.cmp2.enums.test;

import java.util.List;
import junit.framework.Test;
import org.jboss.test.JBossTestCase;
import org.jboss.test.util.ejb.EJBTestCase;
import org.jboss.test.cmp2.enums.ejb.Facade;
import org.jboss.test.cmp2.enums.ejb.FacadeUtil;
import org.jboss.test.cmp2.enums.ejb.ColorEnum;
import org.jboss.test.cmp2.enums.ejb.AnimalEnum;
import org.jboss.test.cmp2.enums.ejb.IDClass;

/**
 *
 * @author <a href="mailto:alex@jboss.org">Alex Loubyansky</a>
 * @author <a href="mailto:gturner@unzane.com">Gerald Turner</a>
 */
public class EnumUnitTestCase
   extends EJBTestCase
{
   public static Test suite() throws Exception
   {
      return JBossTestCase.getDeploySetup(EnumUnitTestCase.class, "cmp2-enum.jar");
   }

   public EnumUnitTestCase(String s)
   {
      super(s);
   }

   // Tests

   public void testColorEnum()
      throws Exception
   {
      Facade facade = FacadeUtil.getHome().create();
      IDClass childId = new IDClass(1);
      facade.createChild(childId);
      assertTrue(ColorEnum.RED == facade.getColorForId(childId));
      facade.setColor(childId, ColorEnum.GREEN);
      assertTrue(ColorEnum.GREEN == facade.getColorForId(childId));
      facade.setColor(childId, ColorEnum.BLUE);
      assertTrue(ColorEnum.BLUE == facade.getColorForId(childId));
      facade.removeChild(childId);
   }

   public void testAnimalEnum()
      throws Exception
   {
      Facade facade = FacadeUtil.getHome().create();
      IDClass childId = new IDClass(2);
      facade.createChild(childId);
      assertTrue(AnimalEnum.PENGUIN == facade.getAnimalForId(childId));
      facade.setAnimal(childId, AnimalEnum.DOG);
      assertTrue(AnimalEnum.DOG == facade.getAnimalForId(childId));
      facade.setAnimal(childId, AnimalEnum.CAT);
      assertTrue(AnimalEnum.CAT == facade.getAnimalForId(childId));
      facade.removeChild(childId);
   }

   public void testFindByColor()
      throws Exception
   {
      Facade facade = FacadeUtil.getHome().create();
      IDClass childId = new IDClass(3);
      facade.createChild(childId);
      try
      {
         facade.setColor(childId, ColorEnum.BLUE);
         IDClass id = facade.findByColor(ColorEnum.BLUE);
         assertEquals(childId, id);
      }
      finally
      {
         facade.removeChild(childId);
      }
   }

   public void testFindAndOrderByColor()
      throws Exception
   {
      Facade facade = FacadeUtil.getHome().create();
      IDClass childId = new IDClass(3);
      facade.createChild(childId);
      try
      {
         facade.setColor(childId, ColorEnum.BLUE);
         IDClass id = facade.findAndOrderByColor(ColorEnum.BLUE);
         assertEquals(childId, id);
      }
      finally
      {
         facade.removeChild(childId);
      }
   }

   public void testFindByColorDeclaredSql()
      throws Exception
   {
      Facade facade = FacadeUtil.getHome().create();
      IDClass childId = new IDClass(4);
      facade.createChild(childId);
      try
      {
         facade.setColor(childId, ColorEnum.BLUE);
         IDClass id = facade.findByColorDeclaredSql(ColorEnum.BLUE);
         assertEquals(childId, id);
      }
      finally
      {
         facade.removeChild(childId);
      }
   }

   public void testLowColor()
      throws Exception
   {
      Facade facade = FacadeUtil.getHome().create();
      IDClass childId = new IDClass(3);
      facade.createChild(childId);
      try
      {
         facade.setColor(childId, ColorEnum.RED);
         List ids = facade.findLowColor(ColorEnum.BLUE);
         assertEquals(1, ids.size());
         assertEquals(childId, ids.get(0));
      }
      finally
      {
         facade.removeChild(childId);
      }
   }

   public void testSelectColors() throws Exception
   {
      Facade facade = FacadeUtil.getHome().create();
      IDClass[] ids = new IDClass[]{new IDClass(555), new IDClass(666), new IDClass(777)};

      try
      {
         facade.createChild(ids[0]);
         ColorEnum min = ColorEnum.RED.valueOf(0);
         facade.setColor(ids[0], min);

         facade.createChild(ids[1]);
         ColorEnum avg = ColorEnum.RED.valueOf(1);
         facade.setColor(ids[1], avg);

         facade.createChild(ids[2]);
         ColorEnum max = ColorEnum.RED.valueOf(2);
         facade.setColor(ids[2], max);

         ColorEnum color = facade.selectMinColor();
         assertEquals(min, color);      
         color = facade.selectMaxColor();
         assertEquals(max, color);
         color = facade.selectAvgColor();
         assertEquals(avg, color);
      }
      finally
      {
         for(int i = 0; i < ids.length; ++i)
            facade.removeChild(ids[i]);
      }
   }

   public void testSelectColor() throws Exception
   {
      Facade facade = FacadeUtil.getHome().create();
      IDClass childId = new IDClass(6);
      facade.createChild(childId);
      try
      {
         facade.setColor(childId, ColorEnum.RED);
         ColorEnum color = facade.selectColor(childId);
         assertEquals(ColorEnum.RED, color);
      }
      finally
      {
         facade.removeChild(childId);
      }
   }
}
