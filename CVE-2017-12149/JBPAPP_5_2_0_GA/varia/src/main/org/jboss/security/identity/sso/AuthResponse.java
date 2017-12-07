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
package org.jboss.security.identity.sso;

import java.io.Serializable;

//$Id: AuthResponse.java 81038 2008-11-14 13:43:27Z dimitris@jboss.org $

/**
 *  Represents a SAML Auth Response
 *  @author <a href="mailto:Sohil.Shah@jboss.org">Sohil Shah</a>
 *  @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 *  @since  Apr 10, 2006 
 *  @version $Revision: 81038 $
 */
public class AuthResponse implements Serializable
{ 
   /** The serialVersionUID */
   private static final long serialVersionUID = -7206332679806340854L;
   private String assertingParty = null;
   private String assertToken = null;
   private SSOUser user = null;    
   private boolean success = false;    
   
   public AuthResponse(String assertingParty,String assertToken,
         SSOUser user,boolean success)
   {
       super();
       this.assertingParty = assertingParty;
       this.assertToken = assertToken;
       this.user = user;
       this.success = success;        
   }
   
   public String getAssertingParty()
   {
       return this.assertingParty;
   }
   
   public void setAssertingParty(String assertingParty)
   {
       this.assertingParty = assertingParty;
   }
   
   public String getAssertToken()
   {
       return this.assertToken;
   }
   
   public SSOUser getUser()
   {
       return this.user;
   }
   
   public void setUser(SSOUser user)
   {
       this.user = user;
   }
   
   public boolean isAuthenticated()
   {
       return this.success;
   }    
}
