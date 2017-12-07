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
package org.jboss.test.jca.test;

import javax.naming.InitialContext;

import junit.framework.Test;

import org.jboss.test.JBossTestCase;
import org.jboss.test.jca.interfaces.PreparedStatementHome;
import org.jboss.test.jca.interfaces.PreparedStatementRemote;

/** Tests of the prepared statement cache.
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public class PSCacheUnitTestCase extends JBossTestCase
{
   public PSCacheUnitTestCase (String name)
   {
      super(name);
   }

   public void testPreparedStatementCache() throws Exception
   {
      log.info("+++ testPreparedStatementCache");
      InitialContext ctx = new InitialContext();
      PreparedStatementHome home = (PreparedStatementHome) ctx.lookup("PreparedStatementBean");
      PreparedStatementRemote bean = home.create("key1", "name1");
      bean.testPreparedStatementCache();
      bean.remove();
   }

   public void testPreparedStatementCacheDoubleClose() throws Exception
   {
      log.info("+++ testPreparedStatementCacheDoubleClose");
      InitialContext ctx = new InitialContext();
      PreparedStatementHome home = (PreparedStatementHome) ctx.lookup("PreparedStatementBean");
      PreparedStatementRemote bean = home.create("key1", "name1");
      bean.testPreparedStatementCache();
      bean.remove();
   }

   public void testBasicPreparedStatement() throws Exception
   {
      log.info("+++ testBasicPreparedStatement");
      InitialContext ctx = new InitialContext();
      PreparedStatementHome home = (PreparedStatementHome) ctx.lookup("PreparedStatementBean");
      PreparedStatementRemote bean = home.create("key1", "name1");
      bean.hashEntityTable();
      bean.remove();
   }

   public void testCallableStatementCache() throws Exception
   {
      log.info("+++ testCallableStatementCache");
      InitialContext ctx = new InitialContext();
      PreparedStatementHome home = (PreparedStatementHome) ctx.lookup("PreparedStatementBean");
      PreparedStatementRemote bean = home.create("key2", "name2");
      bean.testCallableStatementCache("callIdentitySQL");
      bean.remove();
   }

   public void testCallableStatementCacheDoubleClose() throws Exception
   {
      log.info("+++ testCallableStatementCacheDobuleClose");
      InitialContext ctx = new InitialContext();
      PreparedStatementHome home = (PreparedStatementHome) ctx.lookup("PreparedStatementBean");
      PreparedStatementRemote bean = home.create("key2", "name2");
      bean.testCallableStatementCache("callIdentitySQL");
      bean.remove();
   }
   
   public void testBasicCallableStatement() throws Exception 
   { 
      log.info("+++ testBasicCallableStatement"); 
      InitialContext ctx = new InitialContext(); 
      PreparedStatementHome home = (PreparedStatementHome) ctx.lookup("PreparedStatementBean"); 
      PreparedStatementRemote bean = home.create("key2", "name2"); 
      String result = bean.executeStoredProc("callIdentitySQL"); 
      log.info("callIdentitySQL result="+result); 
      bean.remove(); 
   }
   
   public static Test suite() throws Exception
   {
      return getDeploySetup(PSCacheUnitTestCase.class, "pscache.jar");
   }
}
