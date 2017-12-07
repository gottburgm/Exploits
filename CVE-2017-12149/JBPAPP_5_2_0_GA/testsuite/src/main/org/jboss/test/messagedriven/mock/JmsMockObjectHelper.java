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

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * A JmsMockObjectHelper.
 * 
 * @author <a href="weston.price@jboss.com">Weston Price</a>
 * @version $Revision: 81036 $
 */
public class JmsMockObjectHelper
{

   
   public static DocumentBuilder getDocumentBuilder(){
      
      try
      {
         final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
         return builder;
      }
      catch (Exception e)
      {
         throw new JmsMockObjectException(e.getMessage());
         
      }      
      
   }
   
   public static Document getDocument(String xml){
      
      Document doc = null;
      
      try
      {
         DocumentBuilder builder = getDocumentBuilder();
         InputSource is = getInputSource(xml);
         return builder.parse(is);
      }
      catch (Exception e)
      {
         throw new JmsMockObjectException(e.getMessage());
         
      }      
            
   }

   private static InputSource getInputSource(String xml){
      
      InputStream is = null;
      
      try
      {
         is = Thread.currentThread().getContextClassLoader().getResourceAsStream(xml);
         return new InputSource(is);
      
      }finally{
         
         try
         {
            if (is != null)
            {
//               is.close();
            }
         }
         catch (Exception e)
         {
            // TODO: handle exception
         }         
      }
      
      
      
   }

   public static void main(String[] args)
   {
      String fileName = "org/jboss/test/messagedriven/mock/mock-provider-adapter.xml";
      Document doc = getDocument(fileName);
      
   }
   
   
}
