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
package org.jboss.security.jce;

import java.security.Provider;
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.jboss.system.ServiceMBeanSupport;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The JCEProviderService is capable of loading one or more third party 
 * JCE providers at runtime. The configuration looks something like:<br>
 * <pre>
 *  &lt;mbean code="org.jboss.security.jce.JCEProviderService"
 *     name="Security:service=JCEProviderService"&gt;
 *     &lt;attribute name="JceProviders"&gt;
 *        &lt;providers&gt;
 *           &lt;provider class="org.bouncycastle.jce.provider.BouncyCastleProvider" position="3"/&gt;
 *           &lt;provider class="cryptix.provider.Cryptix"/&gt;
 *        &lt;/providers&gt;
 *     &lt;/attribute&gt;
 *  &lt;/mbean&gt;
 * </pre>
 * The required class attribute is the class name of the Provider to load. The optional position attribute
 * is the position in the provider list that this provider would like to be loaded.<p>
 * Any provider that already exists in the provider list will not be loaded again, and it will not be removed from
 * the list when the JCEProviderService is destroyed.
 * 
 * @author <a href="mailto:jasone@greenrivercomputing.com">Jason Essington</a>
 * @version $Revision: 85945 $
 */
public class JCEProviderService extends ServiceMBeanSupport implements JCEProviderServiceMBean
{
   private ArrayList addedProviders = new ArrayList();
   private Element providers;

   public void setJceProviders(Element element)
   {
      providers = element;
      if (getState() != UNREGISTERED)
      {
         synchronized (JCEProviderService.class)
         {
            removeProviders();
            loadProviders();
         }
      }
   }

   public Element getJceProviders()
   {
      return providers;
   }

   protected void createService() throws Exception
   {
      // Install the requested JCE providers
      synchronized (JCEProviderService.class)
      {
         loadProviders();
      }
   }

   protected void destroyService()
   {
      // Uninstall any JCE Providers that we actually loaded
      synchronized (JCEProviderService.class)
      {
         removeProviders();
      }
   }

   private void loadProviders()
   {
      int n = 0;
      if (providers != null)
      {
         addedProviders = new ArrayList();
         NodeList reqdProviders = providers.getElementsByTagName("provider");
         n = reqdProviders.getLength();
         //int providersLoaded = 0;

         for (int i = 0; i < n; i++)
         {
            Provider provider;
            Node reqdProvider = reqdProviders.item(i);

            String providerName;
            String providerClass;
            int requestedPosition = 0;

            if (Node.ELEMENT_NODE == reqdProvider.getNodeType())
            {
               Element prov = (Element) reqdProvider;
               if (prov.hasAttribute("class"))
               {
                  providerClass = prov.getAttribute("class");
               }
               else
               {
                  log.warn("A provider element must, at the very least, have a class attribute: " + prov);
                  continue;
               }

               try
               {
                  provider = (Provider) Class.forName(providerClass).newInstance();
               }
               catch (InstantiationException e1)
               {
                  log.warn("Unable to instantiate an instance of the JCE Provider class " + providerClass, e1);
                  continue;
               }
               catch (IllegalAccessException e1)
               {
                  log.warn("No permission to access the JCE Provider class " + providerClass, e1);
                  continue;
               }
               catch (ClassNotFoundException e1)
               {
                  log.warn("Could not find the JCE Provider class " + providerClass, e1);
                  continue;
               }
               catch (ClassCastException e1)
               {
                  log.warn("The Class " + providerClass + " is not a java.security.Provider");
                  continue;
               }

               providerName = provider.getName();

               if (prov.hasAttribute("position"))
               {
                  try
                  {
                     requestedPosition = Integer.parseInt(prov.getAttribute("position"));
                  }
                  catch (NumberFormatException e)
                  {
                     log.warn("the position '" + prov.getAttribute("position")
                           + "' is not a valid number. This provider has to go to the end of the line. " + prov);
                  }
               }

               int pos;
               if (requestedPosition < 1)
               {
                  pos = Security.addProvider(provider);
               }
               else
               {
                  pos = Security.insertProviderAt(provider, requestedPosition);
               }

               if (pos == -1)
               {
                  int exPos = Arrays.asList(Security.getProviders()).indexOf(provider);
                  log.info("The provider " + providerName + " already exists at position " + exPos);
               }
               else if (requestedPosition >= 1 && pos != requestedPosition)
               {
                  log.info("The position " + requestedPosition + " was requested for Provider " + providerName
                        + " but it was added at position " + pos);
                  addedProviders.add(providerName);
               }
               else
               {
                  log.info("The Provider " + providerName + " was added at position " + pos);
                  addedProviders.add(providerName);
               }
            }
            else
            {
               if (log.isDebugEnabled())
                  log.debug("Ignoring node" + reqdProvider);
            }
         }

         if (addedProviders.size() == 1)
            log.info(addedProviders.size() + " JCE Provider was actually loaded.");
         else
            log.info(addedProviders.size() + " JCE Providers were actually loaded.");

      }
      if (n < 1)
         log.info("No JCE Providers were requested.");
   }
   private void removeProviders()
   {
      for (Iterator iter = addedProviders.iterator(); iter.hasNext();)
      {
         String providerName = (String) iter.next();
         try
         {
            Security.removeProvider(providerName);
         }
         catch (Exception e)
         {
            log.warn("Failed to remove Provider " + providerName);
         }
      }
   }
}
