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
package org.jboss.test.cmp2.audit.beans;

import java.util.Date;

import javax.ejb.CreateException;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;

/**
 * An entity bean with audit fields mapped to CMP fields.
 *
 * @author    Adrian.Brock@HappeningTimes.com
 * @version   $Revision: 81036 $
 */
public abstract class AuditMappedBean
   extends AuditBean
{
   public abstract String getCreatedBy();
   public abstract void setCreatedBy(String s);
   public abstract Date getCreatedTime();
   public abstract void setCreatedTime(Date d);
   public abstract String getUpdatedBy();
   public abstract void setUpdatedBy(String s);
   public abstract Date getUpdatedTime();
   public abstract void setUpdatedTime(Date d);
}
