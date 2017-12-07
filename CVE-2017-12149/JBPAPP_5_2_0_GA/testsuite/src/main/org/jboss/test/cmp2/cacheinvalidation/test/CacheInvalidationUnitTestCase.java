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
package org.jboss.test.cmp2.cacheinvalidation.test;

import org.jboss.test.JBossTestCase;
import org.jboss.test.cmp2.cacheinvalidation.ejb.Facade;
import org.jboss.test.cmp2.cacheinvalidation.ejb.FacadeHome;
import junit.framework.Test;

import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 81036 $</tt>
 */
public class CacheInvalidationUnitTestCase
   extends JBossTestCase
{
   public CacheInvalidationUnitTestCase(String name)
   {
      super(name);
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(CacheInvalidationUnitTestCase.class, "cmp2-cacheinvalidation.jar");
   }

   public void testBasicInvalidation() throws Exception
   {
      Facade facade = getFacadeHome().create();

      try
      {
         facade.setup();

         Long pk = new Long(1);
         String cName = facade.readFirstName("CROLocal", pk);
         assertEquals("Avoka", cName);

         cName = facade.readFirstName("CRWLocal", pk);
         assertEquals("Avoka", cName);

         facade.writeFirstName("CRWLocal", pk, "Ataka");

         cName = facade.readFirstName("CROLocal", pk);
         assertEquals("Ataka", cName);

         cName = facade.readFirstName("CRWLocal", pk);
         assertEquals("Ataka", cName);
      }
      finally
      {
         facade.tearDown();
      }
   }

   public void testCmrInvalidation() throws Exception
   {
      Facade facade = getFacadeHome().create();

      try
      {
         facade.setup();

         Long pk = new Long(1);
         String aName = facade.readRelatedAFirstName("CROLocal", pk);
         assertEquals("Ataka", aName);

         aName = facade.readRelatedAFirstName("CRWLocal", pk);
         assertEquals("Ataka", aName);

         facade.removeA("ARWLocal", new Long(2));

         aName = facade.readRelatedAFirstName("CROLocal", pk);
         assertNull(aName);

         aName = facade.readRelatedAFirstName("CRWLocal", pk);
         assertNull(aName);
      }
      finally
      {
         facade.tearDown();
      }
   }

   private static final FacadeHome getFacadeHome()
   {
      InitialContext ctx = null;
      try
      {
         ctx = new InitialContext();
         return (FacadeHome)ctx.lookup(FacadeHome.JNDI_NAME);
      }
      catch(NamingException e)
      {
         throw new IllegalStateException("Failed to look up jndi binding " + FacadeHome.JNDI_NAME + ": " + e.getMessage());
      }
   }
}
