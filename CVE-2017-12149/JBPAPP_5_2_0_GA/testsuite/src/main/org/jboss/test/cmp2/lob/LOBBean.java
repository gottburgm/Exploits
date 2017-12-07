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

import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.CreateException;
import javax.ejb.RemoveException;
import javax.ejb.FinderException;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Collection;

/**
 * Implementaton of a CMP2 entity bean that is intended to demonstrate the
 * storage of large text and binary objects.
 *
 * @see javax.ejb.EntityBean
 *
 * @version <tt>$Revision: 81036 $</tt>
 * @author  <a href="mailto:steve@resolvesw.com">Steve Coy</a>
 * @author  <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 */
public abstract class LOBBean implements EntityBean
{
   private EntityContext mEntityContext;

   /**
    * Returns the primary key
    * @return Integer
    */
   public abstract Integer getId();

   /**
    * Sets the primary key.
    * @param id
    */
   public abstract void setId(Integer id);

   /**
    * Returns the large string attribute.
    * @return String
    */
   public abstract String getBigString();

	/**
	 * Sets the value of the large string attribute.
     * The idea here is to store it in a CLOB object in associated database table
     * so that we can check the container's LOB functionality properly.
	 * @param s
	 */
   public abstract void setBigString(String s);

	/**
	 * Returns the content of the large binary object.
	 * @return byte[]
	 */
   public abstract byte[] getBinaryData();

	/**
	 * Sets the content of the large binary object.
	 * The idea here is to store it in a BLOB object in the associated database
	 * table so that we check the container's LOB functionality properly.
	 * @param data
	 */
   public abstract void setBinaryData(byte[] data);

   public abstract Object getObjectField();
   public abstract void setObjectField(Object obj);

   public abstract Map getMapField();
   public abstract void setMapField(Map map);

   public abstract List getListField();
   public abstract void setListField(List list);

   public abstract Set getSetField();
   public abstract void setSetField(Set set);

   public abstract ValueHolder getValueHolder();
   public abstract void setValueHolder(ValueHolder valueHolder);

   public abstract ValueHolder getCleanGetValueHolder();
   public abstract void setCleanGetValueHolder(ValueHolder valueHolder);

   public abstract ValueHolder getStateFactoryValueHolder();
   public abstract void setStateFactoryValueHolder(ValueHolder valueHolder);

   public abstract Integer getIntField();
   public abstract void setIntField(Integer i);

   public abstract Collection ejbSelectSelect(String query, Object[] params) throws FinderException;

   public Collection ejbHomeSelect(String query, Object[] params) throws FinderException
   {
      return ejbSelectSelect(query, params);
   }

   
   // EntityBean implementation

   public Integer ejbCreate(Integer id) throws CreateException
   {
      setId(id);
      setMapField(new HashMap());
      setListField(new ArrayList());
      setSetField(new HashSet());
      setValueHolder(new ValueHolder(null));
      setCleanGetValueHolder(new ValueHolder(null));
      setStateFactoryValueHolder(new ValueHolder(null));
      setIntField(new Integer(1));
      return null;
   }

   public void ejbPostCreate(Integer id) {}

   public void ejbActivate() {}
   public void ejbLoad() {}
   public void ejbPassivate() {}
   public void ejbRemove() throws RemoveException {}
   public void ejbStore() {}

   public void setEntityContext(EntityContext ctx)
   {
      mEntityContext = ctx;
   }

   public void unsetEntityContext()
   {
      mEntityContext = null;
   }
}
