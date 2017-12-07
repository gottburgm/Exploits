package org.jboss.test.security.test.authorization.secured;

import java.net.URL;
import java.net.MalformedURLException;

/**
 * Test verifies that there is no /status servlet security baypass in secured profiles.
 *
 * @author rsvoboda@redhat.com
 */
public class HttpRequestStatusServletAuthenticationUnitTestCase extends AbstractHttpAuthenticationUnitTest {

    public HttpRequestStatusServletAuthenticationUnitTestCase(String name) {
        super(name);
    }

    protected URL getURL() throws MalformedURLException {
        return new URL("http://" + getServerHost() + ":8080/status");
    }
}
