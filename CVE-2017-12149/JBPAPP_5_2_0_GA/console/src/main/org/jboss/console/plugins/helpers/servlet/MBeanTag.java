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
package org.jboss.console.plugins.helpers.servlet;

import org.jboss.console.plugins.helpers.jmx.Server;
import org.jboss.mx.util.MBeanProxy;

import javax.management.ObjectName;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * <description>
 *
 * @see <related>
 *
 * @author  <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>.
 * @version $Revision: 81010 $
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>4 janv. 2003 Sacha Labourey:</b>
 * <ul>
 * <li> First implementation </li>
 * </ul>
 */
public class MBeanTag
   extends TagSupport
{
   protected String interfaceName = null;
   protected String variableName = null;
   protected String mbeanName = null;
   
   public String getIntf () { return this.interfaceName; }
   public void setIntf (String intf) { this.interfaceName = intf; }
   
   public String getId () { return this.variableName; }
   public void setId (String var) { this.variableName = var; }
   
   public String getMbean () { return this.mbeanName; }
   public void setMbean (String mbean) { this.mbeanName = mbean; }
   

   public int doStartTag () throws JspTagException
   {
      try
      {
         // Who do we proxy?
         //
         ObjectName objName = null;
         if (mbeanName == null)
         {
            objName = new ObjectName (pageContext.getRequest().getParameter("ObjectName"));
         }
         else
         {
            objName = new ObjectName (mbeanName);
         }
         
         // Which type do we proxy?
         //
         Class type = Thread.currentThread().getContextClassLoader().loadClass(this.interfaceName);
         
         // we build the proxy
         //
         Object result = MBeanProxy.get(type, objName, Server.getMBeanServer());
         
         // we assign the proxy to the variable
         //
         pageContext.setAttribute(variableName, result);
         
         return EVAL_BODY_INCLUDE;
      }
      catch (Exception e)
      {
         throw new JspTagException (e.toString());
      }
   }

   public int doEndTag () throws JspTagException
   {
      return EVAL_PAGE;
   }
}
