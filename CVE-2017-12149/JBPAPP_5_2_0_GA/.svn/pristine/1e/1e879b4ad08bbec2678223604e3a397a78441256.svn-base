/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.samples.swaref;

import org.jboss.wsf.spi.annotation.WebContext;

import javax.activation.DataHandler;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.ejb.Stateless;
import javax.xml.bind.annotation.XmlAttachmentRef;
import javax.xml.ws.WebServiceException;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;

@Stateless
@WebService(name = "WrappedEndpoint", serviceName = "WrappedEndpointService")
@WebContext(contextRoot = "jaxws-swaref")
public class WrappedEndpointImpl implements WrappedEndpoint
{
   @WebMethod
   public DocumentPayload beanAnnotation(DocumentPayload dhw, String test) 
   {
      DataHandler dh;
      
      try {
         System.out.println("[TestServiceImpl] ---> Dans le service");

         // récupère la pièce attachée
         if (dhw != null && dhw.getData() != null) {
            dh=dhw.getData();
            dumpDH(dh);
         }
         else
         {
            System.out.println("[TestServiceImpl] ---> Le DataHandler est NULL.");
         }
      }
      catch (Exception ex) {
         ex.printStackTrace();
      }

      dh = new DataHandler("Server data", "text/plain") ;

      try{
         System.out.println("[TestServiceImpl] ---> Le DataHandler returned.");
         dumpDH(dh);
      }
      catch (Exception ex) {
         ex.printStackTrace();
      }

      return new DocumentPayload(dh);
   }


   @WebMethod
   @XmlAttachmentRef
   public DataHandler parameterAnnotation(DocumentPayload payload, String test, @XmlAttachmentRef DataHandler data)
   {
      try
      {
         Object dataContent = data.getContent();
         System.out.println("Got " + dataContent);
         if (dataContent instanceof InputStream)
         {
            ((InputStream)dataContent).close();
         }
         return new DataHandler("Server data", "text/plain");
      }
      catch (IOException e)
      {
         throw new WebServiceException(e);
      }
   }

   private static void dumpDH(DataHandler in_dh) throws Exception
   {
      InputStream is = in_dh.getInputStream();
      if (is != null) {
         System.out.println("[TestServiceImpl] ---> in_dh START : ");
         // récupère le contenu du fichier
         BufferedReader in = null;
         try
         {
            in = new BufferedReader(new InputStreamReader(is));
            String ligne="";
            ligne = in.readLine();
            while (ligne != null)
            {
               System.out.println(ligne);
               ligne = in.readLine();
            }
         }
         finally
         {
            if (in != null) in.close();
         }
         System.out.println("[TestServiceImpl] ---> END.");
      }
      else
      {
         System.out.println("[TestServiceImpl] ---> in_dh inputstream is null.");
      }

   }
}
