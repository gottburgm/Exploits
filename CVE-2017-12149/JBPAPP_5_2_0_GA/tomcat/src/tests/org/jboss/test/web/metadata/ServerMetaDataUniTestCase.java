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
package org.jboss.test.web.metadata;

import java.io.InputStream;

import junit.framework.TestCase;

import org.jboss.util.xml.JBossEntityResolver;
import org.jboss.web.tomcat.metadata.ListenerMetaData;
import org.jboss.web.tomcat.metadata.ServerMetaData;
import org.jboss.xb.binding.Unmarshaller;
import org.jboss.xb.binding.UnmarshallerFactory;
import org.jboss.xb.binding.sunday.unmarshalling.SchemaBinding;
import org.jboss.xb.builder.JBossXBBuilder;

/**
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 85945 $
 */
public class ServerMetaDataUniTestCase extends TestCase
{

   public void test() throws Exception
   {
      SchemaBinding schema = JBossXBBuilder.build(ServerMetaData.class);
      Unmarshaller u = UnmarshallerFactory.newInstance().newUnmarshaller();
      u.setSchemaValidation(false);
      u.setValidation(false);
      u.setEntityResolver(new JBossEntityResolver());

      InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("test/metadata/server.xml");

      ServerMetaData m = ServerMetaData.class.cast(u.unmarshal(is, schema));
      assertNotNull(m);
      assertNotNull(m.getListeners());
      assertNotNull(m.getServices());
      
      assertEquals(2, m.getListeners().size());
      ListenerMetaData l = m.getListeners().get(0);
      assertNotNull(l);
      assertEquals(l.getClassName(), "org.apache.catalina.core.AprLifecycleListener");
      assertEquals("on", l.getAttributes().get("SSLEngine"));
   }
   
}

