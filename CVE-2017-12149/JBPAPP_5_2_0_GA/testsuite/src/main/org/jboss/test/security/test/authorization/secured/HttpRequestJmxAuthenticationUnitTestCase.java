package org.jboss.test.security.test.authorization.secured;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Test verifies that there is no jmx-console security baypass in secured profiles.
 *
 * @author rsvoboda@redhat.com
 */
public class HttpRequestJmxAuthenticationUnitTestCase extends AbstractHttpAuthenticationUnitTest {

    public HttpRequestJmxAuthenticationUnitTestCase(String name) {
        super(name);
    }

    protected URL getURL() throws MalformedURLException {
        return new URL("http://" + getServerHost() + ":8080/jmx-console");
    }
}
