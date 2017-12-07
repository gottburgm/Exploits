
The Database Configuration Tool (found in the JBOSS_HOME/tools/ds-builder/ directory) is an Apache Ant script. It sets the database to be used by the JBoss Enterprise Application Platform. A list of the supported databases can be found at https://access.redhat.com/knowledge/articles/113083.

To Configure a Database for Production System Use:

It is a prerequisite that
    i) Apache Ant is installed
    ii) the database that you wish to use must already exist
    iii) a user with permission to make changes to that database must already exist.
    iv) the JDBC driver JAR file for the database must be in the server configuration's lib/ directory.

JBoss Enterprise Application Platform uses a database for to persistence. The default database is Hypersonic but this is not suitable for production systems and is not supported. You must switch to a supported database before running the JBoss Enterprise Application Platform in a production environment.

**Warning** You can only use the Database Configuration Tool to change the database configuration once. Also, it must be run before any other changes are made. If you try to run the script on an installation that has already been configured, it may not work as intended.

To run the tool:

    Back Up Your Server Profile
    Make a copy of the server profile for which you plan to configure your database as the Database Configuration Tool modifies the configuration settings.

    Change to the directory containing the Database Configuration script: cd JBOSS_HOME/tools/ds-builder

    Run the 'ant' command to launch the script and enter the data prompted.

    Following the prompts, enter the following information as it is requested:

        the type of database being used,

        the name of the database,

        the host name or IP Address of the database,

        the TCP port being used for the database,

        the user name needed to access the database, and

        the password for this user account.

