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
package org.jboss.test.cmp2.lob;

import java.rmi.RemoteException;
import java.util.Map;
import java.util.Set;
import java.util.List;

/**
 *
 * @author  <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 */
public interface Facade
   extends javax.ejb.EJBObject
{
   public void createLOB(Integer id)
      throws Exception, RemoteException;
   public void removeLOB(Integer id)
      throws Exception, RemoteException;
   public void addMapEntry(Integer id, Object key, Object value)
      throws Exception, RemoteException;
   public Map getMapField(Integer id)
      throws Exception, RemoteException;
   public void addSetElement(Integer id, Object value)
      throws Exception, RemoteException;
   public Set getSetField(Integer id)
      throws Exception, RemoteException;
   public void addListElement(Integer id, Object value)
      throws Exception, RemoteException;
   public List getListField(Integer id)
      throws Exception, RemoteException;
   public void setBinaryData(Integer id, byte[] value)
      throws Exception, RemoteException;
   public void setBinaryDataElement(Integer id, int index, byte value)
      throws Exception, RemoteException;
   public byte getBinaryDataElement(Integer id, int index)
      throws Exception, RemoteException;
   public void setValueHolderValue(Integer id, String value)
      throws Exception, RemoteException;
   public String getValueHolderValue(Integer id)
      throws Exception, RemoteException;
   public void setCleanGetValueHolderValue(Integer id, String value)
      throws Exception, RemoteException;
   public void modifyCleanGetValueHolderValue(Integer id, String value)
      throws Exception, RemoteException;
   public String getCleanGetValueHolderValue(Integer id)
      throws Exception, RemoteException;
   public String getStateFactoryValueHolderValue(Integer id)
      throws Exception, RemoteException;
   public void setStateFactoryValueHolderValue(Integer id, String value)
      throws Exception, RemoteException;
   public void modifyStateFactoryValueHolderValue(Integer id, String value)
      throws Exception, RemoteException;
   boolean executeDynamicQuery(String query, Object[] params)
      throws Exception, RemoteException;
}
