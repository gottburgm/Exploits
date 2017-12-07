/*
 * JBoss, Home of Professional Open Source.
 * Copyright (c) 2012, Red Hat, Inc., and individual contributors
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
package org.jboss.system.property;

import org.jboss.common.beans.property.finder.PropertyEditorFinder;
import org.jboss.logging.Logger;

import java.beans.PropertyEditor;
import java.util.Map;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 */
public class PropertyEditorManagerService
{
   private static final Logger log = Logger.getLogger(PropertyEditorManagerService.class);

   private Map<Class<?>, Class<? extends PropertyEditor>> editors;

   public void setEditors(final Map<Class<?>, Class<? extends PropertyEditor>> editors)
   {
      this.editors = editors;
   }

   public void start()
   {
      if (editors == null)
         return;
      for (Map.Entry<Class<?>, Class<? extends PropertyEditor>> entry : editors.entrySet())
      {
         final Class<?> type = entry.getKey();
         final Class<? extends PropertyEditor> editor = entry.getValue();
         PropertyEditorFinder.getInstance().register(type, editor);
         log.infof("Registered editor %s for %s", editor, type);
      }
   }
}