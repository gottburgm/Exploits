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
package org.jboss.console.plugins;

import gnu.trove.TLongObjectHashMap;
import org.jboss.aop.Advisor;
import org.jboss.aop.AspectManager;
import org.jboss.aop.CallerConstructorInfo;
import org.jboss.aop.CallerMethodInfo;
import org.jboss.aop.ConstructorInfo;
import org.jboss.aop.ClassAdvisor;
import org.jboss.aop.FieldInfo;
import org.jboss.aop.MethodInfo;
import org.jboss.aop.advice.AdviceBinding;
import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.advice.AbstractAdvice;
import org.jboss.aop.advice.CFlowInterceptor;
import org.jboss.aop.introduction.InterfaceIntroduction;
import org.jboss.aop.standalone.Package;
import org.jboss.console.manager.interfaces.ManageableResource;
import org.jboss.console.manager.interfaces.TreeNode;
import org.jboss.console.plugins.helpers.AbstractPluginWrapper;

import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletConfig;

/**
 * As the number of MBeans is very big, we use a real Java class which is far
 * faster than beanshell
 *
 * @author  <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>.
 * @version $Revision: 84116 $
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>2 janv. 2003 Sacha Labourey:</b>
 * <ul>
 * <li> First implementation </li>
 * </ul>
 */
