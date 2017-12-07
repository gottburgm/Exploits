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
package org.jboss.test.jmx.loading;

import javax.ejb.SessionBean;


/**
 * ConcreteBean.java
 *
 *
 * @author <a href="mailto:julien_viet@yahoo.fr">Julien Viet</a>
 * @version
 *
 *
 * @ejb:bean        name="Concrete"
 *                  jndi-name="loading/cpmanifest"
 *                  view-type="remote"
 *                  type="Stateless"
 *
 * @ejb:home        extends="javax.ejb.EJBHome"
 *
 * @ejb:interface   extends="javax.ejb.EJBObject"
 *
 */

public class ConcreteBean
   extends AbstractBean
{

   /**
    * @ejb:create-method
    */
   public void ejbCreate() 
   {
   }

}
