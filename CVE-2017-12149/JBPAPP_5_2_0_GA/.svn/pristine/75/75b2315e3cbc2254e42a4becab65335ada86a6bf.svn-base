http://jira.jboss.com/jira/browse/JBAS-1957

Until we find a way to automate this test, copy test-service.xml next to ./deploy
and deploy scanner-service.xml. Scanner is configured with:

      <attribute name="URLs">
         non-existent-deploy/,
         non-existent-service.xml,
	     test-service.xml
      </attribute>
      
If test-service.xml gets deployed this means non-existent-deploy/ and
non-existent-service.xml have been correctly skipped,
with a warning of some short.