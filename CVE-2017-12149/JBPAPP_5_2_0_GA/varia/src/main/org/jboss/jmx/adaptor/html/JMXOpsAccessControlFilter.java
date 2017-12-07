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
package org.jboss.jmx.adaptor.html;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Set;
import java.util.Iterator;
import java.security.Principal;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse; 
import javax.servlet.http.HttpServletResponse;

import org.jboss.logging.Logger;

//$Id: JMXOpsAccessControlFilter.java 110379 2011-01-14 17:02:40Z mmoyses $

/**
 *  JBAS-3311: Access Control on JMX Operations in the JMX Console.
 *  Filter that allows Role Based Authorization of the various 
 *  JMX Operations. The actions that come as part of the request are:
 *  displayMBeans
 *  inspectMBean
 *  updateAttributes - Operations that involve updation of jmx attributes
 *  invokeOp - Operations that involve "invoke"
 *  invokeOpByName
 *  @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 *  @since  Jun 12, 2006
 *  @version $Revision: 110379 $
 */
public class JMXOpsAccessControlFilter implements Filter
{ 
   private static Logger log = Logger.getLogger(JMXOpsAccessControlFilter.class);
   private boolean trace = log.isTraceEnabled();
   private static final String ACTION_PARAM = "action"; 
   private static final String DISPLAY_MBEANS_ACTION = "displayMBeans";
   private static final String INSPECT_MBEAN_ACTION = "inspectMBean";
   private static final String UPDATE_ATTRIBUTES_ACTION = "updateAttributes";
   private static final String INVOKE_OP_ACTION = "invokeOp";
   private static final String INVOKE_OP_BY_NAME_ACTION = "invokeOpByName";
   
   private List updateAttributesRoles = null;
   private List invokeOpRoles = null; 
   //Rare usecase
   private List invokeMBeanRoles = null;   
   
   //An authorization delegate that the user can plug in which can do the
   //authorization decisions - when deeper access control usecases arise
   //The Authorization Delegate should have a method
   //public Boolean authorize(ServletRequest,ServletResponse,List)
   private Object authorizationDelegate = null;
   
   /**
    * @see Filter#init(javax.servlet.FilterConfig)
    */
   public void init(FilterConfig filterConfig) throws ServletException
   { 
      String updateAttributesStr = filterConfig.getInitParameter("updateAttributes");
      if(updateAttributesStr != null && updateAttributesStr.length() > 0)
         updateAttributesRoles = this.getRoles(updateAttributesStr);
      
      String invokeOpStr = filterConfig.getInitParameter("invokeOp");
      if(invokeOpStr != null && invokeOpStr.length() > 0)
         invokeOpRoles = this.getRoles(invokeOpStr); 
      
      String inspectMBeanStr = filterConfig.getInitParameter("inspectMBean");
      if(inspectMBeanStr != null && inspectMBeanStr.length() > 0)
         invokeMBeanRoles = this.getRoles(inspectMBeanStr);
      
      //Optional - Authorization Delegate
      String delegateStr = filterConfig.getInitParameter("authorizationDelegate");
      if(delegateStr != null && delegateStr.length() > 0)
         authorizationDelegate = this.instantiate(delegateStr);
   }

   /**
    * @see Filter#doFilter(javax.servlet.ServletRequest, 
    * javax.servlet.ServletResponse, javax.servlet.FilterChain)
    */
   public void doFilter(ServletRequest request, ServletResponse response, 
                   FilterChain chain) 
   throws IOException, ServletException
   {  
      boolean passThrough = true;      
      
      String action = request.getParameter(ACTION_PARAM);

      if( action == null )
         action = DISPLAY_MBEANS_ACTION;
      
      if( action.equals(UPDATE_ATTRIBUTES_ACTION))
         passThrough = authorize(request, response, updateAttributesRoles);
      else if( action.equals(INVOKE_OP_ACTION) || action.equals(INVOKE_OP_BY_NAME_ACTION))
         passThrough = authorize(request, response,invokeOpRoles);
      else if( action.equals(INSPECT_MBEAN_ACTION))
         passThrough = authorize(request, response,invokeMBeanRoles);
      
      if(!passThrough)
        ((HttpServletResponse)response).setStatus(HttpServletResponse.SC_FORBIDDEN); 
      else 
         chain.doFilter(request, response);
   }

   /**
    * @see Filter#destroy()
    */
   public void destroy()
   { 
   }  

   /**
    * Authorize the JMX Operations
    * If there is an Authorization Delegate plugged in, it will
    * be consulted for access control
    * @param request
    * @param response
    * @param listToCheck
    * @return
    */
   private boolean authorize(ServletRequest request, 
         ServletResponse response, List listToCheck)
   {
      //Check if there is an authorization delegate
      if(authorizationDelegate != null)
         return checkWithDelegate(request,response,listToCheck);

      if(listToCheck == null || listToCheck.size() == 0)
         return true;
      
      ArrayList subjectRoles = getSubjectRoles();
      
      boolean result = false;
      int len = subjectRoles.size();
      for(int i = 0; i < len; i++)
      {
         String subjectRole = (String)subjectRoles.get(i);
         result = listToCheck.contains(subjectRole);
         if(result)
            break;
      } 
      return result;  
   } 
   
   private boolean checkWithDelegate(ServletRequest request, 
         ServletResponse response, List listToCheck)
   {
      Boolean result = Boolean.FALSE;
      String name = "authorize";
      Class[] args = new Class[] {ServletRequest.class, ServletResponse.class,
            List.class}; 
      try
      {
         Method meth = authorizationDelegate.getClass().getMethod(name,args);
         result = (Boolean)meth.invoke(authorizationDelegate,
               new Object[]{request,response,listToCheck});
      }
      catch ( Exception e)
      {
         if(trace)
            log.error("Error invoking AuthorizationDelegate:",e);
      } 
      return result.booleanValue();
   }
   
   
   /**
    * Get a list of roles from the string that is comma-delimited
    * @param commaSeperatedRoles
    * @return
    */
   private List getRoles(String commaSeperatedRoles)
   {
      StringTokenizer st = new StringTokenizer(commaSeperatedRoles,",");
      int numTokens = st.countTokens();
      String[] strArr = new String[numTokens]; 
      for(int i=0; i < numTokens; i++)
      {
         strArr[i] = st.nextToken();
      }
      return Arrays.asList(strArr);
   }
   
   /**
    * Get a list of roles from the authenticated subject
    * @return
    */
   private ArrayList getSubjectRoles()
   { 
      ArrayList alist = new ArrayList();
   
      this.getMappedSubjectRoles(alist);
      
      if(trace)
         log.trace("Subject Roles="+alist);
      return alist;
   } 

   private void getMappedSubjectRoles(ArrayList alist)
   { 
     try
     {
        Set role_set = SecurityActions.getSubjectRoles();
        Iterator role_iter = role_set.iterator();
        while(role_iter != null && role_iter.hasNext())
        {
          Principal p = (Principal)role_iter.next();
          alist.add(p.getName());
        }
      }
      catch (Exception e)
      {
        if(trace)
          log.trace("Error obtaining mapped roles:",e); 
      } 
   } 

   /**
    * Instantiate The Authorization Delegate
    * @param delegateStr
    * @return
    */
   public Object instantiate(String delegateStr)
   {
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      Object obj = null;
      try
      {
         Class clazz = cl.loadClass(delegateStr);
         obj = clazz.newInstance();
      }
      catch (Exception e)
      { 
         if(trace)
            log.error("Error instantiating AuthorizationDelegate:",e);
      }
      return obj; 
   }
}
