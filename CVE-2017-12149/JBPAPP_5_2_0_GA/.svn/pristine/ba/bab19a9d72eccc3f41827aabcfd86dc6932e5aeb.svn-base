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

/**
 * @author Gavin King
 */
public class Name implements Serializable {
	private String firstName;
	private String lastName;
	private Character initial;
	
	public Name() {}
	
	public Name(String first, Character middle, String last) {
		firstName = first;
		initial = middle;
		lastName = last;
	}
	
	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public Character getInitial() {
		return initial;
	}

	public void setInitial(Character initial) {
		this.initial = initial;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	
	public String toString() {
		StringBuffer buf = new StringBuffer()
			.append(firstName)
			.append(' ');
		if (initial!=null) buf.append(initial)
			.append(' ');
		return buf.append(lastName)
			.toString();
	}

}

