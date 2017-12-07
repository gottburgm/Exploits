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

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.jboss.deployers.spi.management.deploy.DeploymentID;
import org.jboss.deployers.spi.management.deploy.DeploymentProgress;
import org.jboss.deployers.spi.management.deploy.DeploymentStatus;
import org.jboss.deployers.spi.management.deploy.DeploymentTarget;
import org.jboss.deployers.spi.management.deploy.ProgressEvent;
import org.jboss.deployers.spi.management.deploy.ProgressListener;
import org.jboss.deployers.spi.management.deploy.DeploymentStatus.CommandType;
import org.jboss.deployers.spi.management.deploy.DeploymentStatus.StateType;

/**
 * The deployment progress.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 91130 $
 */
public class DeploymentProgressImpl implements DeploymentProgress, Serializable
{
   private static final long serialVersionUID = 1;

   /** The client side listeners */
   private transient CopyOnWriteArrayList<ProgressListener> listeners = new CopyOnWriteArrayList<ProgressListener>();
   private transient DeploymentStatus currentStatus;
   private transient boolean isCancelled;
   /** The targets to distribute to */
   private List<DeploymentTarget> targets;
   /** The deployment being distributed */
   private DeploymentID deployment;
   private CommandType command;

   public DeploymentProgressImpl(List<DeploymentTarget> targets, DeploymentID deployment, CommandType command)
   {
      this.targets = targets;
      this.deployment = deployment;
      this.command = command;
   }

   public synchronized void addProgressListener(ProgressListener listener)
   {
      if(listeners == null)
         listeners = new CopyOnWriteArrayList<ProgressListener>();
      listeners.add(listener);
   }
   public void removeProgressListener(ProgressListener listener)
   {
      listeners.remove(listener);
   }

   /**
    * Begins the deployment command process
    */
   public void run()
   {
      switch(command)
      {
         case DISTRIBUTE:
            distribute();
            break;
         case PREPARE:
            prepare();
            break;
         case START:
            start();
            break;
         case STOP:
            stop();
            break;
         case REMOVE:
            remove();
            break;
         case REDEPLOY:
            redeploy();
            break;
         default:
            throw new IllegalStateException(command+" is not currently handled");
      }
   }

   public void cancel()
   {
      isCancelled = true;
   }

   public DeploymentStatus getDeploymentStatus()
   {
      return currentStatus;
   }

   public DeploymentID getDeploymentID()
   {
      return deployment;
   }

   public List<DeploymentTarget> getDeploymentTargets()
   {
      return targets;
   }

   /**
    * 
    * @param event
    */
   protected void notify(ProgressEvent event)
   {
      if(listeners == null)
         return;

      for(ProgressListener listener : listeners)
      {
         try
         {
            listener.progressEvent(event);
         }
         catch(Throwable ignore)
         {
         }
      }
   }

   protected void distribute()
   {
      SerializableDeploymentStatus status = new SerializableDeploymentStatus(command, StateType.DEPLOYING);
      status.setMessage("Running distribute to: "+targets);
      status.setRunning(true);
      currentStatus = status;
      ProgressEvent event =  new ProgressEvent(deployment, currentStatus);
      notify(event);
      for(DeploymentTarget target : targets)
      {
         if(isCancelled)
         {
            status = new SerializableDeploymentStatus(command, StateType.CANCELLED);
            status.setMessage("Distribute has been cancelled");
            currentStatus = status;
            event =  new ProgressEvent(deployment, currentStatus);
            notify(event);
            break;
         }

         try
         {
            // TODO, percent complete info in upload and overall distribute
            status = new SerializableDeploymentStatus(command, StateType.UPLOADING);
            status.setTarget(target);
            status.setRunning(true);
            status.setMessage("Begining distribute to target: "+target);
            currentStatus = status;
            event =  new ProgressEvent(deployment, currentStatus);
            notify(event);

            // TODO, cancellation of in progress distribution
            target.distribute(deployment);
            status = new SerializableDeploymentStatus(command, StateType.DEPLOYING);
            status.setTarget(target);
            status.setMessage("Completed distribute to target: "+target);
            currentStatus = status;
            event =  new ProgressEvent(deployment, currentStatus);
            notify(event);
         }
         catch(Exception e)
         {
            status = new SerializableDeploymentStatus(command, StateType.FAILED);
            status.setTarget(target);
            status.setFailure(e);
            status.setFailed(true);
            currentStatus = status;
            ProgressEvent error = new ProgressEvent(deployment, currentStatus);
            notify(error);
            break;
         }
      }

      if(currentStatus.isFailed() == false)
      {
         status = new SerializableDeploymentStatus(command, StateType.COMPLETED);
         status.setMessage("Completed distribute to all targets");
         status.setCompleted(true);
         currentStatus = status;
         event =  new ProgressEvent(deployment, currentStatus);
         notify(event);         
      }
   }

