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
package org.jboss.test.cmp2.audit.interfaces;

import java.rmi.RemoteException;
import javax.ejb.EJBObject;

/**
 * Session facade for audit testing.
 *
 * @author    Adrian.Brock@HappeningTimes.com
 * @version   $Revision: 81036 $
 */
public interface AuditSession
   extends EJBObject
{
   public void createAudit(String id)
      throws RemoteException;
   public void updateAudit(String id, String stringValue)
      throws RemoteException;

   public String fullAuditCheck(String id, String user, long beginTime, long endTime)
      throws RemoteException;
   public String createAuditCheck(String id, String user, long beginTime, long endTime)
      throws RemoteException;
   public String updateAuditCheck(String id, String user, long beginTime, long endTime)
      throws RemoteException;

   public void createAuditChangedNames(String id)
      throws RemoteException;
   public void updateAuditChangedNames(String id, String stringValue)
      throws RemoteException;
   public String createAuditChangedNamesCheck(String id, String user, long beginTime, long endTime)
      throws RemoteException;
   public String updateAuditChangedNamesCheck(String id, String user, long beginTime, long endTime)
      throws RemoteException;

   public void createAuditMapped(String id)
      throws RemoteException;
   public void updateAuditMapped(String id, String stringValue)
      throws RemoteException;
   public void createAuditMappedChangedFields(String id, String user, long time)
      throws RemoteException;
   public void updateAuditMappedChangedFields(String id, String stringValue, String user, long time)
      throws RemoteException;
   public String createAuditMappedCheck(String id, String user, long beginTime, long endTime)
      throws RemoteException;
   public String updateAuditMappedCheck(String id, String user, long beginTime, long endTime)
      throws RemoteException;

}
