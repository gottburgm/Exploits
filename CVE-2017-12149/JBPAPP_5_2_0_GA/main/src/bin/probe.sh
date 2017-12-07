#!/bin/sh
### ====================================================================== ###
##                                                                          ##
##  JGroups Cluster Discovery Script                                        ##
##                                                                          ##
### ====================================================================== ###

# Discovers all UDP-based members running on a certain mcast address (use -help for help)
# Probe [-help] [-addr <addr>] [-port <port>] [-ttl <ttl>] [-timeout <timeout>]

CLASSPATH=.:../lib/commons-logging.jar:../server/all/lib/jgroups.jar:$CLASSPATH

# OS specific support (must be 'true' or 'false').
cygwin=false;
case "`uname`" in
    CYGWIN*)
        cygwin=true
        ;;
esac

# Force use of IPv4 stack
JAVA_OPTS="$JAVA_OPTS -Djava.net.preferIPv4Stack=true"

if [ $cygwin = "true" ]; then
   CP=`cygpath -wp $CLASSPATH`
else
   CP=$CLASSPATH
fi

java $JAVA_OPTS -cp $CP org.jgroups.tests.Probe $*
