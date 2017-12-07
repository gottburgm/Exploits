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
package org.jboss.ejb.plugins;

import java.io.Serializable;

/**
 * A helper class for serializing stateful session beans.   
 *
 * Instances of this class are used to replace the non-serializable fields of StatefulSessionBean 
 * during serialization (passivation) and deserialization (activation)
 * Section 6.4.1 of the ejb1.1 specification states when this can happen.
 *      
 * @see org.jboss.ejb.plugins.SessionObjectOutputStream
 @see org.jboss.ejb.plugins.SessionObjectInputStream
 * @author <a href="mailto:sebastien.alborini@m4x.org">Sebastien Alborini</a>
 * @version $Revision: 81030 $
 */
class StatefulSessionBeanField implements Serializable
{
   static final long serialVersionUID = 1396957475833266905L;
   static final byte SESSION_CONTEXT = 0;
   static final byte USER_TRANSACTION = 1;

   byte type;

   StatefulSessionBeanField(byte type)
   {
      this.type = type;
   }

}

