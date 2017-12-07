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
package org.jboss.test.hibernate.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Gavin King
 */
public class User implements Serializable {
	private Long id;
	private String handle;
	private String password;
	private Name name;
	private Calendar timeOfCreation;
	private Calendar timeOfLastUpdate;
	private Set previousPasswords = new HashSet();
	private List roles = new ArrayList();
	private List userRoles = new ArrayList();
	private String email;
	
	public String getHandle() {
		return handle;
	}

	public void setHandle(String handle) {
		this.handle = handle;
	}

	public Long getId() {
		return id;
	}

	private void setId(Long id) {
		this.id = id;
	}

	public Name getName() {
		return name;
	}

	public void setName(Name name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	/**
	 * Change the password, as long as the new password
	 * has not already been used.
	 */
	public boolean changePassword(String newPassword) {
		if ( 
			password.equals(newPassword) || 
			previousPasswords.contains(newPassword) 
		) {
			return false;
		}
		else {
			previousPasswords.add(password);
			password = newPassword;
			return true;
		}
	}
	
	/**
	 * Many-to-many association to Role. This is
	 * a collection of Roles (if we don't want the
	 * extra information defined by UserRole).
	 */
	public List getRoles() {
		return roles;
	}

	private void setRoles(List roles) {
		this.roles = roles;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Calendar getTimeOfCreation() {
		return timeOfCreation;
	}

	public void setTimeOfCreation(Calendar timeOfCreation) {
		this.timeOfCreation = timeOfCreation;
	}

	public Calendar getTimeOfLastUpdate() {
		return timeOfLastUpdate;
	}

	private void setTimeOfLastUpdate(Calendar timeOfLastUpdate) {
		this.timeOfLastUpdate = timeOfLastUpdate;
	}
	
	public UserRole addRole(Role role) {
		if ( getRoles().indexOf(role)>=0 ) {
			throw new RuntimeException("role already assigned");
		}
		getRoles().add(role);
		role.getUsers().add(this);
		UserRole ur = new UserRole(this, role);
		getUserRoles().add(ur);
		return ur;
	}
	
	public void removeRole(int selectedRole) {
		if ( selectedRole>getUserRoles().size() ) {
			throw new RuntimeException("selected role does not exist");
		}
		UserRole ur = (UserRole) getUserRoles().remove(selectedRole);
		ur.getRole().getUsers().remove(this);
		getRoles().remove( ur.getRole() );
	}
	
	/**
	 * Many-to-many association to Role. This
	 * is a collection of UserRoles, the association
	 * class.
	 */
	public List getUserRoles() {
		return userRoles;
	}

	private void setUserRoles(List userRoles) {
		this.userRoles = userRoles;
	}

	public Set getPreviousPasswords() {
		return previousPasswords;
	}

	private void setPreviousPasswords(Set previousPasswords) {
		this.previousPasswords = previousPasswords;
	}

	//it is best to implement equals()/hashCode()
	//to compare a "business key" (in this case
	//the unique handle of the User) rather than
	//the surrogate id
	
	public boolean equals(Object other) {
		if (other==null) return false;
		if ( !(other instanceof User) ) return false;
		return ( (User) other ).getHandle().equals(handle);
	}
	
	public int hashCode() {
		return handle.hashCode();
	}


}

