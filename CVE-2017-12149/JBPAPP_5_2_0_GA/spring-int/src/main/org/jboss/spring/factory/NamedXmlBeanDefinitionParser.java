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
package org.jboss.spring.factory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jboss.util.naming.Util;
import org.jboss.logging.Logger;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.xml.BeanDefinitionParserDelegate;
import org.springframework.beans.factory.xml.DefaultBeanDefinitionDocumentReader;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Named bean definition parser.
 * 
 * @author <a href="mailto:ales.justin@genera-lynx.com">Ales Justin</a>
 */
public class NamedXmlBeanDefinitionParser extends DefaultBeanDefinitionDocumentReader implements Nameable, Instantiable
{
   protected static Logger log = Logger.getLogger(NamedXmlBeanDefinitionParser.class);

   public static final String BEAN_FACTORY_ELEMENT = "BeanFactory=\\(([^)]+)\\)";
   public static final String PARENT_BEAN_FACTORY_ELEMENT = "ParentBeanFactory=\\(([^)]+)\\)";
   public static final String INSTANTIATION_ELEMENT = "Instantiate=\\(([^)]+)\\)";

   private ConfigurableBeanFactory beanFactory;
   private String beanFactoryName;
   private boolean instantiate;

   public NamedXmlBeanDefinitionParser(ConfigurableBeanFactory beanFactory)
   {
      this.beanFactory = beanFactory;
   }

   protected void preProcessXml(Element root) throws BeanDefinitionStoreException
   {
      NodeList nl = root.getChildNodes();
      for (int i = 0; i < nl.getLength(); i++)
      {
         Node node = nl.item(i);
         if (node instanceof Element)
         {
            Element ele = (Element) node;
            if (BeanDefinitionParserDelegate.DESCRIPTION_ELEMENT.equals(node.getNodeName()))
            {
               String nodeValue = ele.getFirstChild().getNodeValue();
               if (log.isTraceEnabled())
                  log.trace("Bean names [description tag]: " + nodeValue);
               Matcher bfm = parse(nodeValue, BEAN_FACTORY_ELEMENT);
               if (bfm.find())
               {
                  beanFactoryName = bfm.group(1);
               }
               Matcher pbfm = parse(nodeValue, PARENT_BEAN_FACTORY_ELEMENT);
               if (pbfm.find())
               {
                  String parentBeanFactoryName = pbfm.group(1);
                  try
                  {
                     beanFactory.setParentBeanFactory((BeanFactory) Util.lookup(parentBeanFactoryName, BeanFactory.class));
                  }
                  catch (Exception e)
                  {
                     throw new BeanDefinitionStoreException("Failure during parent bean factory JNDI lookup: " + parentBeanFactoryName, e);
                  }
               }
               Matcher inst = parse(nodeValue, INSTANTIATION_ELEMENT);
               if (inst.find())
               {
                  instantiate = Boolean.parseBoolean(inst.group(1));
               }
            }
         }
      }
   }

   public String getName()
   {
      return beanFactoryName;
   }

   public boolean doInstantiate()
   {
      return instantiate;
   }

   private static Matcher parse(String value, String regexp)
   {
      Pattern pattern = Pattern.compile(regexp);
      return pattern.matcher(value);
   }
}
