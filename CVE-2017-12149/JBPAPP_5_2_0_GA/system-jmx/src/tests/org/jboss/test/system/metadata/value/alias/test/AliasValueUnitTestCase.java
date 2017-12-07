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
package org.jboss.test.system.metadata.value.alias.test;

import java.util.List;
import java.util.Iterator;

import org.jboss.test.system.metadata.test.AbstractMetaDataTest;
import org.jboss.system.metadata.ServiceMetaData;

/**
 * Test aliases on XMBeans.
 * 
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class AliasValueUnitTestCase extends AbstractMetaDataTest
{
   public AliasValueUnitTestCase(String name)
   {
      super(name);
   }

   public void testAliasEmpty() throws Exception
   {
      ServiceMetaData service = unmarshalSingleMBean();
      assertNull(service.getAliases());
   }

   public void testAliasSingle() throws Exception
   {
      ServiceMetaData service = unmarshalSingleMBean();
      List<String> aliases = service.getAliases();
      assertNotNull(aliases);
      assertEquals(1, aliases.size());
      assertEquals("SingleAlias", aliases.iterator().next());
   }

   public void testAliasMultiple() throws Exception
   {
      ServiceMetaData service = unmarshalSingleMBean();
      List<String> aliases = service.getAliases();
      assertNotNull(aliases);
      assertEquals(3, aliases.size());
      for(int i = 0; i < aliases.size(); i++)
         assertEquals("Alias#" + i, aliases.get(i));
   }
}
