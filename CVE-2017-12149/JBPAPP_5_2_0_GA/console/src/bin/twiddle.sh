#!/bin/sh
### ====================================================================== ###
##                                                                          ##
##  JBoss Shutdown Script                                                   ##
##                                                                          ##
### ====================================================================== ###

### $Id: twiddle.sh 113453 2012-08-14 16:03:47Z jiwils $ ###

DIRNAME=`dirname $0`
PROGNAME=`basename $0`
GREP="grep"

#
# Helper to complain.
#
die() {
    echo "${PROGNAME}: $*"
    exit 1
}

# OS specific support (must be 'true' or 'false').
cygwin=false;
case "`uname`" in
    CYGWIN*)
        cygwin=true
        ;;
esac

# Force use of IPv4 stack
JAVA_OPTS="$JAVA_OPTS -Djava.net.preferIPv4Stack=true"

# For Cygwin, ensure paths are in UNIX format before anything is touched
if $cygwin ; then
    [ -n "$JBOSS_HOME" ] &&
        JBOSS_HOME=`cygpath --unix "$JBOSS_HOME"`
    [ -n "$JAVA_HOME" ] &&
        JAVA_HOME=`cygpath --unix "$JAVA_HOME"`
fi

# Setup JBOSS_HOME
if [ "x$JBOSS_HOME" = "x" ]; then
    JBOSS_HOME=`cd $DIRNAME/..; pwd`
fi
export JBOSS_HOME

# Setup the JVM
if [ "x$JAVA_HOME" != "x" ]; then
    JAVA=$JAVA_HOME/bin/java
else
    JAVA="java"
fi

# Setup the classpath
JBOSS_BOOT_CLASSPATH="$JBOSS_HOME/bin/twiddle.jar"

if [ "x$JBOSS_CLASSPATH" = "x" ]; then
    JBOSS_CLASSPATH="$JBOSS_BOOT_CLASSPATH"
    if [ -f /usr/share/jbossas/client/jbossall-client.jar ]; then
        #set classpath to the jar in the rpm if it exists and is a regular file
        JBOSS_CLASSPATH="$JBOSS_CLASSPATH:/usr/share/jbossas/client/jbossall-client.jar"
    else
        JBOSS_CLASSPATH="$JBOSS_CLASSPATH:$JBOSS_HOME/client/jbossall-client.jar"
    fi
    JBOSS_CLASSPATH="$JBOSS_CLASSPATH:$JBOSS_HOME/client/getopt.jar"
    JBOSS_CLASSPATH="$JBOSS_CLASSPATH:$JBOSS_HOME/client/log4j.jar"
    JBOSS_CLASSPATH="$JBOSS_CLASSPATH:$JBOSS_HOME/lib/jboss-jmx.jar"
    JBOSS_CLASSPATH="$JBOSS_CLASSPATH:$JBOSS_HOME/lib/dom4j.jar"
    JBOSS_CLASSPATH="$JBOSS_CLASSPATH:$JBOSS_HOME/lib/jboss-system-jmx.jar"
    JBOSS_CLASSPATH="$JBOSS_CLASSPATH:$JBOSS_HOME/lib/jboss-common-beans.jar"
else
    JBOSS_CLASSPATH="$JBOSS_CLASSPATH:$JBOSS_BOOT_CLASSPATH"
fi

# Setup the java endorsed dirs
JBOSS_ENDORSED_DIRS="$JBOSS_HOME/lib/endorsed"

# For Cygwin, switch paths to Windows format before running java
if $cygwin; then
    JBOSS_HOME=`cygpath --path --windows "$JBOSS_HOME"`
    JAVA_HOME=`cygpath --path --windows "$JAVA_HOME"`
    JBOSS_CLASSPATH=`cygpath --path --windows "$JBOSS_CLASSPATH"`
    JBOSS_ENDORSED_DIRS=`cygpath --path --windows "$JBOSS_ENDORSED_DIRS"`
fi

# Execute the JVM
exec "$JAVA" \
    $JAVA_OPTS \
    -Djava.endorsed.dirs="$JBOSS_ENDORSED_DIRS" \
    -Dprogram.name="$PROGNAME" \
    -classpath $JBOSS_CLASSPATH \
    org.jboss.console.twiddle.Twiddle "$@"
