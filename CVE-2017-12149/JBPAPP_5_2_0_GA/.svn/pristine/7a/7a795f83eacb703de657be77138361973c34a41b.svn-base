/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.samples.swaref.jaxws;

import org.jboss.test.ws.jaxws.samples.swaref.DocumentPayload;

import javax.activation.DataHandler;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttachmentRef;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "parameterAnnotation", namespace = "http://swaref.samples.jaxws.ws.test.jboss.org/")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "parameterAnnotation", namespace = "http://swaref.samples.jaxws.ws.test.jboss.org/", propOrder = {
		"arg0",
		"arg1",
		"arg2"
		})
public class ParameterAnnotation {

	@XmlElement(name = "arg0", namespace = "")	
	private DocumentPayload arg0;

	@XmlElement(name = "arg1", namespace = "")
	private String arg1;

	@XmlElement(name = "arg2", namespace = "")
	@XmlAttachmentRef
	private DataHandler arg2;


	public DocumentPayload getArg0()
	{
		return arg0;
	}

	public void setArg0(DocumentPayload arg0)
	{
		this.arg0 = arg0;
	}

	/**
	 *
	 * @return
	 *     returns DataHandler
	 */
	public DataHandler getArg2() {
		return this.arg2;
	}

	/**
	 *
	 * @param arg2
	 *     the value for the arg0 property
	 */
	public void setArg2(DataHandler arg2) {
		this.arg2 = arg2;
	}

	/**
	 *
	 * @return
	 *     returns String
	 */
	public String getArg1() {
		return this.arg1;
	}

	/**
	 *
	 * @param arg1
	 *     the value for the arg1 property
	 */
	public void setArg1(String arg1) {
		this.arg1 = arg1;
	}

}