   protected void start()
   {
      SerializableDeploymentStatus status = new SerializableDeploymentStatus(command, StateType.RUNNING);
      status.setMessage("Running start to: "+targets);
      status.setRunning(true);
      currentStatus = status;
      ProgressEvent event =  new ProgressEvent(deployment, currentStatus);
      notify(event);
      for(DeploymentTarget target : targets)
      {
         if(isCancelled)
         {
            status = new SerializableDeploymentStatus(command, StateType.CANCELLED);
            status.setMessage("Start has been cancelled");
            currentStatus = status;
            event =  new ProgressEvent(deployment, currentStatus);
            notify(event);
            break;
         }

         try
         {
            target.start(deployment);
            status = new SerializableDeploymentStatus(command, StateType.COMPLETED);
            status.setTarget(target);
            status.setMessage("Completed start for target: "+target);
            status.setCompleted(true);
            currentStatus = status;
            event =  new ProgressEvent(deployment, currentStatus);
            notify(event);
         }
         catch(Exception e)
         {
            status = new SerializableDeploymentStatus(command, StateType.FAILED);
            status.setTarget(target);
            status.setFailure(e);
            status.setFailed(true);
            currentStatus = status;
            ProgressEvent error = new ProgressEvent(deployment, currentStatus);
            notify(error);
            break;
         }
      }
   }

   protected void stop()
   {
      SerializableDeploymentStatus status = new SerializableDeploymentStatus(command, StateType.RUNNING);
      status.setMessage("Running stop to: "+targets);
      status.setRunning(true);
      currentStatus = status;
      ProgressEvent event =  new ProgressEvent(deployment, currentStatus);
      notify(event);
      for(DeploymentTarget target : targets)
      {
         if(isCancelled)
         {
            status = new SerializableDeploymentStatus(command, StateType.CANCELLED);
            status.setMessage("Stop has been cancelled");
            currentStatus = status;
            event =  new ProgressEvent(deployment, currentStatus);
            notify(event);
            break;
         }

         try
         {
            target.stop(deployment);
            status = new SerializableDeploymentStatus(command, StateType.COMPLETED);
            status.setTarget(target);
            status.setMessage("Completed stop for target: "+target);
            status.setCompleted(true);
            currentStatus = status;
            event =  new ProgressEvent(deployment, currentStatus);
            notify(event);
         }
         catch(Exception e)
         {
            status = new SerializableDeploymentStatus(command, StateType.FAILED);
            status.setTarget(target);
            status.setFailure(e);
            status.setFailed(true);
            currentStatus = status;
            ProgressEvent error = new ProgressEvent(deployment, currentStatus);
            notify(error);
            break;
         }
      }
   }

