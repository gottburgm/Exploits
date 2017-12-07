<?xml version="1.0" encoding="UTF-8"?>
<profiles
	xmlns="urn:jboss:profileservice:profiles:1.0"
	name="ejb3">

	<profile name="ejb3-deployers">
		<profile-source>
			<source>${jboss.server.base.url}deployers</source>
		</profile-source>
		<sub-profile>metadata-deployers</sub-profile>
		<deployment>ejb3.deployer</deployment>
	</profile>

	<profile name="ejb3-runtime">
		<profile-source>
			<source>${jboss.server.base.url}deploy</source>
		</profile-source>
		<deployment>ejb3-container-jboss-beans.xml</deployment>
		<deployment>ejb3-interceptors-aop.xml</deployment>
		<deployment>ejb3-timer-service.xml</deployment>
	</profile>

</profiles>