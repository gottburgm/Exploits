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
package org.jboss.mx.metadata;

import java.io.InputStream;

import org.jboss.logging.Logger;
import org.jboss.mx.service.ServiceConstants;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * XMBeanEntityResolver.java
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81026 $
 * 
 * @deprecated
 * @see org.jboss.util.xml.JBossEntityResolver
 */
public class XMBeanEntityResolver
   implements EntityResolver, ServiceConstants
{
   private static final Logger log = Logger.getLogger(XMBeanEntityResolver.class);

   public InputSource resolveEntity(String publicId, String systemId)
   {
      if (log.isTraceEnabled())
      {
         log.trace("resolveEntity() : publicId=" + publicId + ", systemId=" + systemId);
      }
      
      if (publicId == null)
      {
         // let the parser open a regular URI connection to systemId
         return null;
      }

      try
      {
         if (publicId.equals(PUBLIC_JBOSSMX_XMBEAN_DTD_1_0))
         {
            InputStream dtdStream = getClass().getResourceAsStream("/dtd/" + JBOSSMX_XMBEAN_DTD_1_0);
            return new InputSource(dtdStream);
         }
         else if (publicId.equals(PUBLIC_JBOSSMX_XMBEAN_DTD_1_1))
         {
            InputStream dtdStream = getClass().getResourceAsStream("/dtd/" + JBOSSMX_XMBEAN_DTD_1_1);
            return new InputSource(dtdStream);
         }
         else if (publicId.equals(PUBLIC_JBOSSMX_XMBEAN_DTD_1_2))
         {
            InputStream dtdStream = getClass().getResourceAsStream("/dtd/" + JBOSSMX_XMBEAN_DTD_1_2);
            return new InputSource(dtdStream);
         }         
         else
         {
            log.warn ("Cannot resolve entity: " + publicId);
         }
      }
      catch (Exception ignore)
      {
         log.error ("Cannot load local entity resource for: " + publicId);
      }
      return null;
   }
}
