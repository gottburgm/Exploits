This example shows how to run 2 instances of eap each with a live and backup hornetq server.

Firstly make a copy of the all configuration, lets call this hq1.

Now over write the config files in hq1/deploy/hornetq with the ones in the examples hornetq directory.

Now copy the hornetq-backup1 directory into the hq1/deply direcoty.

we now have a server configured with live and backup.

Now make a second configuration by copying hq1, lets call this hq2.

Now start the first server by running

./run.sh -c hq1

and now start the second server by running

./run.sh -Djboss.service.binding.set="ports-01" -Dhornetq.data.dir=hornetq-backup1 -Dhornetq.backup1.data.dir=hornetq -c hq2

here you can see we have switched which journals the live and the backup point to.

NB these by default are in the root of the eap directory, you can change this in the hornetq-configuration.xml files if needed.

You now have a symmetrical cluster with backups.

To chain more live backups, simply change what journal each libe backup uses or to add multiple backups simply copy the
hornetq-backup1 directory and configure accordingly