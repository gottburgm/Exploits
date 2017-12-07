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
package org.jboss.jmx.adaptor.snmp.agent;

import java.util.ArrayList;

import org.jboss.jmx.adaptor.snmp.config.attribute.AttributeMappings;
import org.jboss.jmx.adaptor.snmp.config.attribute.ManagedBean;
import org.jboss.jmx.adaptor.snmp.config.attribute.MappedAttribute;
import org.jboss.logging.Logger;
import org.jboss.xb.binding.ObjectModelFactory;
import org.jboss.xb.binding.UnmarshallingContext;
import org.xml.sax.Attributes;

/**
 * Parse the mapping of JMX mbean attributes to SNMP OIDs
 * 
 * @author <a href="mailto:hwr@pilhuhn.de">Heiko W. Rupp</a>
 * @version $Revision: 81038 $
 */
public class AttributeMappingsBinding implements ObjectModelFactory
{
   private static Logger log = Logger.getLogger(AttributeMappingsBinding.class);
	
	public Object newRoot(Object root, UnmarshallingContext ctx,
			String namespaceURI, String localName, Attributes attrs)
   {
	   if (!localName.equals("attribute-mappings"))
      {
	      throw new IllegalStateException("Unexpected root " + localName + ". Expected <attribute-mappings>");
		}
	   return new AttributeMappings();
	}

	public Object completeRoot(Object root, UnmarshallingContext ctx, String uri, String name)
   {
	   return root;
	}

	public void setValue(AttributeMappings mappings, UnmarshallingContext navigator,
		      String namespaceUri, String localName, String value)
	{
	}	
	
	public Object newChild(AttributeMappings mappings, UnmarshallingContext navigator,
			String namespaceUri, String localName, Attributes attrs)
	{
		if ("mbean".equals(localName))
      {
			String name = attrs.getValue("name");
			String oidPrefix = attrs.getValue("oid-prefix");
			ManagedBean child = new ManagedBean();
			child.setName(name);
			child.setOidPrefix(oidPrefix);
			if (log.isTraceEnabled())
				log.trace("newChild: " + child.toString());
			return child;
		}
		return null;
	}
	
	public void addChild(AttributeMappings mappings, ManagedBean mbean,
			UnmarshallingContext navigator, String namespaceURI, String localName) 
	{
		mappings.addMonitoredMBean(mbean);
	}
	
	public Object newChild(ManagedBean mbean, UnmarshallingContext navigator,
			String namespaceUri, String localName, Attributes attrs)
	{
		
		MappedAttribute attribute = null;
		if ("attribute".equals(localName)) {
			String oid = attrs.getValue("oid");
			String name = attrs.getValue("name");
			String mode = attrs.getValue("mode");
			attribute = new MappedAttribute();
			if ("rw".equalsIgnoreCase(mode))
            attribute.setReadWrite(true);
			attribute.setName(name);
			attribute.setOid(oid);
		}
		return attribute;
	}
	
	public void addChild(ManagedBean mbean, MappedAttribute attribute,
			UnmarshallingContext navigator, String namespaceURI, String localName)
	{
		if (mbean.getAttributes() == null)
         mbean.setAttributes(new ArrayList());
		
		mbean.getAttributes().add(attribute);
	}
}
