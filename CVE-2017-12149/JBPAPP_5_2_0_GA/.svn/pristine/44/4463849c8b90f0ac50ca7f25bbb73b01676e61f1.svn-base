<?xml version="1.0" encoding="UTF-8"?>
<profiles
	xmlns="urn:jboss:profileservice:profiles:1.0"
	name="clustering">

	<profile name="clustering-deployers">
		<profile-source>
			<source>${jboss.server.base.url}deployers</source>
		</profile-source>
		<deployment>clustering-deployer-jboss-beans.xml</deployment>
	</profile>

	<profile name="clustering-runtime">
		<profile-source>
			<source>${jboss.server.base.url}deploy</source>
		</profile-source>
		<deployment>httpha-invoker.sar</deployment>
	</profile>

</profiles>