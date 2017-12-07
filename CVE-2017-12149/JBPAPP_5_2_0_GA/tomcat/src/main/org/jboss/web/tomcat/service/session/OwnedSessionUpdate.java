/**
 * 
 */
package org.jboss.web.tomcat.service.session;

public class OwnedSessionUpdate
{
   private final String owner;
   private final long updateTime;
   private final int maxInactive;
   private boolean passivated;

   public OwnedSessionUpdate(String owner, long updateTime, int maxInactive, boolean passivated)
   {
      this.owner = owner;
      this.updateTime = updateTime;
      this.maxInactive = maxInactive;
      this.passivated = passivated;
   }
   
   public boolean isPassivated()
   {
      return passivated;
   }

   void setPassivated(boolean passivated)
   {
      this.passivated = passivated;
   }

   public String getOwner()
   {
      return owner;
   }

   public long getUpdateTime()
   {
      return updateTime;
   }

   public int getMaxInactive()
   {
      return maxInactive;
   }
}