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
import java.util.List;

/**
 * @author Gavin King
 */
public class Role implements Serializable {
	private Long id;
	private String name;
	private String description;
	private Calendar timeOfCreation;
	private List users= new ArrayList();
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Long getId() {
		return id;
	}

	private void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List getUsers() {
		return users;
	}

	private void setUsers(List users) {
		this.users = users;
	}
	
	public void addUser(User user) {
		users.add(user);
		user.getRoles().add(this);
	}

	public Calendar getTimeOfCreation() {
		return timeOfCreation;
	}

	public void setTimeOfCreation(Calendar timeOfCreation) {
		this.timeOfCreation = timeOfCreation;
	}
	
	//it is best to implement equals()/hashCode()
	//to compare a "business key" (in this case
	//the unique name of the Role) rather than
	//the surrogate id
	
	public boolean equals(Object other) {
		if (other==null) return false;
		if ( !(other instanceof Role) ) return false;
		return ( (Role) other ).getName().equals(name);
	}
	
	public int hashCode() {
		return name.hashCode();
	}

}

