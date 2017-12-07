/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.profileservice.management.client.upload;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;

import org.jboss.deployers.spi.management.deploy.DeploymentStatus;
import org.jboss.deployers.spi.management.deploy.DeploymentTarget;

/**
 * Simple javabean impl of DeploymentStatus
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 91130 $
 */
public class SerializableDeploymentStatus implements DeploymentStatus,
      Serializable
{
   private static final long serialVersionUID = 1;

   private CommandType command;
   private Exception failure;
   private String message;
   private StateType state;
   private DeploymentTarget target;
   private boolean isCompleted;
   private boolean isFailed;
   private boolean isRunning;

   public SerializableDeploymentStatus(CommandType command, StateType state)
   {
      this.command = command;
      this.state = state;
   }

   public CommandType getCommand()
   {
      return command;
   }
   public void setCommand(CommandType command)
   {
      this.command = command;
   }
   
   public DeploymentTarget getTarget()
   {
      return target;
   }
   public void setTarget(DeploymentTarget target)
   {
      this.target = target;
   }

   public Exception getFailure()
   {
      return failure;
   }
   public void setFailure(Exception failure)
   {
      this.failure = failure;
   }
   public String getMessage()
   {
      return message;
   }
   public void setMessage(String message)
   {
      this.message = message;
   }
   public StateType getState()
   {
      return state;
   }
   public void setState(StateType state)
   {
      this.state = state;
   }
   public boolean isCompleted()
   {
      return isCompleted;
   }
   public void setCompleted(boolean isCompleted)
   {
      this.isCompleted = isCompleted;
   }
   public boolean isFailed()
   {
      return isFailed;
   }
   public void setFailed(boolean isFailed)
   {
      this.isFailed = isFailed;
   }
   public boolean isRunning()
   {
      return isRunning;
   }
   public void setRunning(boolean isRunning)
   {
      this.isRunning = isRunning;
   }

   @Override
   public String toString()
   {
      StringBuffer tmp = new StringBuffer("DeploymentStatus(");
      tmp.append("command=");
      tmp.append(command);
      tmp.append(",state=");
      tmp.append(state);
      tmp.append(",message=");
      tmp.append(message);
      tmp.append(",isCompleted=");
      tmp.append(isCompleted);
      tmp.append(",isRunning=");
      tmp.append(isRunning);
      tmp.append(",isFailed=");
      tmp.append(isFailed);

      if(failure != null)
      {
         StringWriter sw = new StringWriter();
         PrintWriter pw = new PrintWriter(sw);
         failure.printStackTrace(pw);
         tmp.append(",failure:\n");
         tmp.append(sw.toString());
      }
      tmp.append(")");
      return tmp.toString();
   }

}
