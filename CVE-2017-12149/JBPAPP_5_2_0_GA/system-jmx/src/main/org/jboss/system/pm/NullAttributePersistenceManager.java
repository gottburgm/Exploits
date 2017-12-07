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
package org.jboss.system.pm;

import javax.management.AttributeList;
import org.jboss.mx.persistence.AttributePersistenceManager;
import org.w3c.dom.Element;
/**
 * NullAttributePersistenceManager.
 * 
 * @author  <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81033 $
 */
public class NullAttributePersistenceManager
   implements  AttributePersistenceManager
{
   private boolean isCreated = false;
   
   public void create(String version, Element config)
      throws Exception
   {
      isCreated = true;
   }

   public boolean getState()
   {
      return isCreated;
   }
   
   public void destroy()
   {
      isCreated = false;
   }
   
   // AttributePersistenceManager Persistence -----------------------
   
   public boolean exists(String id)
      throws Exception
   {
      return false;
   }

   public AttributeList load(String id)
      throws Exception
   {
      return null;
   }
   
   public void store(String id, AttributeList attrs)
      throws Exception
   {
      // empty
   }
   
   public void remove(String id)
      throws Exception
   {
      // empty
   }
   
   public void removeAll()
      throws Exception
   {
      // empty
   }
   
   public String[] listAll()
      throws Exception
   {
      return new String[0];
   }   
}
