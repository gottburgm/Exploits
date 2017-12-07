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
package test.compliance.core.notification;

import org.w3c.dom.Element;

/**
 * @author <a href="mailto:telrod@e2technologies.net">Tom Elrod</a>
 */
public interface InvokerTestMBean
{
   /**
    * @jmx:managed-attribute
    */
   String getSomething();

   /**
    * @jmx:managed-attribute
    */
   CustomClass getCustom();

   /**
    * @jmx:managed-attribute
    */
   void setCustom(CustomClass custom);

   /**
    * @jmx:managed-attribute
    */
   NonserializableClass getNonserializableClass();

   /**
    * @jmx:managed-attribute
    */
   void setNonserializableClass(NonserializableClass custom);

   /**
    * @jmx:managed-attribute
    */
   Element getXml();

   /**
    * @jmx:managed-attribute
    */
   void setXml(Element xml);

   /**
    * @jmx:managed-operation
    */
   CustomClass doSomething(CustomClass custom);

   /**
    * @jmx:managed-operation
    */
   CustomClass doSomething();

   /**
    * @jmx:managed-operation
    */
   void stop();
}
