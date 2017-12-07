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
package org.jboss.web.tomcat.metadata;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 85945 $
 */
public class ServiceMetaData extends AnyXmlMetaData
{
   private String name;
   private EngineMetaData engine;
   private ExecutorMetaData executor;
   private List<ConnectorMetaData> connectors;
   private List<ListenerMetaData> listeners;

   public String getName()
   {
      return name;
   }
   @XmlAttribute(name = "name")
   public void setName(String name)
   {
      this.name = name;
   }
   
   public EngineMetaData getEngine()
   {
      return engine;
   }
   @XmlElement(name = "Engine")
   public void setEngine(EngineMetaData engine)
   {
      this.engine = engine;
   }

   public ExecutorMetaData getExecutor()
   {
      return executor;
   }
   @XmlElement(name = "Executor")
   public void setExecutor(ExecutorMetaData executor)
   {
      this.executor = executor;
   }

   public List<ConnectorMetaData> getConnectors()
   {
      return connectors;
   }
   @XmlElement(name = "Connector")
   public void setConnectors(List<ConnectorMetaData> connectors)
   {
      this.connectors = connectors;
   }

   public List<ListenerMetaData> getListeners()
   {
      return listeners;
   }
   @XmlElement(name = "Listener")
   public void setListeners(List<ListenerMetaData> listeners)
   {
      this.listeners = listeners;
   }

}
