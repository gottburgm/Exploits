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
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 87645 $
 */
@XmlRootElement(name="Context")
public class ContextMetaData extends AnyXmlMetaData
{
   private String name;
   private String docBase;
   private String path;
   private List<ListenerMetaData> listeners;
   private List<ValveMetaData> valves;
   private List<String> instanceListeners;
   private LoaderMetaData loader;
   private ManagerMetaData manager;
   private RealmMetaData realm;
   private List<ParameterMetaData> parameters;
   private ResourcesMetaData resources;
   private SessionCookieMetaData sessionCookie;
   // FIXME: no support for the naming elements (which might be the right thing to do)
   // FIXME: no WatchedResource, WrapperLifecycle, WrapperListener
   
   public String getName()
   {
      return name;
   }
   @XmlAttribute(name = "name")
   public void setName(String name)
   {
      this.name = name;
   }
   
   public String getDocBase()
   {
      return docBase;
   }
   @XmlAttribute(name = "docBase")
   public void setDocBase(String docBase)
   {
      this.docBase = docBase;
   }
   
   public String getPath()
   {
      return path;
   }
   @XmlAttribute(name = "path")
   public void setPath(String path)
   {
      this.path = path;
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

   public RealmMetaData getRealm()
   {
      return realm;
   }
   @XmlElement(name = "Realm")
   public void setRealm(RealmMetaData realm)
   {
      this.realm = realm;
   }
   
   public List<ValveMetaData> getValves()
   {
      return valves;
   }
   @XmlElement(name = "Valve")
   public void setValves(List<ValveMetaData> valves)
   {
      this.valves = valves;
   }

   public List<String> getInstanceListeners()
   {
      return instanceListeners;
   }
   @XmlElement(name = "InstanceListener")
   public void setInstanceListeners(List<String> instanceListeners)
   {
      this.instanceListeners = instanceListeners;
   }

   public LoaderMetaData getLoader()
   {
      return loader;
   }
   @XmlElement(name = "Loader")
   public void setLoader(LoaderMetaData loader)
   {
      this.loader = loader;
   }
   
   public ManagerMetaData getManager()
   {
      return manager;
   }
   @XmlElement(name = "Manager")
   public void setManager(ManagerMetaData manager)
   {
      this.manager = manager;
   }
   
   public List<ParameterMetaData> getParameters()
   {
      return parameters;
   }
   @XmlElement(name = "Parameters")
   public void setParameters(List<ParameterMetaData> parameters)
   {
      this.parameters = parameters;
   }

   public ResourcesMetaData getResources()
   {
      return resources;
   }
   @XmlElement(name = "Resources")
   public void setResources(ResourcesMetaData resources)
   {
      this.resources = resources;
   }
   
   public SessionCookieMetaData getSessionCookie()
   {
      return sessionCookie;
   }
   @XmlElement(name = "SessionCookie")
   public void setSessionCookie(SessionCookieMetaData sessionCookie)
   {
      this.sessionCookie = sessionCookie;
   }
   
}
