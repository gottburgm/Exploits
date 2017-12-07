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
package org.jboss.tutorial.spring;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;

/**
 * @author <a href="mailto:ales.justin@genera-lynx.com">Ales Justin</a>
 */
public class FileLineReader extends StaticWordsCreator implements InitializingBean
{

   private Resource resource;
   protected List<String> words;

   public Resource getResource()
   {
      return resource;
   }

   public void setResource(Resource resource)
   {
      this.resource = resource;
   }

   public void afterPropertiesSet() throws Exception
   {
      if (getResource() == null)
      {
         throw new IllegalArgumentException("Resource must be set!");
      }
      BufferedReader reader = new BufferedReader(new InputStreamReader(getResource().getInputStream()));
      try
      {
         words = new ArrayList<String>();
         String line;
         while ((line = reader.readLine()) != null)
         {
            words.add(line);
         }
      }
      finally
      {
         reader.close();
      }
   }

   protected String[] getArray()
   {
      return words.toArray(new String[words.size()]);
   }

}