public class AOPLister
        extends AbstractPluginWrapper
{
   Thread refreshPoller;

   
   public AOPLister()
   {
      super();
   }

   TreeNode[] createMetaDataTree(org.jboss.aop.metadata.SimpleMetaData metaData, String description, String baseUrl) throws Exception
   {
      HashSet groups = metaData.tags();
      if (groups.size() == 0)
      {
         return null;
      }

      TreeNode[] nodes = new TreeNode[groups.size()];
      Iterator it = groups.iterator();
      for (int i = 0; it.hasNext(); i++)
      {
         String group = (String) it.next();
         nodes[i] = createTreeNode(
                 group, // name
                 description,
                 "images/database.gif", // Icon URL
                 baseUrl + "&group=" + group,
                 null, // menu
                 null, // sub nodes
                 null   // Sub-Resources
         );
      }
      return nodes;
   }

   TreeNode[] loadDefaultMetaData(Advisor advisor, String classname) throws Exception
   {
      org.jboss.aop.metadata.SimpleMetaData metaData = advisor.getDefaultMetaData();
      return createMetaDataTree(metaData,
              "Default metadata for " + classname,
              "AOPDefaultMetaData.jsp?classname=" + classname);
   }

   TreeNode[] loadClassMetaData(Advisor advisor, String classname) throws Exception
   {
      org.jboss.aop.metadata.SimpleMetaData metaData = advisor.getClassMetaData();
      return createMetaDataTree(metaData,
              "Class metadata for " + classname,
              "AOPClassMetaData.jsp?classname=" + classname);
   }

   TreeNode[] loadMethodMetaData(Advisor advisor, String classname) throws Exception
   {
      org.jboss.aop.metadata.MethodMetaData metaData = advisor.getMethodMetaData();

      Iterator it = metaData.getMethods();
      if (!it.hasNext()) return null;
      ArrayList methods = new ArrayList();
      while (it.hasNext())
      {
         String method = (String) it.next();
         org.jboss.aop.metadata.SimpleMetaData methodData = metaData.getMethodMetaData(method);
         TreeNode[] methodNodes = createMetaDataTree(methodData,
                 "Metadata for method " + method,
                 "AOPMethodMetaData.jsp?classname=" + classname + "&method=" + java.net.URLEncoder.encode(method));
         methods.add(createTreeNode(
                 method, // name
                 "Metadata for method " + method,
                 "images/starfolder.gif", // Icon URL
                 null,
                 null, // menu
                 methodNodes, // sub nodes
                 null   // Sub-Resources
         ));

      }
      return (TreeNode[]) methods.toArray(new TreeNode[methods.size()]);
   }

   TreeNode[] loadFieldMetaData(Advisor advisor, String classname) throws Exception
   {
      org.jboss.aop.metadata.FieldMetaData metaData = advisor.getFieldMetaData();

      Iterator it = metaData.getFields();
      if (!it.hasNext()) return null;
      ArrayList fields = new ArrayList();
      while (it.hasNext())
      {
         String field = (String) it.next();
         org.jboss.aop.metadata.SimpleMetaData fieldData = metaData.getFieldMetaData(field);
         TreeNode[] fieldNodes = createMetaDataTree(fieldData,
                 "Metadata for field " + field,
                 "AOPFieldMetaData.jsp?classname=" + classname + "&field=" + field);
         fields.add(createTreeNode(
                 field, // name
                 "Metadata for field " + field,
                 "images/starfolder.gif", // Icon URL
                 null,
                 null, // menu
                 fieldNodes, // sub nodes
                 null   // Sub-Resources
         ));

      }
      return (TreeNode[]) fields.toArray(new TreeNode[fields.size()]);
   }

   TreeNode[] loadConstructorMetaData(Advisor advisor, String classname) throws Exception
   {
      org.jboss.aop.metadata.ConstructorMetaData metaData = advisor.getConstructorMetaData();

      Iterator it = metaData.getConstructors();
      if (!it.hasNext()) return null;
      ArrayList constructors = new ArrayList();
      while (it.hasNext())
      {
         String signature = (String)it.next();
         org.jboss.aop.metadata.SimpleMetaData constructorData = metaData.getConstructorMetaData(signature);
         TreeNode[] constructorNodes = createMetaDataTree(constructorData,
                 "Metadata for constructor",
                 "AOPConstructorMetaData.jsp?classname=" + classname + "&constructor=" + java.net.URLEncoder.encode(signature));
         constructors.add(createTreeNode(
                 signature, // name
                 "Metaata for constructor " + signature,
                 "images/starfolder.gif", // Icon URL
                 null,
                 null, // menu
                 constructorNodes, // sub nodes
                 null   // Sub-Resources
         ));

      }
      return (TreeNode[]) constructors.toArray(new TreeNode[constructors.size()]);
   }

   TreeNode getMetaData(Advisor advisor) throws Exception
   {
      ArrayList nodes = new ArrayList();

      String classname = advisor.getClazz().getName();
      
      TreeNode[] defaultMetaData = loadDefaultMetaData(advisor, classname);
      if (defaultMetaData != null)
      {
         nodes.add(createTreeNode(
                 "Default",
                 "Default metadata for for " + classname, // description
                 "images/starfolder.gif", // Icon URL
                 null,
                 null, // menu
                 defaultMetaData, // sub nodes
                 null   // Sub-Resources
         ));
      }

      TreeNode[] classMetaData = loadClassMetaData(advisor, classname);
      if (classMetaData != null)
      {
         nodes.add(createTreeNode(
               "Class",
               "Class metadata for for " + classname, // description
               "images/starfolder.gif", // Icon URL
               null,
               null, // menu
               classMetaData, // sub nodes
               null   // Sub-Resources
       ));
      }

      TreeNode[] methodMetaData = loadMethodMetaData(advisor, classname);
      if (methodMetaData != null)
      {
         nodes.add(createTreeNode(
                 "Methods",
                 "Method metadata for for " + classname, // description
                 "images/starfolder.gif", // Icon URL
                 null,
                 null, // menu
                 methodMetaData, // sub nodes
                 null   // Sub-Resources
         ));
      }

      TreeNode[] fieldMetaData = loadFieldMetaData(advisor, classname);
      if (fieldMetaData != null)
      {
         nodes.add(createTreeNode(
                 "Fields",
                 "Field metadata for for " + classname, // description
                 "images/starfolder.gif", // Icon URL
                 null,
                 null, // menu
                 fieldMetaData, // sub nodes
                 null   // Sub-Resources
         ));
      }

      TreeNode[] constructorMetaData = loadConstructorMetaData(advisor, classname);
      if (constructorMetaData != null)
      {
         nodes.add(createTreeNode(
                 "Constructors",
                 "Constructor metadata for for " + classname, // description
                 "images/starfolder.gif", // Icon URL
                 null,
                 null, // menu
                 constructorMetaData, // sub nodes
                 null   // Sub-Resources
         ));
      }


      if (nodes.size() == 0) return null;
      TreeNode[] subnodes = (TreeNode[]) nodes.toArray(new TreeNode[nodes.size()]);

      return createTreeNode(
              "Metadata", // name
              "Metadata for " + classname, // description
              "images/starfolder.gif", // Icon URL
              null,
              null, // menu
              subnodes, // sub nodes
              null   // Sub-Resources
      );
   }

   TreeNode[] getIntroductions(Advisor advisor) throws Exception
   {
      ArrayList introductions = advisor.getInterfaceIntroductions();
      if (introductions == null || introductions.size() == 0) return null;

      TreeNode[] nodes = new TreeNode[introductions.size()];
      for (int i = 0; i < introductions.size(); i++)
      {
         InterfaceIntroduction introduction = (InterfaceIntroduction) introductions.get(i);
         nodes[i] = createTreeNode(
                 "Introduction " + i, // name
                 "Introduction for " + advisor.getName(), // description
                 "images/service.gif", // Icon URL
                 "AOPIntroductionPointcut.jsp?pointcut=" + java.net.URLEncoder.encode(introduction.getName()), // Default URL
                 null, // menu
                 null, // sub nodes
                 null   // Sub-Resources
         );
      }

      return nodes;
   }

   public static String shortenMethod(String classname, Method method)
   {
      return method.toString().replaceAll(classname + "." + method.getName(), method.getName());
   }

   public static String shortenConstructor(String classname, Constructor constructor)
   {
      String base = classname.substring(classname.lastIndexOf('.') + 1);
      return constructor.toString().replaceAll(classname, base);
   }

   public static String shortenField(String classname, Field field)
   {
      return field.toString().replaceAll(classname + "." + field.getName(), field.getName());
   }

   public TreeNode[] createAdvisorNodes(Advisor advisor) throws Exception
   {
      ArrayList nodes = new ArrayList();
      if(advisor != null)
      {
        populateIntroductions(advisor, nodes);

        if(advisor instanceof ClassAdvisor)
        {
          populateConstructors((ClassAdvisor) advisor, nodes);
          populateMethods((ClassAdvisor) advisor, nodes);
        }

        if(advisor instanceof ClassAdvisor)
          populateFields((ClassAdvisor) advisor, nodes);
        TreeNode metadata = getMetaData(advisor);
        if (metadata != null) nodes.add(metadata);
      }

      return (TreeNode[]) nodes.toArray(new TreeNode[nodes.size()]);
   }

   private void populateFields(ClassAdvisor advisor, ArrayList nodes) throws Exception
   {
      if (advisor.getAdvisedFields() == null) return;
      ArrayList fieldWriteNodes = new ArrayList();
      ArrayList fieldReadNodes = new ArrayList();
      for (int i = 0; i < advisor.getAdvisedFields().length; i++)
      {
         Field f = advisor.getAdvisedFields()[i];
         FieldInfo[] chain = advisor.getFieldWriteInfos();
         if (chain != null && chain.length > 0)
         {
            fieldWriteNodes.add(createTreeNode(
                    shortenField(advisor.getName(), f),
                    "Field write interceptor chain",
                    "images/service.gif", // Icon URL
                    "AOPFieldChain.jsp?classname=" + java.net.URLEncoder.encode(advisor.getName())
                    + "&field=" + i 
                    + "&mode=write",
                    null, // menu
                    null, // sub nodes
                    null   // Sub-Resources
            ));
         }
         chain = advisor.getFieldReadInfos();
         if (chain != null && chain.length > 0)
         {
            fieldReadNodes.add(createTreeNode(
                    shortenField(advisor.getName(), f),
                    "Field read interceptor chain",
                    "images/service.gif", // Icon URL
                    "AOPFieldChain.jsp?classname=" + java.net.URLEncoder.encode(advisor.getName())
                    + "&field=" + i
                    + "&mode=read",
                    null, // menu
                    null, // sub nodes
                    null   // Sub-Resources
            ));
         }
      }
      
      if (fieldWriteNodes.size() > 0 && fieldWriteNodes.size() > 0)
      {
         ArrayList fieldReadWriteNodes = new ArrayList();
	      if (fieldWriteNodes.size() > 0)
	      {
	         TreeNode[] cnodes = (TreeNode[]) fieldWriteNodes.toArray(new TreeNode[fieldWriteNodes.size()]);
	         fieldReadWriteNodes.add(createTreeNode(
	                 "write interceptors", // name
	                 "field write info", // description
	                 "images/starfolder.gif", // Icon URL
	                 null,
	                 null, // menu
	                 cnodes, // sub nodes
	                 null   // Sub-Resources
	         ));
	      }
	
	      if (fieldReadNodes.size() > 0)
	      {
	         TreeNode[] cnodes = (TreeNode[]) fieldReadNodes.toArray(new TreeNode[fieldReadNodes.size()]);
	         fieldReadWriteNodes.add(createTreeNode(
	                 "read interceptors", // name
	                 "field read info", // description
	                 "images/starfolder.gif", // Icon URL
	                 null,
	                 null, // menu
	                 cnodes, // sub nodes
	                 null   // Sub-Resources
	         ));
	      }
	      
	      TreeNode[] fieldRwNodes = (TreeNode[]) fieldReadWriteNodes.toArray(new TreeNode[fieldReadWriteNodes.size()]);
	      nodes.add(createTreeNode(
               "Fields", //name
               "field info", //description
               "images/starfolder.gif", // Icon URL
               null,
               null, // menu
               fieldRwNodes, // sub nodes
               null   // Sub-Resources
         ));

      }
}

   private void populateConstructors(ClassAdvisor advisor, ArrayList nodes) throws Exception
   {
      if (advisor.getConstructors() == null) return;
      if (advisor.getConstructorInterceptors() == null) return;
      if (advisor.getMethodCalledByConInterceptors() == null) return;
      ArrayList constructorNodes = new ArrayList();
      for (int i = 0; i < advisor.getConstructors().length; i++)
      {
         Constructor con = advisor.getConstructors()[i];
         ConstructorInfo[] chain = advisor.getConstructorInfos();
         HashMap methodCallers = advisor.getMethodCalledByConInterceptors()[i];
         HashMap conCallers = advisor.getConCalledByConInterceptors()[i];
         if ((chain != null && chain.length > 0) || methodCallers != null || conCallers != null)
         {
            ArrayList conNodes = new ArrayList();
            if (chain != null && chain.length > 0)
            {
               conNodes.add(createTreeNode(
                       "Interceptors",
                       "Execution Interceptors",
                       "images/service.gif", // Icon URL
                       "AOPConstructorChain.jsp?classname=" + java.net.URLEncoder.encode(con.getDeclaringClass().getName())
                       + "&constructor=" + i,
                       null, // menu
                       null, // sub nodes
                       null   // Sub-Resources
               ));
            }
            if (conCallers != null)
            {
               conNodes.add(createTreeNode(
                       "constructor callers",
                       "constructor caller interceptions",
                       "images/starfolder.gif", // Icon URL
                       null,
                       null, // menu
                       createConstructorConstructorCallers(i, advisor, conCallers), // sub nodes
                       null   // Sub-Resources
               ));
            }
            if (methodCallers != null)
            {
               conNodes.add(createTreeNode(
                       "method callers",
                       "method caller interceptions",
                       "images/starfolder.gif", // Icon URL
                       null,
                       null, // menu
                       createConstructorMethodCallers(i, advisor, methodCallers), // sub nodes
                       null   // Sub-Resources
               ));
            }
            TreeNode[] cnodes = (TreeNode[]) conNodes.toArray(new TreeNode[conNodes.size()]);
            constructorNodes.add(createTreeNode(
                    shortenConstructor(advisor.getName(), con), // name
                    "constructor info", // description
                    "images/starfolder.gif", // Icon URL
                    null,
                    null, // menu
                    cnodes, // sub nodes
                    null   // Sub-Resources
            ));
         }
      }
      if (constructorNodes.size() > 0)
      {
         TreeNode[] cnodes = (TreeNode[]) constructorNodes.toArray(new TreeNode[constructorNodes.size()]);
         nodes.add(createTreeNode(
                 "Constructors", // name
                 "constructor info", // description
                 "images/starfolder.gif", // Icon URL
                 null,
                 null, // menu
                 cnodes, // sub nodes
                 null   // Sub-Resources
         ));
      }
   }

   private void populateMethods(ClassAdvisor advisor, ArrayList nodes) throws Exception
   {
      if (advisor.getMethodInterceptors() == null) return;
      ArrayList methodNodes = new ArrayList();
      long[] keys = advisor.getMethodInterceptors().keys();
      for (int i = 0; i < keys.length; i++)
      {
         long key = keys[i];
         MethodInfo method = (MethodInfo) advisor.getMethodInfo(key);
         HashMap methodCallers = (HashMap) advisor.getMethodCalledByMethodInterceptors().get(key);
         HashMap conCallers = (HashMap) advisor.getConCalledByMethodInterceptors().get(key);
         if (method == null && methodCallers == null) continue;
         if (method != null && methodCallers == null && (method.getInterceptors() == null || method.getInterceptors().length < 1)) continue;
         ArrayList mNodes = new ArrayList();
         if (method.getInterceptors() != null && method.getInterceptors().length > 0  || methodCallers != null || conCallers != null)
         {
            mNodes.add(createTreeNode(
                    "Interceptors",
                    "Execution Interceptors",
                    "images/service.gif", // Icon URL
                    "AOPMethodChain.jsp?classname=" + java.net.URLEncoder.encode(advisor.getName())
                    + "&method=" + keys[i],
                    null, // menu
                    null, // sub nodes
                    null   // Sub-Resources
            ));
         }
         if (conCallers != null)
         {
            mNodes.add(createTreeNode(
                    "constructor callers",
                    "constructor caller interceptions",
                    "images/starfolder.gif", // Icon URL
                    null,
                    null, // menu
                    createMethodConstructorCallers(key, advisor, conCallers), // sub nodes
                    null   // Sub-Resources
            ));
         }
         if (methodCallers != null)
         {
            mNodes.add(createTreeNode(
                    "method callers",
                    "method caller interceptions",
                    "images/starfolder.gif", // Icon URL
                    null,
                    null, // menu
                    createMethodMethodCallers(key, advisor, methodCallers), // sub nodes
                    null   // Sub-Resources
            ));
         }
         TreeNode[] mnodes = (TreeNode[]) mNodes.toArray(new TreeNode[mNodes.size()]);
         methodNodes.add(createTreeNode(
                 shortenMethod(advisor.getName(), method.getAdvisedMethod()), // name
                 "method info", // description
                 "images/starfolder.gif", // Icon URL
                 null,
                 null, // menu
                 mnodes, // sub nodes
                 null   // Sub-Resources
         ));
      }
      if (methodNodes.size() > 0)
      {
         TreeNode[] cnodes = (TreeNode[]) methodNodes.toArray(new TreeNode[methodNodes.size()]);
         nodes.add(createTreeNode(
                 "Methods", // name
                 "method info", // description
                 "images/starfolder.gif", // Icon URL
                 null,
                 null, // menu
                 cnodes, // sub nodes
                 null   // Sub-Resources
         ));
      }
   }

   private void populateIntroductions(Advisor advisor, ArrayList nodes) throws Exception
   {
      ArrayList introductions = advisor.getInterfaceIntroductions();
      if (introductions != null && introductions.size() > 0)
      {
         TreeNode[] introductionNodes = getIntroductions(advisor);
         TreeNode introductionsNode = createTreeNode(
                 "Introductions", // name
                 "Introductions for " + advisor.getName(), // description
                 "images/starfolder.gif", // Icon URL
                 null,
                 null, // menu
                 introductionNodes, // sub nodes
                 null   // Sub-Resources
         );
         nodes.add(introductionsNode);
      }
   }

   public TreeNode[] createConstructorMethodCallers(int index, ClassAdvisor advisor, HashMap called) throws Exception
   {
      ArrayList nodes = new ArrayList();
      Iterator it = called.keySet().iterator();
      while (it.hasNext())
      {
         String calledClass = (String) it.next();
         TLongObjectHashMap map = (TLongObjectHashMap) called.get(calledClass);
         Object[] values = map.getValues();
         long[] keys = map.keys();
         for (int i = 0; i < values.length; i++)
         {
            CallerMethodInfo caller = (CallerMethodInfo) values[i];
            nodes.add(createTreeNode(
                    caller.getMethod().toString(),
                    "caller interceptions",
                    "images/service.gif", // Icon URL
                    "AOPConstructorMethodCallerChain.jsp?index=" + index + "&hash=" + java.net.URLEncoder.encode(Long.toString(keys[i])) + "&classname=" + java.net.URLEncoder.encode(advisor.getName()) + "&calledclassname=" + java.net.URLEncoder.encode(calledClass),
                    null, // menu
                    null, // sub nodes
                    null   // Sub-Resources
            ));
         }
      }
      return (TreeNode[]) nodes.toArray(new TreeNode[nodes.size()]);
   }

   public TreeNode[] createConstructorConstructorCallers(int index, ClassAdvisor advisor, HashMap called) throws Exception
   {
      ArrayList nodes = new ArrayList();
      Iterator it = called.keySet().iterator();
      while (it.hasNext())
      {
         String calledClass = (String) it.next();
         TLongObjectHashMap map = (TLongObjectHashMap) called.get(calledClass);
         Object[] values = map.getValues();
         long[] keys = map.keys();
         for (int i = 0; i < values.length; i++)
         {
            CallerConstructorInfo caller = (CallerConstructorInfo) values[i];
            nodes.add(createTreeNode(
                    caller.getConstructor().toString(),
                    "caller interceptions",
                    "images/service.gif", // Icon URL
                    "AOPConstructorConstructorCallerChain.jsp?index=" + index + "&hash=" + java.net.URLEncoder.encode(Long.toString(keys[i])) + "&classname=" + java.net.URLEncoder.encode(advisor.getName()) + "&calledclassname=" + java.net.URLEncoder.encode(calledClass),
                    null, // menu
                    null, // sub nodes
                    null   // Sub-Resources
            ));
         }
      }
      return (TreeNode[]) nodes.toArray(new TreeNode[nodes.size()]);
   }

   public TreeNode[] createMethodMethodCallers(long callingHash, ClassAdvisor advisor, HashMap called) throws Exception
   {
      ArrayList nodes = new ArrayList();
      Iterator it = called.keySet().iterator();
      while (it.hasNext())
      {
         String calledClass = (String) it.next();
         TLongObjectHashMap map = (TLongObjectHashMap) called.get(calledClass);
         Object[] values = map.getValues();
         long[] keys = map.keys();
         for (int i = 0; i < values.length; i++)
         {
            CallerMethodInfo caller = (CallerMethodInfo) values[i];
            nodes.add(createTreeNode(
                    caller.getMethod().toString(),
                    "caller interceptions",
                    "images/service.gif", // Icon URL
                    "AOPMethodMethodCallerChain.jsp?callinghash=" + callingHash + "&hash=" + java.net.URLEncoder.encode(Long.toString(keys[i])) + "&classname=" + java.net.URLEncoder.encode(advisor.getName()) + "&calledclassname=" + java.net.URLEncoder.encode(calledClass),
                    null, // menu
                    null, // sub nodes
                    null   // Sub-Resources
            ));
         }
      }
      return (TreeNode[]) nodes.toArray(new TreeNode[nodes.size()]);
   }

   public TreeNode[] createMethodConstructorCallers(long callingHash, ClassAdvisor advisor, HashMap called) throws Exception
   {
      ArrayList nodes = new ArrayList();
      Iterator it = called.keySet().iterator();
      while (it.hasNext())
      {
         String calledClass = (String) it.next();
         TLongObjectHashMap map = (TLongObjectHashMap) called.get(calledClass);
         Object[] values = map.getValues();
         long[] keys = map.keys();
         for (int i = 0; i < values.length; i++)
         {
            CallerConstructorInfo caller = (CallerConstructorInfo) values[i];
            nodes.add(createTreeNode(
                    caller.getConstructor().toString(),
                    "caller interceptions",
                    "images/service.gif", // Icon URL
                    "AOPMethodConstructorCallerChain.jsp?callinghash=" + callingHash + "&hash=" + java.net.URLEncoder.encode(Long.toString(keys[i])) + "&classname=" + java.net.URLEncoder.encode(advisor.getName()) + "&calledclassname=" + java.net.URLEncoder.encode(calledClass),
                    null, // menu
                    null, // sub nodes
                    null   // Sub-Resources
            ));
         }
      }
      return (TreeNode[]) nodes.toArray(new TreeNode[nodes.size()]);
   }

   public TreeNode[] getUnboundBindings() throws Exception
   {
      ArrayList unbounded = new ArrayList();
      Iterator it = AspectManager.instance().getBindings().values().iterator();
      while (it.hasNext())
      {
         AdviceBinding binding = (AdviceBinding) it.next();
         if (!binding.hasAdvisors())
         {
            unbounded.add(createTreeNode(
                    binding.getName(),
                    "Unbounded Binding",
                    "images/service.gif", // Icon URL
                    "AOPBinding.jsp?binding=" + java.net.URLEncoder.encode(binding.getName()),
                    null, // menu
                    null, // sub nodes
                    null   // Sub-Resources
            ));
         }
      }
      if (unbounded.size() == 0) return null;
      return (TreeNode[])unbounded.toArray(new TreeNode[unbounded.size()]);
   }


   TreeNode[] createAOPNodes(Package root) throws Exception
   {
      ArrayList nodes = new ArrayList();
      Iterator it = root.packages.entrySet().iterator();
      while (it.hasNext())
      {
         Map.Entry entry = (Map.Entry) it.next();
         String pkgName = (String) entry.getKey();
         Package p = (Package) entry.getValue();
         nodes.add(createTreeNode(
                 pkgName, // name
                 "Package " + pkgName, // description
                 "images/starfolder.gif", // Icon URL
                 null, // Default URL
                 null, // menu
                 createAOPNodes(p), // sub nodes
                 null   // Sub-Resources
         ));
      }
      it = root.advisors.entrySet().iterator();
      while (it.hasNext())
      {
         Map.Entry entry = (Map.Entry) it.next();
         String classname = (String) entry.getKey();
         Advisor advisor = (Advisor) entry.getValue();
         nodes.add(createTreeNode(
                 classname, // name
                 "Class " + classname, // description
                 "images/serviceset.gif", // Icon URL
                 null,
                 null, // menu
                 createAdvisorNodes(advisor), // sub nodes
                 null   // Sub-Resources
         )
         );
      }
      TreeNode[] result;
      if (nodes.size() == 0)
      {
         result = null;
      }
      else
      {
         result = (TreeNode[]) nodes.toArray(new TreeNode[nodes.size()]);
      }

      return result;
   }

   protected TreeNode getTreeForResource(String profile, ManageableResource resource)
   {
      try
      {
         TreeNode[] unbounded = getUnboundBindings();
         TreeNode[] children = new TreeNode[2];
         children[0] = createTreeNode(
                 "Classes", // name
                 "Display all Classes", // description
                 "images/serviceset.gif", // Icon URL
                 null, // Default URL
                 null,
                 createAOPNodes(Package.aopClassMap()), // sub nodes
                 null   // Sub-Resources
         );
         children[1] = createTreeNode(
                 "Unbound Bindings", // name
                 "Unbound Bindings", // description
                 "images/serviceset.gif", // Icon URL
                 null, // Default URL
                 null,
                 unbounded, // sub nodes
                 null   // Sub-Resources
         );
         return createTreeNode (
               "AOP", // name
               "AOP Management", // description
               "images/spirale32.gif", // Icon URL
               null, // Default URL
               null,
               children,
               null);
      }
      catch (Exception e)
      {
         e.printStackTrace();
         return null;
      }
   }
   
   
   public static String outputChain(Interceptor[] chain)
   {
      String output = "";
      for (int i = 0; i < chain.length; i++)
      {
         output += "<tr>";
         if (chain[i] instanceof AbstractAdvice)
         {
            output +="<td><font size=\"1\">advice</font></td><td><font size=\"1\">" + chain[i].getName() + "</font></td>";
         }
         else if (chain[i] instanceof CFlowInterceptor)
         {
            output +="<td><font size=\"1\">cflow</font></td><td><font size=\"1\">" + ((CFlowInterceptor) chain[i]).getCFlowString() + "</font></td>";
         }
         else
         {
            output +="<td><font size=\"1\">interceptor</font></td><td><font size=\"1\">" + chain[i].getClass().getName() + "</font></td>";
         }
         output += "</tr>";
      }
      return output;
   }

   // org.jboss.console.plugins.helpers.PluginWrapper overrides -----------------------
   
   public void init(ServletConfig servletConfig) throws Exception
   {
      super.init(servletConfig);
      refreshPoller = new RefreshPoller();
      refreshPoller.start();
   }

   class RefreshPoller extends Thread
   {

   	  final static int REFRESH_RATE = 20 * 1000;

	  RefreshPoller()
	  {
		 setName("AOPListner");
		 setDaemon(true);
	  }
   	 
	  public void run()
	  {
	     try
	     {
			int advisorCount = 0;
			while (!isInterrupted())
			{
			   int count = AspectManager.instance().getAdvisors().size();
			   if (count != advisorCount)
			   {
				  pm.regenerateAdminTree();
			   }
			   advisorCount = count;
			   
			   Thread.sleep(REFRESH_RATE);
			} 
		 }
	     catch (InterruptedException e)
	     {
			return;
	     }
	  }
   } 
   
   public void destroy()
   {
      super.destroy();
      try
      {
         refreshPoller.interrupt();
         refreshPoller.join();
      } 
      catch (Exception e)
      {
      }
   }

   /**
    * For jsp pages to get hold of the advisor given a classname, since
    * AspectManager.getAdvisor(String classname) has been deprecated.
    * (I attempted to load the class from the class name and then to
    * call AspectManager.findAdvisor(), which worked on first deploy,
    * but returned null on subsequent deploys)
    * @param classname
    * @return
    */
   public static ClassAdvisor findAdvisor(String classname)
   {
      return AdvisorFinder.getAdvisor(classname);
   }
   /**
    * Helper class For jsp pages to get hold of the advisor given a classname, since
    * AspectManager.getAdvisor(String classname) has been deprecated.
    */
   static class AdvisorFinder
   {
      public static ClassAdvisor getAdvisor(String classname)
      {
         String[] name = classname.split("\\.");
         Package root = Package.aopClassMap();
         
         if (!root.name.equals("classes"))throw new RuntimeException("Did not get expected root 'classes'"); 
         
         for (Iterator it = root.packages.entrySet().iterator() ; it.hasNext() ; )
         {
            Map.Entry entry = (Map.Entry) it.next();
            Package pkg = (Package) entry.getValue();
            ClassAdvisor advisor = findAdvisor(pkg, classname, name, 0);
            if (advisor != null)
            {
               return advisor;
            }
         }
         return null;
      }

      private static ClassAdvisor findAdvisor(Package pkg, String classname, String[] name, int depth)
      {
         if (depth >= name.length || !pkg.name.equals(name[depth]))
         {
            return null;
         }
         
         for (Iterator it = pkg.packages.entrySet().iterator() ; it.hasNext() ; )
         {
            Map.Entry entry = (Map.Entry) it.next();
            Package p = (Package) entry.getValue();
            ClassAdvisor advisor = findAdvisor(p, classname, name, depth + 1);
            if (advisor != null)
            {
               return advisor;
            }
         }

         for (Iterator it = pkg.advisors.entrySet().iterator(); it.hasNext() ; )
         {
            Map.Entry entry = (Map.Entry) it.next();
            ClassAdvisor advisor = (ClassAdvisor) entry.getValue();
            if (advisor.getClazz().getName().equals(classname))
            {
               return advisor;
            }
         }
         
         return null;
      }
   }
   

      
}
