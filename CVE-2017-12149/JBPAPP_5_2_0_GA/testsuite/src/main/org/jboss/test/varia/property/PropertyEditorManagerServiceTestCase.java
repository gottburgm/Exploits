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
package org.jboss.test.varia.property;


import java.util.Properties;

import org.jboss.common.beans.property.ByteEditor;
import org.jboss.common.beans.property.finder.PropertyEditorFinder;
import org.jboss.test.JBossTestCase;
import org.jboss.varia.property.PropertyEditorManagerService;

/** Unit tests for the PropertyEditorManagerServiceTestCase utility service.
 *
 * @see org.jboss.varia.property.PropertyEditorManagerService
 * @author Scott.Stark@jboss.org
 * @author Dimitris.Andreadis@jboss.org
 * 
 * @version $Revision: 113110 $
 */
public class PropertyEditorManagerServiceTestCase extends JBossTestCase
{
 
   PropertyEditorManagerService s = new PropertyEditorManagerService();
   Class bc = Byte.class;
   Class be = ByteEditor.class;
   Class sc = String.class;
   Class de = DummyEditor.class;

   public PropertyEditorManagerServiceTestCase(String name)
   {
      super(name);
   }

   public static class DummyEditor extends java.beans.PropertyEditorSupport
   {
      public void setAsText(String s)
      {
         setValue(s);
      }
   }

   protected void setUp()
   {
      getLog().debug("+++ " + getName());
      PropertyEditorFinder.getInstance().register(bc, null);
      PropertyEditorFinder.getInstance().register(sc, null);
   }

   /** 
    * Tests opertions.
    * @throws Exception
    */ 
   public void testOperations()
      throws Exception
   {
      s.registerEditor(bc, be);
      assertEquals(be, s.findEditor(bc).getClass());
      assertEquals(be, s.findEditor("java.lang.Byte").getClass());
      s.registerEditor(bc.getName(), be.getName());

      String ed = "org.jboss.common.beans.property,org.example.editor";
      s.setEditorSearchPath(ed);
      assertEquals(ed, s.getEditorSearchPath());

      s.setBootstrapEditors(
            "# COMMENT \n" + 
            sc.getName() + "=" + de.getName() + "\n");
      assertEquals(de, s.findEditor(sc).getClass());
      Properties p = new Properties();
      p.put(sc.getName(), de.getName());
      s.setEditors(p);
      assertEquals(de, s.findEditor(sc).getClass());
   }

   public void testUnregister()
      throws Exception
   {
      PropertyEditorManagerService s = new PropertyEditorManagerService();
      Class tc = Thread.class;
      s.registerEditor(tc, de);
      assertEquals(tc, s.getRegisteredEditors()[0]);
      s.start();
      s.destroy();
      assertEquals(null, s.findEditor(tc));
      assertEquals(null, PropertyEditorFinder.getInstance().find(tc));
      assertEquals(0, s.getRegisteredEditors().length);
   }

}

