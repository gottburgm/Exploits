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
package org.jboss.test.bootstrapdependencies.jbas5349.ejb2;


import javax.ejb.EntityBean;
import javax.ejb.CreateException;
import javax.ejb.EntityContext;

/**
 * A simple EJB2 CMP entity.
 * 
 * @author <a href="istudens@redhat.com">Ivo Studensky</a>
 * @version $Revision: 85945 $
 */
public abstract class SimpleEntityBean implements EntityBean 
{
   private static final long serialVersionUID = 1L;
   
   private transient EntityContext ctx;
   
   public abstract Long getId();
   public abstract void setId(Long id);

   public Long ejbCreate(Long id) throws CreateException
   {
      setId(id);
      return null;
   }

   public void ejbPostCreate(Long id) throws CreateException {}

   public void ejbActivate() {}

   public void ejbPassivate() {}

   public void ejbLoad() {}

   public void ejbStore() {}

   public void ejbRemove() {}

   public void setEntityContext(EntityContext context) 
   {
      this.ctx = context;
   }

   public void unsetEntityContext() 
   {
      this.ctx = null;
   }

}