   protected void remove()
   {
      SerializableDeploymentStatus status = new SerializableDeploymentStatus(command, StateType.RUNNING);
      status.setMessage("Running undeploy to: "+targets);
      status.setRunning(true);
      currentStatus = status;
      ProgressEvent event =  new ProgressEvent(deployment, currentStatus);
      notify(event);
      for(DeploymentTarget target : targets)
      {
         if(isCancelled)
         {
            status = new SerializableDeploymentStatus(command, StateType.CANCELLED);
            status.setMessage("Undeploy has been cancelled");
            currentStatus = status;
            event =  new ProgressEvent(deployment, currentStatus);
            notify(event);
            break;
         }

         try
         {
            target.remove(deployment);
            status = new SerializableDeploymentStatus(command, StateType.COMPLETED);
            status.setTarget(target);
            status.setMessage("Completed undeploy for target: "+target);
            status.setCompleted(true);
            currentStatus = status;
            event =  new ProgressEvent(deployment, currentStatus);
            notify(event);
         }
         catch(Exception e)
         {
            status = new SerializableDeploymentStatus(command, StateType.FAILED);
            status.setTarget(target);
            status.setFailure(e);
            status.setFailed(true);
            currentStatus = status;
            ProgressEvent error = new ProgressEvent(deployment, currentStatus);
            notify(error);
            break;
         }
      }
   }
   
   protected void redeploy()
   {
      SerializableDeploymentStatus status = new SerializableDeploymentStatus(command, StateType.RUNNING);
      status.setMessage("Running redeploy to: "+targets);
      status.setRunning(true);
      currentStatus = status;
      ProgressEvent event =  new ProgressEvent(deployment, currentStatus);
      notify(event);
      for(DeploymentTarget target : targets)
      {
         if(isCancelled)
         {
            status = new SerializableDeploymentStatus(command, StateType.CANCELLED);
            status.setMessage("Redeploy has been cancelled");
            currentStatus = status;
            event =  new ProgressEvent(deployment, currentStatus);
            notify(event);
            break;
         }

         try
         {
            target.redeploy(deployment);
            status = new SerializableDeploymentStatus(command, StateType.COMPLETED);
            status.setTarget(target);
            status.setMessage("Completed redeploy for target: "+target);
            status.setCompleted(true);
            currentStatus = status;
            event =  new ProgressEvent(deployment, currentStatus);
            notify(event);
         }
         catch(Exception e)
         {
            status = new SerializableDeploymentStatus(command, StateType.FAILED);
            status.setTarget(target);
            status.setFailure(e);
            status.setFailed(true);
            currentStatus = status;
            ProgressEvent error = new ProgressEvent(deployment, currentStatus);
            notify(error);
            break;
         }
      }
   }
   
   protected void prepare()
   {
      SerializableDeploymentStatus status = new SerializableDeploymentStatus(command, StateType.RUNNING);
      status.setMessage("Running prepare to: "+targets);
      status.setRunning(true);
      currentStatus = status;
      ProgressEvent event =  new ProgressEvent(deployment, currentStatus);
      notify(event);
      for(DeploymentTarget target : targets)
      {
         if(isCancelled)
         {
            status = new SerializableDeploymentStatus(command, StateType.CANCELLED);
            status.setMessage("Prepare has been cancelled");
            currentStatus = status;
            event =  new ProgressEvent(deployment, currentStatus);
            notify(event);
            break;
         }

         try
         {
            target.prepare(deployment);
            status = new SerializableDeploymentStatus(command, StateType.COMPLETED);
            status.setTarget(target);
            status.setMessage("Completed prepare for target: "+target);
            status.setCompleted(true);
            currentStatus = status;
            event =  new ProgressEvent(deployment, currentStatus);
            notify(event);
         }
         catch(Exception e)
         {
            status = new SerializableDeploymentStatus(command, StateType.FAILED);
            status.setTarget(target);
            status.setFailure(e);
            status.setFailed(true);
            currentStatus = status;
            ProgressEvent error = new ProgressEvent(deployment, currentStatus);
            notify(error);
            break;
         }
      }
   }
}
