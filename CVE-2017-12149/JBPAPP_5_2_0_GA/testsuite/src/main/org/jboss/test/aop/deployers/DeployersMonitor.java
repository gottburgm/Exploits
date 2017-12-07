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
package org.jboss.test.aop.deployers;

import java.util.Map;
import java.util.TreeMap;

import org.jboss.aop.AspectManager;
import org.jboss.aop.advice.AdviceBinding;
import org.jboss.aop.advice.AspectDefinition;
import org.jboss.aop.advice.AspectFactory;
import org.jboss.aop.advice.GenericAspectFactory;

/**
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 85945 $
 */
public class DeployersMonitor implements DeployersMonitorMBean
{
   AspectManager manager = AspectManager.instance();

   public Map<String, String> getCurrentBindings()
   {
      Map<String, String> result = new TreeMap<String, String>();
      Map bindings = manager.getBindings();
      for (Object binding : bindings.values())
      {
         String key = ((AdviceBinding)binding).getName();
         String poincut = ((AdviceBinding)binding).getPointcut().getExpr();
         result.put(key, poincut);
      }
      
      return result;
   }

   public Map<String, String> getCurrentAspectDefinitions()
   {
      Map<String, String> result = new TreeMap();
      Map definitions = manager.getAspectDefinitions();
      for (Object def : definitions.values())
      {
         AspectFactory factory = ((AspectDefinition)def).getFactory();
         
         if (factory instanceof GenericAspectFactory)
         {
            String key = ((AspectDefinition)def).getName();
            String clazz = ((GenericAspectFactory)factory).getClassname();
            result.put(key, clazz);
         }
      }
      
      return result;
   }

   public void invokeXmlPOJO() throws Exception
   {
      org.jboss.test.aop.deployers.xml.POJO pojo = new org.jboss.test.aop.deployers.xml.POJO();
      org.jboss.test.aop.deployers.xml.POJO.invoked = false;
      org.jboss.test.aop.deployers.xml.SomeInterceptor.invoked = 0;
      org.jboss.test.aop.deployers.xml.SomeAspect.invoked = 0;
      
      pojo.someMethod();
      
      if (org.jboss.test.aop.deployers.xml.SomeInterceptor.invoked != 2) throw new RuntimeException("SomeInterceptor should have intercepted 2 times not " + org.jboss.test.aop.deployers.xml.SomeInterceptor.invoked);
      if (org.jboss.test.aop.deployers.xml.SomeAspect.invoked != 2) throw new RuntimeException("SomeAspect should have intercepted 2 times not " + org.jboss.test.aop.deployers.xml.SomeAspect.invoked);
      if (!org.jboss.test.aop.deployers.xml.POJO.invoked) throw new RuntimeException("POJO was not called");
   }
   
   public void invokeAnnotationPOJO() throws Exception
   {
      org.jboss.test.aop.deployers.annotations.POJO pojo = new org.jboss.test.aop.deployers.annotations.POJO();
      org.jboss.test.aop.deployers.annotations.POJO.invoked = false;
      org.jboss.test.aop.deployers.annotations.SomeInterceptor.invoked = 0;
      org.jboss.test.aop.deployers.annotations.SomeAspect.invoked = 0;
      
      pojo.someMethod();
      
      if (org.jboss.test.aop.deployers.annotations.SomeInterceptor.invoked != 1) throw new RuntimeException("SomeInterceptor should have intercepted 1 times not " + org.jboss.test.aop.deployers.annotations.SomeInterceptor.invoked);
      if (org.jboss.test.aop.deployers.annotations.SomeAspect.invoked != 1) throw new RuntimeException("SomeAspect should have intercepted 1 times not " + org.jboss.test.aop.deployers.annotations.SomeAspect.invoked);
      if (!org.jboss.test.aop.deployers.annotations.POJO.invoked) throw new RuntimeException("POJO was not called");
   }
}
