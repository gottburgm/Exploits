package org.jboss.ha.framework.server.lock;

import java.io.Serializable;

import org.jboss.ha.framework.interfaces.ClusterNode;

/** 
 * Return value for a "remoteLock" call. This class is public as an
 * aid in unit testing.
 */   
public class RemoteLockResponse implements Serializable
{  
   public enum Flag 
   { 
      /** Lock acquired on responding node */
      OK,
      /** Attempt to acquire local lock failed */
      FAIL, 
      /** 
       * Request rejected either because lock is held or
       * local node is attempting to acquire lock.
       */
      REJECT 
   }
   
   /** The serialVersionUID */
   private static final long serialVersionUID = -8878607946010425555L;
   
   public final RemoteLockResponse.Flag flag;
   public final ClusterNode holder;
   public final ClusterNode responder;
   
   public RemoteLockResponse(ClusterNode responder, RemoteLockResponse.Flag flag)
   {
      this(responder, flag, null);
   }
   
   public RemoteLockResponse(ClusterNode responder, RemoteLockResponse.Flag flag, ClusterNode holder)
   {
      this.flag = flag;
      this.holder = holder;
      this.responder = responder;
   }
}