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
package org.jboss.ejb;

import java.util.Iterator;
import java.util.Set;

import javax.security.jacc.EJBMethodPermission;
import javax.security.jacc.EJBRoleRefPermission;
import javax.security.jacc.PolicyConfiguration;
import javax.security.jacc.PolicyContextException;

import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;
import org.jboss.metadata.ejb.spec.ExcludeListMetaData;
import org.jboss.metadata.ejb.spec.MethodInterfaceType;
import org.jboss.metadata.ejb.spec.MethodPermissionMetaData;
import org.jboss.metadata.ejb.spec.MethodPermissionsMetaData;
import org.jboss.metadata.ejb.spec.MethodsMetaData;
import org.jboss.metadata.javaee.spec.SecurityRoleRefsMetaData;

//$Id: EJBPermissionMapping.java 85945 2009-03-16 19:45:12Z dimitris@jboss.org $

/**
 *  Utility class to create the EJB Permissions from the metadata available
 *  @author Scott.Stark@jboss.org
 *  @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 *  @since  Dec 12, 2006 
 *  @version $Revision: 85945 $
 */
public class EJBPermissionMapping
{

   public static void createPermissions(JBossEnterpriseBeanMetaData bean, PolicyConfiguration pc)
         throws PolicyContextException
   {
      // Process the method-permission MethodMetaData
      MethodPermissionsMetaData perms = bean.getMethodPermissions();
      if (perms != null)
         for (MethodPermissionMetaData perm : perms)
         {
            MethodsMetaData methods = perm.getMethods();
            if (methods != null)
               for (org.jboss.metadata.ejb.spec.MethodMetaData mmd : methods)
               {
                  String[] params =
                  {};
                  if (mmd.getMethodParams() != null)
                     params = mmd.getMethodParams().toArray(params);
                  else
                     params = null;
                  String methodName = mmd.getMethodName();
                  if (methodName != null && methodName.equals("*"))
                     methodName = null;
                  MethodInterfaceType miType = mmd.getMethodIntf();
                  String iface = miType != null ? miType.name() : null;
                  EJBMethodPermission p = new EJBMethodPermission(mmd.getEjbName(), methodName, iface, params);
                  if (perm.getUnchecked() != null)
                  {
                     pc.addToUncheckedPolicy(p);
                  }
                  else
                  {
                     Set<String> roles = perm.getRoles();
                     Iterator riter = roles.iterator();
                     while (riter.hasNext())
                     {
                        String role = (String) riter.next();
                        pc.addToRole(role, p);
                     }
                  }
               }
         }

      // Process the exclude-list MethodMetaData
      ExcludeListMetaData excluded = bean.getExcludeList();
      if (excluded != null)
      {
         MethodsMetaData methods = excluded.getMethods();
         if (methods != null)
            for (org.jboss.metadata.ejb.spec.MethodMetaData mmd : methods)
            {
               String[] params =
               {};
               if (mmd.getMethodParams() != null)
                  params = mmd.getMethodParams().toArray(params);
               else
                  params = null;
               String methodName = mmd.getMethodName();
               if (methodName != null && methodName.equals("*"))
                  methodName = null;
               MethodInterfaceType miType = mmd.getMethodIntf();
               String iface = miType != null ? miType.name() : null;
               EJBMethodPermission p = new EJBMethodPermission(mmd.getEjbName(), methodName, iface, params);
               pc.addToExcludedPolicy(p);
            }
      }

      // Process the security-role-ref SecurityRoleRefMetaData
      SecurityRoleRefsMetaData refs = bean.getSecurityRoleRefs();
      if (refs != null)
         for (org.jboss.metadata.javaee.spec.SecurityRoleRefMetaData ref : refs)
         {
            EJBRoleRefPermission p = new EJBRoleRefPermission(bean.getEjbName(), ref.getRoleName());
            pc.addToRole(ref.getRoleLink(), p);
         }

      /* Special handling of stateful session bean getEJBObject due how the
      stateful session handles acquire the proxy by sending an invocation to
      the ejb container.
       */
      if (bean.isSession())
      {
         JBossSessionBeanMetaData smd = (JBossSessionBeanMetaData) bean;
         if (smd.isStateful())
         {
            EJBMethodPermission p = new EJBMethodPermission(bean.getEjbName(), "getEJBObject", "Home", null);
            pc.addToUncheckedPolicy(p);
         }
      }
   }
}
