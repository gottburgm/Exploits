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
package org.jboss.test.security.test.authorization.secured;

import java.io.BufferedInputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Test verifies that there is no admin-console security baypass in secured profiles.
 * @author <a href="mailto:pskopekf@redhat.com">Peter Skopek</a>
 * @version $Revision: $
 */
public class HttpRequestAdminConsoleAuthenticationUnitTestCase extends
		AbstractHttpAuthenticationUnitTest {

	public HttpRequestAdminConsoleAuthenticationUnitTestCase(String name) {
        super(name);
    }

	private String getPage(HttpURLConnection con) throws Exception {
		BufferedInputStream is = new BufferedInputStream(con.getInputStream());
		int ch;
		StringBuffer sb = new StringBuffer();
		while ((ch = is.read()) != -1 ) {
			sb.append((char)ch);
		}
		String page = sb.toString();
		log.debug("page"+page);
		return page;
	}
	
	
	private void checkIsLoginPage(String requestMethod) throws Exception {
		con.setRequestMethod(requestMethod);
		con.connect();			

		assertTrue("Has to be login page", isLoginPage(getPage(con)));
		
	}
	
	
	public void testGet() throws Exception {
		checkIsLoginPage(GET);
	}

	/* (non-Javadoc)
	 * @see org.jboss.test.security.test.authorization.secured.AbstractHttpAuthenticationUnitTest#getURL()
	 */
	@Override
	protected URL getURL() throws MalformedURLException {
		// http://localhost:8080/admin-console/secure/summary.seam
        return new URL("http://" + getServerHost() + ":8080/admin-console/secure/summary.seam");
	}
	
	private boolean isLoginPage(String page) {
		boolean isLoginPage = page.indexOf("login_form") != -1;
		isLoginPage = isLoginPage && page.indexOf("login.seam") != -1;
		isLoginPage = isLoginPage && page.indexOf("login_form:submit") != -1;
		isLoginPage = isLoginPage && page.indexOf("login_form:password") != -1;

		return isLoginPage;
	}

	/* (non-Javadoc)
	 * @see org.jboss.test.security.test.authorization.secured.AbstractHttpAuthenticationUnitTest#testDelete()
	 */
	@Override
	public void testDelete() throws Exception {
		checkIsLoginPage(DELETE);
	}

	/* (non-Javadoc)
	 * @see org.jboss.test.security.test.authorization.secured.AbstractHttpAuthenticationUnitTest#testHead()
	 */
	@Override
	public void testHead() throws Exception {

		// cannot get better information to distinguish non secured page
		
		con.setRequestMethod(HEAD);
		con.connect();			

		assertEquals(HttpURLConnection.HTTP_OK, con.getResponseCode());
		assertTrue("Has to return empty page", getPage(con).equals(""));
		
	}

	/* (non-Javadoc)
	 * @see org.jboss.test.security.test.authorization.secured.AbstractHttpAuthenticationUnitTest#testOptions()
	 */
	@Override
	public void testOptions() throws Exception {
		checkIsLoginPage(OPTIONS);
	}

	/* (non-Javadoc)
	 * @see org.jboss.test.security.test.authorization.secured.AbstractHttpAuthenticationUnitTest#testPost()
	 */
	@Override
	public void testPost() throws Exception {
		checkIsLoginPage(POST);
	}

	/* (non-Javadoc)
	 * @see org.jboss.test.security.test.authorization.secured.AbstractHttpAuthenticationUnitTest#testPut()
	 */
	@Override
	public void testPut() throws Exception {
		checkIsLoginPage(PUT);
	}


}
