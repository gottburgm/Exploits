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

package org.jboss.test.classloader.leak.clstore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A node (i.e. line item) in a {@link LeakAnalyzer} report.
 * 
 * @author <a href="brian.stansberry@jboss.com">Brian Stansberry</a>
 * @version $Revision: 85945 $
 */
public class ReferenceReportNode
{
   private String message;
   private List<ReferenceReportNode> children = new ArrayList<ReferenceReportNode>();; 
   private ReferenceReportNode parent;
   private Boolean critical = Boolean.TRUE;
   private int nonCriticalChildren = 0;
   
   public ReferenceReportNode()
   {      
   }
   
   public ReferenceReportNode(String msg) 
   {
      this.message = msg;
   }
   
   public void addChild(ReferenceReportNode child) 
   {
      children.add(child);
      child.setParent(this);
      critical = null;
   }
   
   public void removeBranch()  
   {
      if (parent != null)
         parent.removeChild(this);
   }
   
   private void removeChild(ReferenceReportNode child) 
   {
       children.remove(child);
       if (children.size() == 0)
          removeBranch();
   }
   
   public boolean isLeaf()
   {
      return children.size() == 0;
   }
   
   public boolean isCritical()
   {
      if (critical != null)
      {
         return critical.booleanValue();
      }
      return (children.size() - nonCriticalChildren > 0); 
   }
   
   public void markNonCritical()
   {
      critical = Boolean.FALSE;
      if (parent != null)
         parent.markChildNonCritical();
   }
   
   private void markChildNonCritical()
   {
      nonCriticalChildren++;
      if (!isCritical() && parent != null)
         parent.markChildNonCritical();
   }
   
   public List<ReferenceReportNode> getChildren()
   {
      return Collections.unmodifiableList(children);
   }
   
   public ReferenceReportNode getParent()
   {
      return parent;
   }
   
   private void setParent(ReferenceReportNode parent) 
   {
      this.parent = parent;
   }
   
   public String getMessage() 
   {         
      return message;
   }
   
   public void setMessage(String msg)
   {
      this.message = msg;
   }
}   