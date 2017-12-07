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
package org.jboss.test.cmp2.keygen.test;

import java.util.Collection;
import javax.naming.Context;
import javax.naming.InitialContext;

import junit.framework.Test;
import org.jboss.logging.Logger;
import org.jboss.test.JBossTestCase;
import org.jboss.test.cmp2.keygen.ejb.UnknownPKLocal;
import org.jboss.test.cmp2.keygen.ejb.UnknownPKLocalHome;
import org.jboss.test.cmp2.keygen.ejb.IntegerPKLocalHome;
import org.jboss.test.cmp2.keygen.ejb.UnknownPKHome;
import org.jboss.test.cmp2.keygen.ejb.UnknownPK;
import org.jboss.test.util.ejb.EJBTestCase;

/** Tests of the entity-command key generation
 *
 * @author <a href="mailto:jeremy@boynes.com">Jeremy Boynes</a>
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public class KeyGenerationUnitTestCase extends EJBTestCase
{
   static final Logger log = Logger.getLogger(KeyGenerationUnitTestCase.class);

   public static Test suite() throws Exception
   {
      return JBossTestCase.getDeploySetup(KeyGenerationUnitTestCase.class, "cmp2-keygen.jar");
   }

   public KeyGenerationUnitTestCase(String name)
   {
      super(name);
   }

   public void testJBAS1249() throws Exception
   {
      UnknownPKHome home = getUnknownPKRemoteHome("remote/TestPkSqlEJB");
      UnknownPK ejb = home.create("kloop");
      try
      {
         ejb.getHandle().getEJBObject();
      }
      catch(Exception e)
      {
         fail("Handler.getEJBObject() worked");
         throw e;
      }
      finally
      {
         ejb.remove();
      }
   }

   public void testUUIDKeyGenerator() throws Exception
   {
      UnknownPKLocalHome home = getUnknownPKHome("local/TestUUIDKeyGenEJB");
      UnknownPKLocal ejb1 = home.create("testUUIDKeyGenerator");
      UnknownPKLocal ejb2 = home.create("testUUIDKeyGenerator");
      try
      {
         UnknownPKLocal ejb1a = home.findByPrimaryKey(ejb1.getPrimaryKey());
         assertTrue(ejb1.isIdentical(ejb1a));
         assertTrue(ejb1.isIdentical(ejb2) == false);
         assertTrue(ejb1.getPrimaryKey().equals(ejb2.getPrimaryKey()) == false);
      }
      finally
      {
         ejb1.remove();
         ejb2.remove();
      }
   }

   public void testPkSQLKeyGenerator() throws Exception
   {
      UnknownPKLocalHome home = getUnknownPKHome("local/TestPkSqlEJB");
      UnknownPKLocal ejb1 = home.create("testPkSQLKeyGenerator");
      Thread.sleep(50);
      UnknownPKLocal ejb2 = home.create("testPkSQLKeyGenerator");
      try
      {
         UnknownPKLocal ejb1a = home.findByPrimaryKey(ejb1.getPrimaryKey());
         assertTrue(ejb1.isIdentical(ejb1a));
         assertTrue(ejb1.isIdentical(ejb2) == false);
         assertTrue(ejb1.getPrimaryKey().equals(ejb2.getPrimaryKey()) == false);
      }
      finally
      {
         ejb1.remove();
         ejb2.remove();
      }
   }

   public void testHsqldbKeyGenerator() throws Exception
   {
      UnknownPKLocalHome home = getUnknownPKHome("local/TestHsqldbEJB");
      UnknownPKLocal ejb1 = home.create("testHsqldbKeyGenerator");
      UnknownPKLocal ejb2 = home.create("testHsqldbKeyGenerator");
      try
      {
         UnknownPKLocal ejb1a = home.findByPrimaryKey(ejb1.getPrimaryKey());
         assertTrue(ejb1.isIdentical(ejb1a));
         assertTrue(ejb1.isIdentical(ejb2) == false);
         assertTrue(ejb1.getPrimaryKey().equals(ejb2.getPrimaryKey()) == false);
      }
      finally
      {
         ejb1.remove();
         ejb2.remove();
      }
   }

   public void testHsqldbIntegerKeyGenerator() throws Exception
   {
      Context ctx = new InitialContext();
      IntegerPKLocalHome home = (IntegerPKLocalHome) ctx.lookup("java:comp/env/local/TestHsqldbIntegerEJB");
      UnknownPKLocal ejb1 = home.create("testHsqldbIntegerKeyGenerator");
      UnknownPKLocal ejb2 = home.create("testHsqldbIntegerKeyGenerator");
      try
      {
         Integer key = (Integer) ejb1.getPrimaryKey();
         UnknownPKLocal ejb1a = home.findByPrimaryKey(key);
         assertTrue(ejb1.isIdentical(ejb1a));
         assertTrue(ejb1.isIdentical(ejb2) == false);
         assertTrue(ejb1.getPrimaryKey().equals(ejb2.getPrimaryKey()) == false);
      }
      finally
      {
         ejb1.remove();
         ejb2.remove();
      }
   }

   public void testInvalidHsqldbIntegerKeyGenerator() throws Exception
   {
      Context ctx = new InitialContext();
      IntegerPKLocalHome home = (IntegerPKLocalHome) ctx.lookup("java:comp/env/local/InvalidHsqldbIntegerEJB");
      try
      {
         UnknownPKLocal ejb1 = home.create("testInvalidHsqldbIntegerKeyGenerator");
         Object key = ejb1.getPrimaryKey();
         assertTrue("InvalidHsqldbIntegerEJB key != null", key != null);
      }
      catch(Exception e)
      {
         log.debug("create failed as expected", e);
         // Remove the bean that was inserted into the table
         Collection beans = home.findAll();
         UnknownPKLocal ejb1 = (UnknownPKLocal) beans.iterator().next();
         ejb1.remove();
      }
   }

   public void testOtherKeyGenerator() throws Exception
   {
      UnknownPKLocalHome home = getUnknownPKHome("local/TestOtherEJB");
      UnknownPKLocal ejb1 = home.create("testOtherKeyGenerator1");
      UnknownPKLocal ejb2 = home.create("testOtherKeyGenerator2");
      try
      {
         UnknownPKLocal ejb1a = home.findByPrimaryKey(ejb1.getPrimaryKey());
         assertTrue(ejb1.isIdentical(ejb1a));
         assertEquals("testOtherKeyGenerator1", ejb1a.getValue());
         assertTrue(ejb1.isIdentical(ejb2) == false);
         assertTrue(ejb1.getPrimaryKey().equals(ejb2.getPrimaryKey()) == false);
      }
      finally
      {
         ejb1.remove();
         ejb2.remove();
      }
   }

   private UnknownPKLocalHome getUnknownPKHome(String jndiName) throws Exception
   {
      return (UnknownPKLocalHome) getHome(jndiName);
   }

   private UnknownPKHome getUnknownPKRemoteHome(String jndiName) throws Exception
   {
      return (UnknownPKHome)getHome(jndiName);
   }

   private Object getHome(String jndiName) throws Exception
   {
      Context ctx = new InitialContext();
      return ctx.lookup("java:comp/env/"+jndiName);
   }
}
