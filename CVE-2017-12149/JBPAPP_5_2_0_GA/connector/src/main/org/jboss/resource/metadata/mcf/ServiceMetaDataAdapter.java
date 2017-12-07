/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.resource.metadata.mcf;

import java.util.List;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.jboss.system.metadata.ServiceMetaData;
import org.jboss.system.metadata.ServiceMetaData2ElementConverter;
import org.jboss.system.metadata.ServiceMetaDataParser;
import org.w3c.dom.Element;

/**
 * A ServiceMetaDataAdapter.
 * 
 * @author <a href="weston.price@jboss.org">Weston Price</a>
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 85945 $
 */
public class ServiceMetaDataAdapter extends XmlAdapter<Object, ServiceMetaData>
{
   
   @Override
   public ServiceMetaData unmarshal(Object e) throws Exception
   {
      ServiceMetaDataParser parser = new ServiceMetaDataParser((Element)e);      
      List<ServiceMetaData> services = parser.parse();      
      
      ServiceMetaData md = null;
      
      if(services != null)
      {
         md = services.get(0);
         
      }
      return md;
   }

   @Override
   public Element marshal(ServiceMetaData service) throws Exception
   {
      if(service == null)
         return null;
      ServiceMetaData2ElementConverter converter = new ServiceMetaData2ElementConverter();
      return converter.createServiceMetaDataElement(service);
   }
}
