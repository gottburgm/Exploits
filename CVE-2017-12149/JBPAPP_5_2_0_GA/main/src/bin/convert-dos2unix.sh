#!/bin/sh
### ====================================================================== ###
##  convert-dos2unix.sh                                                     ##
##  A *.xml/*.properties DOS to UNIX file conversion script.                ##
##                                                                          ##
##  This script converts *.xml and *.properties files contained in the      ## 
##  JBoss EAP distribution to UNIX format using what should be universally  ##
##  available command line utilities.                                       ##
### ====================================================================== ###

# This script lives in jboss-eap-<version>/jboss-as/bin, so we set the root
# directory for the changes accordingly.  This can be changed as necessary.
DIRNAME=`dirname $0`

if [ "x$JBOSS_HOME" = "x" ]; then
    # get the full path (without any relative bits)
    JBOSS_HOME=`cd $DIRNAME/..; pwd`
fi

EAP_ROOT=`cd $JBOSS_HOME/..; pwd`

# Convert *.xml files.
for i in `find $EAP_ROOT -name \*.xml`; do echo $i;sed -i 's/\r$//' $i; done

# Convert *.properties files.
for i in `find $EAP_ROOT -name \*.properties`; do echo $i;sed -i 's/\r$//' $i; done
