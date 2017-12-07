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
package org.jboss.test.cmp2.commerce;

import javax.naming.InitialContext;
import javax.ejb.CreateException; 
import javax.ejb.EntityBean; 
import javax.ejb.EntityContext; 

import org.jboss.varia.autonumber.AutoNumberFactory;

public abstract class LineItemBean implements EntityBean {
	transient private EntityContext ctx;

	public Long ejbCreate() throws CreateException {
		setId(new Long(AutoNumberFactory.getNextInteger("LineItem").longValue()));
		return null;
	}

	public void ejbPostCreate() throws CreateException {
      try {
         InitialContext jndiContext = new InitialContext();

         ProductHome ph = (ProductHome) jndiContext.lookup("commerce/Product"); 
         Product p = ph.create();
      } catch(CreateException e) {
         throw e;
      } catch(Exception e) {
         e.printStackTrace();
         throw new CreateException("hosed");
      }
   }

	public Long ejbCreate(Long id) throws CreateException {
		setId(id);
		return null;
	}

	public void ejbPostCreate(Long id) throws CreateException {
   }

	public Long ejbCreate(Order order) throws CreateException {
		setId(new Long(AutoNumberFactory.getNextInteger("LineItem").longValue()));
		return null;
	}

	public void ejbPostCreate(Order order) throws CreateException {
      order.getLineItems().add((LineItem)ctx.getEJBLocalObject());
   }

	public abstract Long getId();
	public abstract void setId(Long id);
	
	public abstract Order getOrder();
	public abstract void setOrder(Order o);

	public abstract Product getProduct();
	public abstract void setProduct(Product p);
	
	public abstract int getQuantity();
	public abstract void setQuantity(int q);
	
	public abstract boolean getShipped();
	public abstract void setShipped(boolean shipped);
	
	public void setEntityContext(EntityContext ctx) { this.ctx = ctx; }
	public void unsetEntityContext() { this.ctx = null; }
	public void ejbActivate() { }
	public void ejbPassivate() { }
	public void ejbLoad() { }
	public void ejbStore() { }
	public void ejbRemove() { }
}
