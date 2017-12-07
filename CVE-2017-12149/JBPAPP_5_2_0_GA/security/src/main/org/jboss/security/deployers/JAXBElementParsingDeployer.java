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
package org.jboss.security.deployers;

import java.io.InputStream;
import java.util.Map;

import javax.xml.bind.JAXBContext;  
import javax.xml.bind.Unmarshaller;

import org.jboss.deployers.vfs.spi.deployer.JAXBDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit; 
import org.jboss.virtual.VirtualFile;
import org.xml.sax.InputSource;

/**
 * A parsing deployer that is capable of parsing
 * a JAXB model with the root element being
 * JAXBElement<T>
 * 
 * @author Anil.Saldhana@redhat.com
 * @since Mar 17, 2009
 */
@SuppressWarnings("unchecked")
public class JAXBElementParsingDeployer<T,V> extends JAXBDeployer 
{
   /** The JAXBContext */ 
   protected JAXBContext context;

   /** The properties */
   protected Map<String, Object> properties;
   
   protected Class<V> enclosed;
    
   /**
    * CTR
    * @param output  JAXBElement.class
    * @param enclosed Type enclosed by JAXBElement
    */
   public JAXBElementParsingDeployer(Class<T> output, Class<V> enclosed)
   {
      super(output); 
      this.enclosed = enclosed;
   } 
   
   /**
    * Create lifecycle
    * 
    * @throws Exception for any problem
    */
   @Override
   public void create() throws Exception
   {
      if (properties != null)
         context = JAXBContext.newInstance(new Class[] {enclosed}, properties);
      else
         context = JAXBContext.newInstance(enclosed);
   } 

   /**
    * Destroy lifecycle
    */
   public void destroy()
   {
      context = null;
   }

   @Override
   protected Object parse(VFSDeploymentUnit unit, VirtualFile file, Object root) throws Exception
   {
      Unmarshaller unmarshaller = context.createUnmarshaller();
      InputStream is = openStreamAndValidate(file);
      try
      {
         InputSource source = new InputSource(is);
         source.setSystemId(file.toURI().toString());
         Object o = unmarshaller.unmarshal(source);
         return getOutput().cast(o);
      }
      finally
      {
         try
         {
            is.close();
         }
         catch (Exception ignored)
         {
         }
      }
   } 
}
