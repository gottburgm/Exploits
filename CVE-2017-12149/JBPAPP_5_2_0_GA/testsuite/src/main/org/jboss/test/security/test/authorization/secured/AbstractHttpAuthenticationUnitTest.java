package org.jboss.test.security.test.authorization.secured;

import java.net.*;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.test.JBossTestCase;
import org.jboss.test.JBossTestSetup;

/**
 * Test verifies that there is no jmx-console security baypass in secured profiles.
 * Reused test from JBPAPP-3952, JBPAPP-4160.
 *
 * @author bshim@redhat.com
 * @author rsvoboda@redhat.com
 */
public abstract class AbstractHttpAuthenticationUnitTest extends JBossTestCase {
	
	private URL u;
	protected HttpURLConnection con;
	protected static final String GET = "GET";
	protected static final String POST = "POST";
	protected static final String HEAD = "HEAD";
	protected static final String OPTIONS = "OPTIONS";
	protected static final String PUT = "PUT";
	protected static final String DELETE = "DELETE";
	protected static final String TRACE = "TRACE"; 
	
	public AbstractHttpAuthenticationUnitTest(String name){
		super(name);
	}
	
	public void testGet() throws Exception {
		con.setRequestMethod(GET);
		con.connect();			
		assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, con.getResponseCode());
	}
	
	public void testPost() throws Exception {
		con.setRequestMethod(POST);
		con.connect();
		assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, con.getResponseCode());
	}
	
	public void testHead() throws Exception {
		con.setRequestMethod(HEAD);
		con.connect();			
		assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, con.getResponseCode());
	}
	
	public void testOptions() throws Exception {
		con.setRequestMethod(OPTIONS);
		con.connect();
		assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, con.getResponseCode());
	}
	
	public void testPut() throws Exception {
		con.setRequestMethod(PUT);
		con.connect();			
		assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, con.getResponseCode());
	}
	
	public void testTrace()  throws Exception {
		con.setRequestMethod(TRACE);
		con.connect();
                assertEquals(HttpURLConnection.HTTP_BAD_METHOD, con.getResponseCode());
	}
	
	public void testDelete()  throws Exception {
		con.setRequestMethod(DELETE);
		con.connect();
		assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, con.getResponseCode());
	}
	
	protected void setUp() throws Exception {
		super.setUp();
//		u = new URL("http://" + getServerHost() + ":8080/jmx-console");
                u = getURL();
		con = (HttpURLConnection) u.openConnection();
		try {
			con.setDoInput(true);
			con.setRequestProperty("Cookie","MODIFY ME IF NEEDED");
		} finally {
			con.disconnect();
		}
	}
	
        protected abstract URL getURL() throws MalformedURLException;

	protected void tearDown(){
		if (con != null)
			con.disconnect();
	}
}
