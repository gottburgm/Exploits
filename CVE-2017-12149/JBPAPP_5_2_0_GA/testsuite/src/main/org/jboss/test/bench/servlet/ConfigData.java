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
package org.jboss.test.bench.servlet;

import java.util.ArrayList;
import java.util.Hashtable;

public class ConfigData {
	ArrayList names = new ArrayList();
	Hashtable infos = new Hashtable();

	public ConfigData() {
		setInfo("Hardware", "");
		setInfo("CPU", "");
		setInfo("RAM", "");
		setInfo("OS", "");
		setInfo("JDK Vendor/Version", "");
		setInfo("EJB Server", "");
		setInfo("Servlet Engine", "");
		setInfo("Web Server", "");
		setInfo("DB", "");
	}

	public int size() {
		return infos.size();
	}

	public String getName(int i) {
		return (String)names.get(i);
	}

	public String getValue(int i) {
		return (String)infos.get(names.get(i));
	}

	public void setInfo(String name, String value) {
		if (!infos.containsKey(name)) names.add(name);
		infos.put(name, value);
	}
}

