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
package org.jboss.test.cmp2.cmrtransaction.ejb;

import java.util.Collection;

import javax.ejb.EJBException;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.RemoveException;
import javax.ejb.EJBLocalObject;
import javax.ejb.CreateException;

import org.jboss.test.cmp2.cmrtransaction.interfaces.TreeLocal;


/**
 * @author  B Stansberry brian_stansberry@wanconcepts.com
 */
public abstract class TreeEntity implements EntityBean
{
    // ----------------------------------------------------  Abstract Accessors


    // CMP Fields

    public abstract String getId();
    public abstract void setId(String id);
    public abstract int getSortOrder();
    public abstract void setSortOrder(int sortOrder);
    public abstract Collection getMenuChildren();


    // CMR Fields
    public abstract void setMenuChildren(Collection menuChildren);
    public abstract TreeLocal getMenuParent();

    public abstract void setMenuParent(TreeLocal menuParent);

    // -------------------------------------------------------  Instance Fields


    private EntityContext entityContext = null;


    // --------------------------------------------------------  Bean Lifecycle

    public TreeEntity() {}


    public String ejbCreate(String name, TreeLocal parent)
        throws CreateException
    {
        setId(name);
        setSortOrder(0);

        return null;
    }

    public void ejbPostCreate(String name, TreeLocal parent)
    {
        setMenuParent(parent);
    }


    public void ejbRemove() throws RemoveException, EJBException {}
    public void ejbActivate() throws EJBException {}
    public void ejbPassivate() throws EJBException {}
    public void ejbLoad() throws EJBException {}
    public void ejbStore() throws EJBException {}

    public void setEntityContext(EntityContext entityContext) throws EJBException
    {
        this.entityContext = entityContext;
    }

    public EntityContext getEntityContext()
    {
        return entityContext;
    }

    public void unsetEntityContext() throws EJBException
    {
        entityContext = null;
    }


    // --------------------------------------------------------  Public Methods

    public void setPrecededBy(TreeLocal precedingNode)
    {
        EJBLocalObject self = getEntityContext().getEJBLocalObject();
        if (precedingNode == null)
        {
            setSortOrder(1);
        }
        else if (self.isIdentical(precedingNode))
        {
            System.out.println("MenuResource " + getId() + ": "
                                + "attempt to set menu resource "
                                + precedingNode.getId()
                                + " as preceding node; cannot set a node as "
                                + "its own preceding node.");
        }
        else if (ejbEquals(getMenuParent(), precedingNode.getMenuParent()))
        {
            setSortOrder(precedingNode.getSortOrder() + 1);
        }
        else
        {
            System.out.println("MenuResource " + getId() + ": "
                                + "attempt to set menu resource "
                                + precedingNode.getId()
                                + " as preceding node; invalid as node has a "
                                + "different menu parent.");
        }
    }


    // -------------------------------------------------------  Private Methods


    /**
     * Generic EJBObject equality algoritm that can handle null arguments.
     *
     * @param   a       The first object to compare
     * @param   b       The second object to compare
     *
     * @return  <code>true</code> if both arguments are null or neither is null
     *          and <code>a.isIdentical(b)</code>.  Otherwise, returns
     *          <code>false</code>
     *
     * @pre $none
     * @post $none
     */
    private boolean ejbEquals(EJBLocalObject a, EJBLocalObject b)
    {
        boolean result = false;

        if (a == b)
        {
            result = true; // deals w/ null equality
        }
        else if ((a == null) || (b == null))
        {
            result = false;
        }
        else
        {
            result = a.isIdentical(b);
        }

        return result;
    }


}
