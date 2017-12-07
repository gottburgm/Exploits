To make HornetQ the default messaging provider, run the switch.sh (switch.bat
if on Windows) on a clean install. This will remove all JBoss Messaging 
components and replace them with HornetQ.

Call the switch.sh script with -Dbackup=true to create additional profiles for
dedicated backup server usage.

You can find the HornetQ documentation at http://jboss.org/hornetq/docs.html.

There are some JMS and Java EE examples under the examples directory. To run
these, you will need Ant 1.7 or later and Java 6. To run the JMS examples, cd
into the example directory of choice and run ant, for instance:

cd examples/jms/queue
ant

To run the Java EE examples, cd into the example directory and run "ant 
deploy" to create a new configuration and start the server, and then, in a new
window, run ant. For instance:

cd examples/javaee/mdb-bmt
ant deploy

and once the server has started, in a new window, type:

cd examples/javaee/mdb-bmt
ant

