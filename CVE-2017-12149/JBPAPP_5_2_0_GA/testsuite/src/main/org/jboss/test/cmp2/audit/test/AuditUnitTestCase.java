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
package org.jboss.test.cmp2.audit.test;

import junit.framework.Test;

import org.jboss.test.JBossTestCase;
import org.jboss.test.cmp2.audit.interfaces.AuditSession;
import org.jboss.test.cmp2.audit.interfaces.AuditSessionHome;

/**
 * Tests of audit fields
 *
 * @author    Adrian.Brock@HappeningTimes.com
 * @version   $Revision: 81036 $
 */
public class AuditUnitTestCase 
   extends JBossTestCase
{
   public static Test suite() throws Exception
   {
	return JBossTestCase.getDeploySetup(AuditUnitTestCase.class, "cmp2-audit.jar");
   }

   public AuditUnitTestCase(String name)
   {
	super(name);
   }

   public void testCreateAudit()
      throws Exception
   {
      AuditSession audit = getAuditSession();

      long beginTime = System.currentTimeMillis();
      audit.createAudit("createAudit");
      long endTime = System.currentTimeMillis();

      String failure = audit.fullAuditCheck("createAudit", "audituser1", beginTime, endTime);
      if (failure != null)
         fail(failure);
   }

   public void testUpdateAudit()
      throws Exception
   {
      AuditSession audit = getAuditSession();

      long beginCreateTime = System.currentTimeMillis();
      audit.createAudit("updateAudit");
      long endCreateTime = System.currentTimeMillis();
      long beginUpdateTime = System.currentTimeMillis();
      audit.updateAudit("updateAudit", "updateAuditString");
      long endUpdateTime = System.currentTimeMillis();

      String failure = audit.createAuditCheck("updateAudit", "audituser1", beginCreateTime, endCreateTime);
      if (failure != null)
         fail(failure);
      failure = audit.updateAuditCheck("updateAudit", "audituser2", beginUpdateTime, endUpdateTime);
      if (failure != null)
         fail(failure);
   }

   public void testUpdateAuditWithClear()
      throws Exception
   {
      AuditSession audit = getAuditSession();

      long beginCreateTime = System.currentTimeMillis();
      audit.createAudit("testUpdateAuditWithClear");
      long endCreateTime = System.currentTimeMillis();
      long beginUpdateTime = System.currentTimeMillis();
      audit.updateAudit("testUpdateAuditWithClear", "testUpdateAuditWithClearUpdate");
      long endUpdateTime = System.currentTimeMillis();

      String failure = audit.createAuditCheck("testUpdateAuditWithClear", "audituser1", beginCreateTime, endCreateTime);
      if (failure != null)
         fail(failure);
      failure = audit.updateAuditCheck("testUpdateAuditWithClear", "audituser2", beginUpdateTime, endUpdateTime);
      if (failure != null)
         fail(failure);
   }

   public void testUpdateAuditChangedNames()
      throws Exception
   {
      AuditSession audit = getAuditSession();

      long beginCreateTime = System.currentTimeMillis();
      audit.createAuditChangedNames("updateAudit");
      long endCreateTime = System.currentTimeMillis();
      long beginUpdateTime = System.currentTimeMillis();
      audit.updateAuditChangedNames("updateAudit", "updateAuditString");
      long endUpdateTime = System.currentTimeMillis();

      String failure = audit.createAuditChangedNamesCheck("updateAudit", "audituser1", beginCreateTime, endCreateTime);
      if (failure != null)
         fail(failure);
      failure = audit.updateAuditChangedNamesCheck("updateAudit", "audituser2", beginUpdateTime, endUpdateTime);
      if (failure != null)
         fail(failure);
   }

   public void testUpdateAuditMapped()
      throws Exception
   {
      AuditSession audit = getAuditSession();

      long beginCreateTime = System.currentTimeMillis();
      audit.createAuditMapped("updateAudit");
      long endCreateTime = System.currentTimeMillis();
      long beginUpdateTime = System.currentTimeMillis();
      audit.updateAuditMapped("updateAudit", "updateAuditString");
      long endUpdateTime = System.currentTimeMillis();

      String failure = audit.createAuditMappedCheck("updateAudit", "audituser1", beginCreateTime, endCreateTime);
      if (failure != null)
         fail(failure);
      failure = audit.updateAuditMappedCheck("updateAudit", "audituser2", beginUpdateTime, endUpdateTime);
      if (failure != null)
         fail(failure);
   }

   public void testCreateAuditMappedChangedFields()
      throws Exception
   {
      AuditSession audit = getAuditSession();

      long beginCreateTime = System.currentTimeMillis();
      audit.createAuditMappedChangedFields("createAuditChangedFields", "myUser", 1234);
      long endCreateTime = System.currentTimeMillis();

      String failure = audit.createAuditMappedCheck("createAuditChangedFields", "myUser", 1234, 1234);
      if (failure != null)
         fail(failure);
      failure = audit.updateAuditMappedCheck("createAuditChangedFields", "audituser1", beginCreateTime, endCreateTime);
      if (failure != null)
         fail(failure);
   }

   public void testUpdateAuditMappedChangedFields()
      throws Exception
   {
      AuditSession audit = getAuditSession();

      long beginCreateTime = System.currentTimeMillis();
      audit.createAuditMapped("updateAuditChangedFields");
      long endCreateTime = System.currentTimeMillis();
      audit.updateAuditMappedChangedFields("updateAuditChangedFields", "updateAuditString", "anotherUser", 4567);

      String failure = audit.createAuditMappedCheck("updateAuditChangedFields", "audituser1", beginCreateTime, endCreateTime);
      if (failure != null)
         fail(failure);
      failure = audit.updateAuditMappedCheck("updateAuditChangedFields", "anotherUser", 4567, 4567);
      if (failure != null)
         fail(failure);
   }

   private AuditSession getAuditSession()
   {
      try
      {
         return ((AuditSessionHome) getInitialContext().lookup("cmp2/audit/AuditSession")).create();
      }
      catch(Exception e)
      {
         fail("Exception in getAuditSession: " + e.getMessage());
         return null;
      }
   }
}
