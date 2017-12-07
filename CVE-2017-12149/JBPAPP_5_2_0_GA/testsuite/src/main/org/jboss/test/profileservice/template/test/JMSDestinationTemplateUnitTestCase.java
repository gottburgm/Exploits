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
package org.jboss.test.profileservice.template.test;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jboss.managed.api.DeploymentTemplateInfo;
import org.jboss.metatype.api.types.CompositeMetaType;
import org.jboss.metatype.api.types.MapCompositeMetaType;
import org.jboss.metatype.api.types.SimpleMetaType;
import org.jboss.metatype.api.values.CompositeValue;
import org.jboss.metatype.api.values.CompositeValueSupport;
import org.jboss.metatype.api.values.MapCompositeValueSupport;
import org.jboss.metatype.api.values.MetaValue;
import org.jboss.metatype.api.values.SimpleValueSupport;
import org.jboss.profileservice.management.templates.JmsDestinationTemplateInfo;

/**
 * JMSDestination Queue and Topic template testcase.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public class JMSDestinationTemplateUnitTestCase extends AbstractTemplateTest
{

   /** The security mapper meta types. */
   private static MapCompositeMetaType mapMetaType;
   private static CompositeMetaType compositeMetaType;
   
   static
   {
//      SecurityConfigMapper mapper = new SecurityConfigMapper();
//      mapMetaType = (MapCompositeMetaType) mapper.getMetaType();
//      compositeMetaType = SecurityConfigMapper.composite;
   }
   
   public JMSDestinationTemplateUnitTestCase(String name)
   {
      super(name);
   }

   public void testQueueTemplateInfo() throws Exception
   {
//      DeploymentTemplateInfo info = assertTemplate("QueueTemplate",
//            QueueServiceMO.class);
//      
//      info.getProperties().get("name").setValue(new SimpleValueSupport(SimpleMetaType.STRING, "Test"));
//      info.getProperties().get("JNDIName").setValue(new SimpleValueSupport(SimpleMetaType.STRING, "TestJNDIName"));
//      
//      info.getProperties().get("serverPeer").setValue(new SimpleValueSupport(SimpleMetaType.STRING, "jboss.messaging.destination:service=Queue"));
//      info.getProperties().get("DLQ").setValue(new SimpleValueSupport(SimpleMetaType.STRING, "jboss.messaging.destination:service=Queue,name=PrivateDLQ"));
//      info.getProperties().get("expiryQueue").setValue(new SimpleValueSupport(SimpleMetaType.STRING, "jboss.messaging.destination:service=Queue,name=PrivateExpiryQueue"));
//      info.getProperties().get("securityConfig").setValue(createComposityType());
//      
//      DeploymentTemplate t = new JmsDestinationTemplate();
//      // Try to apply the template and delete the file afterwards
//      t.applyTemplate(info);
   }
   
   public void testTopicTemplateInfo() throws Exception
   {
//      DeploymentTemplateInfo info = assertTemplate("TopicTemplate",
//            TopicServiceMO.class);
//      
//      info.getProperties().get("name").setValue(new SimpleValueSupport(SimpleMetaType.STRING, "Test"));
//      info.getProperties().get("JNDIName").setValue(new SimpleValueSupport(SimpleMetaType.STRING, "TestJNDIName"));      
//      info.getProperties().get("serverPeer").setValue(new SimpleValueSupport(SimpleMetaType.STRING, "jboss.messaging.destination:service=Queue"));
//      info.getProperties().get("DLQ").setValue(new SimpleValueSupport(SimpleMetaType.STRING, "jboss.messaging.destination:service=Queue,name=PrivateDLQ"));      
//      info.getProperties().get("expiryQueue").setValue(new SimpleValueSupport(SimpleMetaType.STRING, "jboss.messaging.destination:service=Queue,name=PrivateExpiryQueue"));
//      info.getProperties().get("securityConfig").setValue(createComposityType());
//     
//      DeploymentTemplate t = new JmsDestinationTemplate();
//      // Try to apply the template and delete the file afterwards
//      t.applyTemplate(info);  
   }
   
   @Override
   DeploymentTemplateInfo createDeploymentInfo(String name, Class<?> attachment) throws Exception
   {
      DeploymentTemplateInfo info = getFactory().createTemplateInfo(JmsDestinationTemplateInfo.class, attachment, name, null);
      ((JmsDestinationTemplateInfo)info).start();
      ((JmsDestinationTemplateInfo)info).setDestinationType(name);
      return info;
   }
   
   protected MapCompositeValueSupport createComposityType()
   {
      Map<String, MetaValue> values = new HashMap<String, MetaValue>();

      values.put("admin", createCompositeValue(true, true, true));
      values.put("publisher", createCompositeValue(true, true, false));
      values.put("user", createCompositeValue(true, null, null));

      return new MapCompositeValueSupport(values, mapMetaType);
   }
   
   protected CompositeValue createCompositeValue(Boolean read, Boolean write, Boolean create)
   {
      Map<String, MetaValue> map = new HashMap<String, MetaValue>();

      map.put("read", new SimpleValueSupport(SimpleMetaType.BOOLEAN, read));
      map.put("write", new SimpleValueSupport(SimpleMetaType.BOOLEAN, write));
      map.put("create", new SimpleValueSupport(SimpleMetaType.BOOLEAN, create));

      return new CompositeValueSupport(compositeMetaType, map);
   }
   
   @Override
   protected Collection<String> getExcludes()
   {
      return Collections.singleton("destinationType");
   }

}

