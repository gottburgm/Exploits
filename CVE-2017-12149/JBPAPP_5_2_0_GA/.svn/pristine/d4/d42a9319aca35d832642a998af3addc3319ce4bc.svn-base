#!/bin/sh
### ====================================================================== ###
##  jarcheck.sh                                                             ##
##  A script for obtaining MD5 checksums on the JARs in a JBoss EAP         ##
##  distribution.                                                           ##
### ====================================================================== ###

# Setup the current directory
DIRNAME=`dirname $0`

# Setup JBOSS_HOME and EAP_ROOT
if [ "x$JBOSS_HOME" = "x" ]; then
    # get the full path (without any relative bits)
    JBOSS_HOME=`cd $DIRNAME/..; pwd`
fi

EAP_ROOT=`cd $JBOSS_HOME/..; pwd`

# Setup Java
if [ "x$JAVA" = "x" ]; then
    if [ "x$JAVA_HOME" != "x" ]; then
	JAVA="$JAVA_HOME/bin/java"
    else
	JAVA="java"
    fi
fi

# Setup JARCHECK_JAR
JARCHECK_JAR="$JBOSS_HOME/bin/jarcheck.jar"
if [ ! -f "$JARCHECK_JAR" ]; then
    die "Missing required file: $JARCHECK_JAR"
fi

# Run JARCheck
eval \"$JAVA\" \
  -jar $JARCHECK_JAR \
  $EAP_ROOT > jarcheck-report.txt
