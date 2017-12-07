<?xml version="1.0" encoding="UTF-8"?>
<profiles xmlns="urn:jboss:profileservice:profiles:1.0">

	<profile name="ignored-profile">
		<profile-source>
			<source>${jboss.server.base.url}conf</source>	
		</profile-source>
		<deployment>someDeployment</deployment>
	</profile>

	<profile name="default">
		<profile-source>
			<source>${jboss.server.base.url}deploy</source>
			<source>${jboss.server.base.url}deploy2</source>
		</profile-source>
		<sub-profile>jmx</sub-profile>
		<sub-profile>seam</sub-profile>
		<sub-profile>jboss-web</sub-profile>
		<sub-profile>clustering</sub-profile>
	</profile>

</profiles>