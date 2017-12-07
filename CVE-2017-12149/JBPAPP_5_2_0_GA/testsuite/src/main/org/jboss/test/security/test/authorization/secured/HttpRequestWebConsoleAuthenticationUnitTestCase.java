package org.jboss.test.security.test.authorization.secured;

import java.net.MalformedURLException;
import java.net.URL;

import javax.naming.Context;

/**
 * Test verifies that there is no jmx-console security baypass in secured profiles.
 *
 * @author rsvoboda@redhat.com
 */
public class HttpRequestWebConsoleAuthenticationUnitTestCase extends AbstractHttpAuthenticationUnitTest {

    public HttpRequestWebConsoleAuthenticationUnitTestCase(String name) {
        super(name);
    }

    protected URL getURL() throws MalformedURLException {
        return new URL("http://" + getServerHost() + ":8080/web-console");
    }
}
