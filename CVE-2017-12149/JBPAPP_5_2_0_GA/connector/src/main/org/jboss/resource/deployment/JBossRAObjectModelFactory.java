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
package org.jboss.resource.deployment;

import org.jboss.logging.Logger;
import org.jboss.resource.metadata.ConfigPropertyMetaData;
import org.jboss.resource.metadata.DependsMetaData;
import org.jboss.resource.metadata.JBossRAMetaData;
import org.jboss.xb.binding.ObjectModelFactory;
import org.jboss.xb.binding.UnmarshallingContext;
import org.xml.sax.Attributes;

/**
 * A JBossRAObjectModelFactory.
 * 
 * @author <a href="weston.price@jboss.com">Weston Price</a>
 * @version $Revision: 76970 $
 */
public class JBossRAObjectModelFactory implements ObjectModelFactory
{

   private boolean trace ;
   
   private Logger log = Logger.getLogger(JBossRAObjectModelFactory.class);
   
   public Object completeRoot(Object root, UnmarshallingContext arg1, String arg2, String arg3)
   {
      return root;
   }

   public Object newRoot(Object root, UnmarshallingContext navigator, String namespaceURI, String localName, Attributes atts)
   {
      
      if(localName == null || !localName.equals("jboss-ra"))
      {
         throw new IllegalArgumentException("Error invalid root element for jboss-ra.xml" + localName);
         
      }
      
      JBossRAMetaData ramd = new JBossRAMetaData();      
      return ramd;
   }
   
   public Object newChild(JBossRAMetaData ramd, UnmarshallingContext navigator, String namespaceURI, String localName, Attributes attrs)
   {      
      if(localName.equals("ra-config-property"))
      {
         ConfigPropertyMetaData cpmd = new ConfigPropertyMetaData();
         ramd.addProperty(cpmd);
         return cpmd;
         
      }      
      else if(localName.equals("depends"))
      {
    	  DependsMetaData dmd = new DependsMetaData();
    	  ramd.addDependsMetaData(dmd);
    	  return dmd;
      }
      else
      {
         return null;         
      }      
   }
   
   public void setValue(ConfigPropertyMetaData cpmd, UnmarshallingContext navigator, String namespaceURI, String localName, String value)
   {
      if (trace)
         log.trace("config property setValue: nuri=" + namespaceURI + " localName=" + localName + " value=" + value);
    
      if (localName.equals("ra-config-property-name"))
         cpmd.setName(value);
      else if (localName.equals("ra-config-property-type"))
         cpmd.setType(value);
      else if (localName.equals("ra-config-property-value"))
         cpmd.setValue(value);
      else
         throw new IllegalArgumentException("Unknown config property setValue: nuri=" + namespaceURI + " localName=" + localName + " value=" + value);
   }
   
   public void setValue(DependsMetaData dmd, UnmarshallingContext navigator, String namespaceURI, String localName, String value)
   {
      if (trace)
         log.trace("depends property setValue: nuri=" + namespaceURI + " localName=" + localName + " value=" + value);
      
      dmd.setDependsName(value);
   }


}
