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
package org.jboss.test.cts.ejb;

import java.util.Properties;
import javax.ejb.DuplicateKeyException;
import javax.naming.InitialContext;

import org.jboss.test.util.ejb.EJBTestCase;
import org.jboss.test.JBossTestCase;
import org.jboss.test.cts.interfaces.CtsCmpLocalHome;
import org.jboss.test.cts.interfaces.CtsCmpLocal;
import org.jboss.test.cts.keys.AccountPK;
import org.jboss.logging.Logger;
import junit.framework.Test;

/** Tests of local ejbs
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public class LocalEjbTests extends EJBTestCase
{
   Logger log = Logger.getLogger(LocalEjbTests.class);

   public LocalEjbTests(String methodName)
   {
      super(methodName);
   }

   public static Test suite() throws Exception
   {
		return JBossTestCase.getDeploySetup(LocalEjbTests.class, "cts.jar");
   }

   public void setUpEJB(java.util.Properties props) throws Exception
   {
      super.setUpEJB(props);
   }

   public void tearDownEJB(Properties props) throws Exception
   {
      super.tearDownEJB(props);
   }

   public void testEntityIdentity() throws Exception
   {
      InitialContext ctx = new InitialContext();
      CtsCmpLocalHome home = (CtsCmpLocalHome) ctx.lookup("ejbcts/LocalCMPBean");
      AccountPK key1 = new AccountPK("1");
      CtsCmpLocal bean1 = null;
      try
      {
         bean1 = home.create(key1, "testEntityIdentity");
      }
      catch(DuplicateKeyException e)
      {
         bean1 = home.findByPrimaryKey(key1);
      }
      AccountPK key2 = new AccountPK("2");
      CtsCmpLocal bean2 = null;
      try
      {
         bean2 = home.create(key2, "testEntityIdentity");
      }
      catch(DuplicateKeyException e)
      {
         bean2 = home.findByPrimaryKey(key2);
      }
      CtsCmpLocalHome home2 = (CtsCmpLocalHome) ctx.lookup("ejbcts/LocalCMPBean2");
      CtsCmpLocal bean12 = null;
      try
      {
         bean12 = home2.create(key1, "testEntityIdentity");
      }
      catch(DuplicateKeyException e)
      {
         bean12 = home2.findByPrimaryKey(key1);
      }

      boolean isIdentical = false;
      isIdentical = bean1.isIdentical(bean1);
      log.debug(bean1+" isIdentical to "+bean1+" = "+isIdentical);
      assertTrue(bean1+" isIdentical to "+bean1, isIdentical == true);
      isIdentical = bean2.isIdentical(bean1);
      log.debug(bean2+" isIdentical to "+bean1+" = "+isIdentical);
      assertTrue(bean2+" isIdentical to "+bean1, isIdentical == false);
      isIdentical = bean1.isIdentical(bean12);
      log.debug(bean1+" isIdentical to "+bean12+" = "+isIdentical);
      assertTrue(bean1+" isIdentical to "+bean12, isIdentical == false);
   }
}
