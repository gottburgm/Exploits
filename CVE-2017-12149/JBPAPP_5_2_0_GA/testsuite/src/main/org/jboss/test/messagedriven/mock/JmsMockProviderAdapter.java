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
package org.jboss.test.messagedriven.mock;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingException;

import org.jboss.jms.jndi.JMSProviderAdapter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A MockJMSProviderAdapter.
 * 
 * @author <a href="weston.price@jboss.com">Weston Price</a>
 * @version $Revision: 81036 $
 */
public class JmsMockProviderAdapter implements JMSProviderAdapter, JmsMockObject
{
   private static final String DEFAULT_URL = "org/jboss/test/messagedriven/mock/mock-provider-adapter.xml";
   
   private String name;
   private String factoryRef;
   private String queueFactoryRef;
   private String topicFactoryRef;
   
   
   //TODO create mock provider adapter xml file.
   public String getFactoryRef()
   {
      return this.factoryRef;
   }

   public Context getInitialContext() throws NamingException
   {
     return null;
     
   }

   public String getName()
   {
      return this.name;
   }

   public Properties getProperties()
   {
      // TODO Auto-generated method stub
      return null;
   }

   public String getQueueFactoryRef()
   {
      // TODO Auto-generated method stub
      return this.queueFactoryRef;
   }

   public String getTopicFactoryRef()
   {
      // TODO Auto-generated method stub
      return this.topicFactoryRef;
   }

   public void setFactoryRef(String newFactoryRef)
   {
      this.factoryRef = newFactoryRef;

   }

   public void setName(String name)
   {
      this.name = name;
   }

   public void setProperties(Properties properties)
   {
      // TODO Auto-generated method stub

   }

   public void setQueueFactoryRef(String newQueueFactoryRef)
   {
      this.queueFactoryRef = newQueueFactoryRef;
      
   }

   public void setTopicFactoryRef(String newTopicFactoryRef)
   {
      this.topicFactoryRef = newTopicFactoryRef;
   }
   
   public void load(final String xml)
   {
      
      Document doc = JmsMockObjectHelper.getDocument(xml);
      
      final NodeList nl = doc.getDocumentElement().getChildNodes();
      
      for(int i = 0; i < nl.getLength(); i++){
         
         Node node = (Node)nl.item(i);
         
         if(node.getNodeType() == Node.ELEMENT_NODE){
               
            Element elem = (Element)node;
            String nodeName = elem.getNodeName();
            
            NodeList childNodes = elem.getChildNodes();
            
            for(int j = 0; j < childNodes.getLength(); j++){
               
               Node child = (Node)childNodes.item(j);
               if(child.getNodeType() == Node.TEXT_NODE){
//                  setValue(nodeName, child.getTextContent());
                  
               }
            }
            
         }
         
      }
      
   }

   public void load(Element xml)
   {
    
   }

   public void load()
   {
    
      load(DEFAULT_URL);
      
   }
   
   public static void main(String[] args)
   {
      JmsMockProviderAdapter a = new JmsMockProviderAdapter();
      a.load();
      
   }
   
   private void setValue(String name, String value){

      if(name.equals("name")){
         setName(value);

      }else if(name.equals("factoryRef")){
         
         setFactoryRef(value);
         
      }else if(name.equals("queueFactoryRef")){
         
         setQueueFactoryRef(value);
         
      }else if(name.equals("topicFactoryRef")){
         
         setTopicFactoryRef(value);
      }
   
   }


}
