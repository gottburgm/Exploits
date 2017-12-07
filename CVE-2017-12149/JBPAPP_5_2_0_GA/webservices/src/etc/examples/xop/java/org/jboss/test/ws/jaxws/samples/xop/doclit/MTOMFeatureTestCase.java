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
package org.jboss.test.ws.jaxws.samples.xop.doclit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.soap.MTOMFeature;
import javax.xml.ws.soap.SOAPBinding;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * [JBWS-2448] This test verify the MTOMFeature correctly enable MTOM on client side.
 * 
 * @author alessio.soldano@jboss.com
 * @since 14-Jan-2009
 */
public class MTOMFeatureTestCase extends JBossWSTest {

	public final String TARGET_ENDPOINT_ADDRESS = "http://" + getServerHost() + ":8080/jaxws-samples-xop-doclit/bare";

	protected MTOMEndpoint port;

	public static Test suite() {
		return new JBossWSTestSetup(MTOMFeatureTestCase.class, "jaxws-samples-xop-doclit.war");
	}

	private MTOMEndpoint getPort(boolean mtomEnabled) throws Exception {
		QName serviceName = new QName("http://doclit.xop.samples.jaxws.ws.test.jboss.org/", "MTOMService");
		URL wsdlURL = new URL(TARGET_ENDPOINT_ADDRESS + "?wsdl");

		Service service = Service.create(wsdlURL, serviceName);
		return service.getPort(MTOMEndpoint.class, new MTOMFeature(mtomEnabled));
	}

	@SuppressWarnings("unchecked")
	private static void addMTOMCheckHandler(MTOMEndpoint port) {
		SOAPBinding binding = (SOAPBinding) ((BindingProvider) port).getBinding();
		List<Handler> handlerChain = new ArrayList<Handler>();
		handlerChain.addAll(binding.getHandlerChain());
		handlerChain.add(new MTOMCheckClientHandler());
		binding.setHandlerChain(handlerChain);
	}

	public void testWithMTOMRequest() throws Exception {
		DataHandler dh = new DataHandler("DataHandlerRoundtrip", "text/plain");
		MTOMEndpoint port = getPort(true);
		addMTOMCheckHandler(port);
		DHResponse response = port.echoDataHandler(new DHRequest(dh));
		assertNotNull(response);

		Object content = getContent(response.getDataHandler());
		String contentType = response.getDataHandler().getContentType();

		assertEquals("Server data", content);
		assertEquals("text/plain", contentType);
	}

	public void testWithoutMTOMRequest() throws Exception {
		DataHandler dh = new DataHandler("DataHandlerResponseOptimzed", "text/plain");
		DHResponse response = getPort(false).echoDataHandler(new DHRequest(dh));
		assertNotNull(response);

		Object content = getContent(response.getDataHandler());
		String contentType = response.getDataHandler().getContentType();

		assertEquals("Server data", content);
		assertEquals("text/plain", contentType);
	}
	
	public void testErrorWithoutMTOMRequest() throws Exception {
		DataHandler dh = new DataHandler("DataHandlerResponseOptimzed", "text/plain");
		MTOMEndpoint port = getPort(false);
		addMTOMCheckHandler(port);
		try
		{
			port.echoDataHandler(new DHRequest(dh));
			fail("Exception expected");
		}
		catch (Exception e)
		{
			//OK
		}
	}

	protected Object getContent(DataHandler dh) throws IOException {
		Object content = dh.getContent();

		// Metro returns an ByteArrayInputStream
		if (content instanceof InputStream) {
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(
						(InputStream) content));
				return br.readLine();
			} finally {
				((InputStream) content).close();
			}
		}
		return content;
	}
}
