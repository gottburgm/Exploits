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
package org.jboss.services.deployment.metadata;

import java.io.Serializable;

/**
 * Simple POJO class to model XML data
 * 
 * @author  <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * 
 * @version $Revision: 81038 $
 */
public class TemplateInfo
   implements Serializable
{
   /** @since 4.0.2 */
   private static final long serialVersionUID = 2231674463239010529L;
      
   private String input;
   private String output;
   
   public TemplateInfo()
   {
      // empty
   }
   
   public TemplateInfo(String input, String output)
   {
      this.input  = input;
      this.output = output;
   }
   
   public String getInput()
   {
      return input;
   }
   
   public void setInput(String input)
   {
      this.input = input;
   }
   
   public String getOutput()
   {
      return output;
   }
   
   public void setOutput(String output)
   {
      this.output = output;
   }
   
   public String toString()
   {
      StringBuffer sbuf = new StringBuffer(128);
      
      sbuf.append('[')
      .append("input=").append(input)
      .append(", output=").append(output)
      .append(']');
      
      return sbuf.toString();      
   }   
}
