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
package org.jboss.test.jsf.managed;

import java.net.URL;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.faces.FacesException;

import org.jboss.test.web.ejb3.SimpleLocal;
import org.jboss.test.web.ejb3.SimpleStateful;
import org.jboss.test.web.ejb3.SimpleStateless;

/**
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 85945 $
 */
public class SimpleManagedBean
{

   @EJB(mappedName = "simpleStatefulMappedName")
   private SimpleStateful simpleStateful;
   
   @EJB
   private SimpleStateless simplestateless;
   
   @EJB
   SimpleLocal simpleLocal;
   
   @Resource(name = "url/Injection", mappedName = "http://jboss.org")
   private java.net.URL url;
   
   /**
    * Test the injection of a simple stateless Bean with a mappedName
    * 
    * @throws Exception
    */
   public boolean getTestStatelessBean() throws Exception
   {
      if(simplestateless == null)
         throw new FacesException("stateless bean in null.");
      
      return simplestateless.doSomething();
   }
   
   public boolean getTestStatefulBean() throws Exception
   {
      if(simpleStateful == null)
         throw new FacesException("stateful bean in null.");
      
      return simpleStateful.doSomething();      
   }
   
   /**
    * Test a url resource injection
    * 
    * @throws Exception
    */
   public boolean getTestURL() throws Exception
   {
      if(url == null)
         throw new FacesException("url is null.");
      
      URL url = new URL("http://jboss.org");
      if(! url.equals(this.url))
         throw new FacesException("url mismatch.");
      
      return true;
   }
   
   /**
    * Test the injected SimpleLocal.
    * 
    * @throws Exception
    */
   public boolean getTestSimpleLocal() throws Exception
   {
      if(simpleLocal == null)
         throw new FacesException("simpleLocal is null.");
      
      return simpleLocal.testLocal();
   }
   
}

