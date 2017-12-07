package org.jboss.test.security.test.authorization.secured;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Test verifies that there is no jbossws console security baypass in secured profiles.
 *
 * @author rsvoboda@redhat.com
 */
public class HttpRequestJBossWSAuthenticationUnitTestCase extends AbstractHttpAuthenticationUnitTest {

    public HttpRequestJBossWSAuthenticationUnitTestCase(String name) {
        super(name);
    }

    protected URL getURL() throws MalformedURLException {
        return new URL("http://" + getServerHost() + ":8080/jbossws");
    }
}
