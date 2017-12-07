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
package org.jboss.monitor.client;

/**
 *
 * @see Monitorable
 * @author <a href="mailto:simone.bordet@compaq.com">Simone Bordet</a>
 * @version $Revision: 81030 $
 */
public class BeanCacheSnapshot
	implements java.io.Serializable
{
	// Constants ----------------------------------------------------
	
	// Attributes ---------------------------------------------------
	public String m_application;
	public String m_container;
	public int m_passivatingBeans;
	public int m_cacheMinCapacity;
	public int m_cacheMaxCapacity;
	public int m_cacheCapacity;
	public int m_cacheSize;
	private StringBuffer m_buffer = new StringBuffer();
	
	// Static -------------------------------------------------------

	// Constructors -------------------------------------------------
	public BeanCacheSnapshot() {}
	
	// Public -------------------------------------------------------
	public String toString()
	{
		m_buffer.setLength(0);
		m_buffer.append("Cache Snapshot for application '");
		m_buffer.append(m_application);
		m_buffer.append("', container for bean '");
		m_buffer.append(m_container);
		m_buffer.append("':\nmin capacity: ");
		m_buffer.append(m_cacheMinCapacity);
		m_buffer.append("\nmax capacity: ");
		m_buffer.append(m_cacheMaxCapacity);
		m_buffer.append("\ncapacity: ");
		m_buffer.append(m_cacheCapacity);
		m_buffer.append("\nsize: ");
		m_buffer.append(m_cacheSize);
		m_buffer.append("\nnumber of beans scheduled for passivation: ");
		m_buffer.append(m_passivatingBeans);
		return m_buffer.toString();
	}
}
