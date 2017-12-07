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
package org.jboss.wsf.container.jboss50.deployment.tomcat;

import org.jboss.logging.Logger;
import org.jboss.metadata.javaee.spec.ParamValueMetaData;
import org.jboss.metadata.web.jboss.JBossServletMetaData;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.metadata.web.spec.ListenerMetaData;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.deployment.Endpoint;

import javax.xml.ws.WebServiceException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The rewriter for web.xml
 *
 * @author Thomas.Diesler@jboss.org
 * @since 19-May-2007
 */
public class WebMetaDataModifierImpl implements WebMetaDataModifier
{
   // logging support
   private static Logger log = Logger.getLogger(WebMetaDataModifierImpl.class);

   public RewriteResults modifyMetaData(Deployment dep)
   {
      JBossWebMetaData jbwmd = dep.getAttachment(JBossWebMetaData.class);
      if (jbwmd == null)
         throw new WebServiceException("Cannot find web meta data");

      try
      {
         RewriteResults results = modifyMetaData(dep, jbwmd);
         return results;
      }
      catch (RuntimeException rte)
      {
         throw rte;
      }
      catch (Exception e)
      {
         throw new WebServiceException(e);
      }
   }

   private RewriteResults modifyMetaData(Deployment dep, JBossWebMetaData jbwmd) throws ClassNotFoundException
   {
      RewriteResults results = new RewriteResults();

      String servletClass = (String)dep.getProperty(PROPERTY_WEBAPP_SERVLET_CLASS);
      if (servletClass == null)
         throw new IllegalStateException("Cannot obtain context property: " + PROPERTY_WEBAPP_SERVLET_CLASS);

      String contextRoot = dep.getService().getContextRoot();
      jbwmd.setContextRoot(contextRoot);

      Map<String, String> depCtxParams = (Map<String, String>)dep.getProperty(PROPERTY_WEBAPP_CONTEXT_PARAMETERS);
      if (depCtxParams != null)
      {
         List<ParamValueMetaData> contextParams = jbwmd.getContextParams();
         if (contextParams == null)
         {
            contextParams = new ArrayList<ParamValueMetaData>();
            jbwmd.setContextParams(contextParams);
         }

         for (Map.Entry<String, String> entry : depCtxParams.entrySet())
         {
            ParamValueMetaData param = new ParamValueMetaData();
            param.setParamName(entry.getKey());
            param.setParamValue(entry.getValue());
            contextParams.add(param);
         }
      }

      String listenerClass = (String)dep.getProperty(PROPERTY_WEBAPP_SERVLET_CONTEXT_LISTENER);
      if (listenerClass != null)
      {
         List<ListenerMetaData> listeners = jbwmd.getListeners();
         if (listeners == null)
         {
            listeners = new ArrayList<ListenerMetaData>();
            jbwmd.setListeners(listeners);
         }
         ListenerMetaData listener = new ListenerMetaData();
         listener.setListenerClass(listenerClass);
      }

      for (Iterator it = jbwmd.getServlets().iterator(); it.hasNext();)
      {
         JBossServletMetaData servlet = (JBossServletMetaData)it.next();
         List<ParamValueMetaData> initParams = servlet.getInitParam();
         if (initParams == null)
         {
            initParams = new ArrayList<ParamValueMetaData>();
            servlet.setInitParam(initParams);
         }
         
         String linkName = servlet.getServletName();

         // find the servlet-class
         String orgServletClassName = servlet.getServletClass();

         // JSP
         if (orgServletClassName == null)
            continue;

         // Get the servlet class
         Class orgServletClass = null;
         try
         {
            ClassLoader loader = dep.getInitialClassLoader();
            orgServletClass = loader.loadClass(orgServletClassName);
         }
         catch (ClassNotFoundException ex)
         {
            log.warn("Cannot load servlet class: " + orgServletClassName);
         }

         String targetBeanName = null;

         // Nothing to do if we have an <init-param>
         if (isAlreadyModified(servlet))
         {
            for (ParamValueMetaData initParam : initParams)
            {
               String paramName = initParam.getParamName();
               String paramValue = initParam.getParamValue();
               if (Endpoint.SEPID_DOMAIN_ENDPOINT.equals(paramName))
               {
                  targetBeanName = paramValue;
               }
            }
         }
         else
         {
            // Check if it is a real servlet that we can ignore
            if (orgServletClass != null && javax.servlet.Servlet.class.isAssignableFrom(orgServletClass))
            {
               log.info("Ignore servlet: " + orgServletClassName);
               continue;
            }
            /*
            Legacy code: This shouldn't be used in recent AS 5 versions
            
            else if (orgServletClassName.endsWith("Servlet"))
            {
               log.info("Ignore <servlet-class> that ends with 'Servlet': " + orgServletClassName);
               continue;
            }*/

            servlet.setServletClass(servletClass);

            // add additional init params
            if (orgServletClassName.equals(servletClass) == false)
            {
               targetBeanName = orgServletClassName;
               ParamValueMetaData initParam = new ParamValueMetaData();
               initParam.setParamName(Endpoint.SEPID_DOMAIN_ENDPOINT);
               initParam.setParamValue(targetBeanName);
               initParams.add(initParam);
            }
         }

         if (targetBeanName == null)
            throw new IllegalStateException("Cannot obtain service endpoint bean for: " + linkName);

         // remember the target bean name
         results.sepTargetMap.put(linkName, targetBeanName);
      }

      return results;
   }

   // Return true if the web.xml is already modified
   private boolean isAlreadyModified(JBossServletMetaData servlet)
   {
      for (ParamValueMetaData initParam : servlet.getInitParam())
      {
         String paramName = initParam.getParamName();
         if (Endpoint.SEPID_DOMAIN_ENDPOINT.equals(paramName))
            return true;
      }
      return false;
   }
}
