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
package org.jboss.test.jca.support;

import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;

import org.jboss.resource.metadata.mcf.DataSourceDeploymentMetaData;
import org.jboss.resource.metadata.mcf.LocalDataSourceDeploymentMetaData;
import org.jboss.resource.metadata.mcf.ManagedConnectionFactoryDeploymentGroup;
import org.jboss.resource.metadata.mcf.ManagedConnectionFactoryDeploymentMetaData;
import org.jboss.resource.metadata.mcf.NonXADataSourceDeploymentMetaData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/**
 * A ManagedDeploymentSupportHelper.
 * 
 * @author <a href="weston.price@jboss.org">Weston Price</a>
 * @version $Revision: 85945 $
 */
public class ManagedDeploymentSupportHelper
{
   
   private static final Class[] CTX_CLASS = {ManagedConnectionFactoryDeploymentGroup.class};
   
   public static String marshalResourceAsString(ManagedConnectionFactoryDeploymentGroup group) throws Exception
   {
      Class[] classes = {ManagedConnectionFactoryDeploymentGroup.class, ManagedConnectionFactoryDeploymentMetaData.class, LocalDataSourceDeploymentMetaData.class, NonXADataSourceDeploymentMetaData.class, DataSourceDeploymentMetaData.class};      
      JAXBContext context = JAXBContext.newInstance(classes);      
      Marshaller m = context.createMarshaller();      
      JAXBElement element = new JAXBElement(new QName("", "datasources"), group.getClass(), group);
      StringWriter w = new StringWriter();
      m.marshal(element, w);      
      return w.toString();      
   }
   
   public static ManagedConnectionFactoryDeploymentGroup unmarshalSource(String content) throws Exception
   {
      JAXBContext ctx = JAXBContext.newInstance(CTX_CLASS);
      Unmarshaller um = ctx.createUnmarshaller();
      StringReader r = new StringReader(content);
      InputSource is = new InputSource(r);
      Source s = new SAXSource(is);
      JAXBElement<ManagedConnectionFactoryDeploymentGroup> elem = um.unmarshal(s, ManagedConnectionFactoryDeploymentGroup.class);
      ManagedConnectionFactoryDeploymentGroup group = elem.getValue();
      return group;
   }
   public static ManagedConnectionFactoryDeploymentGroup unmarshalResource(String resourceName) throws Exception
   {
      JAXBContext ctx = JAXBContext.newInstance(CTX_CLASS);
      Unmarshaller um = ctx.createUnmarshaller();
      Document d = getDocumentForResource(resourceName);
      JAXBElement<ManagedConnectionFactoryDeploymentGroup> elem = um.unmarshal(d, ManagedConnectionFactoryDeploymentGroup.class);
      ManagedConnectionFactoryDeploymentGroup group = elem.getValue();
      //      ManagedConnectionFactoryDeploymentGroup group = elem.getValue();
      return group;      
   }
   
   public static Document getDocumentForResource(String resourceName) throws Exception
   {
      InputStream is = null;
      try
      {
         ClassLoader cl = Thread.currentThread().getContextClassLoader();
         is = cl.getResourceAsStream(resourceName);
         Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
         return doc;
         
      }finally
      {
         if(is != null)
         {
            is.close();
         }
      }
   
   }
   public static boolean isValidDeployment(String resourceName, ManagedConnectionFactoryDeploymentMetaData md, String...elements) throws Exception   
   {
      Document doc = getDocumentForResource(resourceName);
      
      List<Element> candidates = new ArrayList<Element>();

      for (int i = 0; i < elements.length; i++)
      {
         String elemName = elements[i];
         candidates.add((Element)doc.getElementsByTagName(elemName).item(0));

      }
      
      return isValidDeployment(md, candidates);
      
      
   }
   public static boolean isValidDeployment(ManagedConnectionFactoryDeploymentMetaData md, List<Element> elements)
   {
      boolean results = false;
      
      for (Element elem : elements)
      {
         String name = elem.getNodeName();
         String value = elem.getTextContent();
         
         if(name.equals("jndi-name"))
         {
            results = value.equals(md.getJndiName());            
         }
      }
      
      return results;
   }
   
   public static boolean hasAnnotation(ManagedConnectionFactoryDeploymentMetaData md, String name) throws Exception
   {
      Annotation[] a = md.getClass().getAnnotations();
      boolean results = true;
      
      for (int i = 0; i < a.length; i++)
      {
         if(a[i] instanceof XmlElement)
         {
            XmlElement xme = (XmlElement)a[i];
            xme.name().equals(name);
            results = true;
         }
      } 
      return results;
   }
   
   private static List<Annotation> getAllAnnotationsForClass(Class c, List<Annotation> a)
   {
      if(a == null)
      {
         a = new ArrayList<Annotation>();         
      }
      
      if(c.equals(Object.class))
      {
         return a;
      }
      
      else
      {
         a.addAll(Arrays.asList(c.getDeclaredAnnotations()));
      }
     
      
      return getAllAnnotationsForClass(c.getSuperclass(), a);

   }
   private static List<Field> getAllFieldsForClass(Class c, List<Field> fields)
   {
      if(fields == null)
      {
         fields = new ArrayList<Field>();         
      }
      
      if(c.equals(Object.class))
      {
         return fields;
      }
      
      else
      {
         fields.addAll(Arrays.asList(c.getDeclaredFields()));
      }
     
      
      return getAllFieldsForClass(c.getSuperclass(), fields);

   }
}
