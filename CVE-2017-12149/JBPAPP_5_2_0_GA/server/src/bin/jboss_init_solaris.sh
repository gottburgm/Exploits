#!/sbin/sh
#
# $Id$
#
# JBoss Applicaton Server Control Script for Solaris
#

##################################################################
#
# Installation Instructions:
# 1. Copy this file to /etc/init.d and make it executable:
#    cp -p ${JBOSS_HOME}/bin/jboss_init_solaris.sh /etc/init.d/jboss
#    chmod 0755 /etc/init.d/jboss
# 2. Create softlinks to the file from the various startup and shutdown 
#    directories:
#    ln -s /etc/init.d/jboss /etc/rc2.d/K01jboss
#    ln -s /etc/init.d/jboss /etc/rc3.d/S99jboss
# 3. Change the variables defined below to match your JBoss installation.
#

##################################################################
#
# The following variables should be defined.
#
# JBOSS_HOME - root directory for jboss installation.
#   The default is /opt/jboss/jboss-as
#
# SERVER_NAME - server instance name.  Normally "all",
#   "default", or "minimal".
#   Maps to server instance at $JBOSS_HOME/server/$SERVER_NAME
#   The default value is usually "default".
#
# JBOSS_OPTIONS - additional options passed to run.sh.  For example,
#   "-b 192.168.13.106".  The default is "" (no options).
#
# JBOSS_USER - the username by which the jboss application server
#   is started.  The default user is jboss.
#
# JBOSS_CONSOLE - file where jboss console logging will be written
#   Use "/dev/null" if console logging is not desired.
#   The default is /var/opt/jboss/<user>_<instance>.log
#
# JAVA_HOME should also be defined in the target users environment, such
#   as is the case when it is exported from ~/.profile.
#
#
JBOSS_HOME=/opt/jboss/jboss-as
JBOSS_USER=jboss
SERVER_NAME=default
JBOSS_OPTIONS=""
JBOSS_CONSOLE="/var/opt/jboss/${JBOSS_USER}_${SERVER_NAME}.log"

umask 022

PATH=/sbin:/usr/sbin:/usr/bin:$PATH
export PATH

#Usage: isJBossRunning <jbossHome> <jbossUserId> <instance>
# sets non zero return code if Jboss is not running
# sets global isJBossRunningPid with 1st process id if it is running 
isJBossRunning()
{
    JBossUserId=$1
    JBossHome=$2
    JBossInstance=$3
    # Note that we don't check $instance, as it will be truncated in
    # the -o args output below

    # Find correct run.sh process (we might find more than 1 candidate)
    isJBossRunningPid=`ps -u ${JBossUserId} -o pid -o args | \
        grep 'java\ .*run\.sh' | awk '{print $1;}'`
    if [ "x${isJBossRunningPid}" = "x" ]; then
      return 1
    fi

    # Find java process with one of the above processes as parent
    for JBossPid in ${isJBossRunningPid}; do
      isPidRunning=`ps -u ${JBossUserId} -o pid -o args -o ppid | \
        grep 'java\ .*run\.sh' | awk '{print $1;}'`
      if [ "x${isPidRunning}" != "x" ]; then
        args=`pargs -a ${JBossPid} 2>/dev/null`
        test "x${args}" = "x" && continue
        argv=""
        for i in ${args}
        do
            argv="$argv `echo $i | grep -v '^argv'`"
        done
        inst=`echo ${argv} | grep "\-c ${JBossInstance}" 2>/dev/null`
        test "x${inst}" != "x" && return 0
      fi
    done
    return 1
}

#USAGE: isUserOK
# prints any problems on standard error and returns with a non-zero status
# returns a 0 status if all is OK
isUserOK()
{
    # check if the user exists
    id ${JBOSS_USER} >/dev/null 2>&1
    if [ $? -ne 0 ] ; then
	echo "ERROR: The user ${JBOSS_USER} does not exist." 1>&2
	return 1
    fi

    # check if we are the user
    username=`id | sed -e "s/[^(]*(//" -e "s/).*//"`
    if [ x"${username}" = x"${JBOSS_USER}" ]; then
	su_req=0
    else
	su_req=1
    fi
    # check if the user has write permission on the jboss instance directory
    if [ ${su_req} -eq 1 ]; then
    	su ${JBOSS_USER} -c "test -w ${JBOSS_HOME}/server/${SERVER_NAME}" >/dev/null 2>&1
    else
    	test -w ${JBOSS_HOME}/server/${SERVER_NAME} >/dev/null 2>&1
    fi
    if [ $? -ne 0 ]; then
	echo "ERROR: The user ${JBOSS_USER} does not have write access to ${JBOSS_HOME}/server/${SERVER_NAME}" 1>&2
	return 2
    fi

    # user must have JAVA_HOME defined
    if [ ${su_req} -eq 1 ]; then
	su ${JBOSS_USER} -c 'which java || [ -d $JAVA_HOME ]' >/dev/null 2>&1
    else
	(which java || [ -d $JAVA_HOME ]) >/dev/null 2>&1
    fi
    if [ $? -ne 0 ]; then
	echo "ERROR: The user \"${JBOSS_USER}\" does not have \$JAVA_HOME defined; either define \$JAVA_HOME for this user or ensure that a path to \"java\" exists." 1>&2
	return 3
    fi

    # user looks good so far
    return 0
}

# Usage: startJBoss
startJBoss()
{
    # make sure the console log exists with appropriate permissions for JBOSS_USER
    if [ x"${JBOSS_CONSOLE}" != "x" ]; then
	JBOSS_CONSOLE_DIR=`echo ${JBOSS_CONSOLE} | sed -e "s/\/[^/]*$//"`
	if [ ! -d ${JBOSS_CONSOLE_DIR} ]; then
	    mkdir -p ${JBOSS_CONSOLE_DIR}
	    chmod 0755 ${JBOSS_CONSOLE_DIR} 
	    chown ${JBOSS_USER} ${JBOSS_CONSOLE_DIR}
	fi
    fi

    # do some basic error checking
    if [ ! -d ${JBOSS_HOME} ]; then
	echo "ERROR: JBOSS_HOME is not a valid directory : ${JBOSS_HOME}" 1>&2
	return 1
    fi

    isUserOK || return 2

    isJBossRunning ${JBOSS_USER} ${JBOSS_HOME} ${SERVER_NAME}
    if [ $? = 0 ]; then
	echo "JBoss AS PID $isJBossRunningPid is already running for user ${JBOSS_USER} at ${JBOSS_HOME} with instance ${SERVER_NAME}" 1>&2
	return 3
    fi

    # keep last version of the console log around
    [ -f ${JBOSS_CONSOLE} ] && yes | mv ${JBOSS_CONSOLE} ${JBOSS_CONSOLE}.old

    # JBoss is not running, start it up
    CMD_START="${JBOSS_HOME}/bin/run.sh -c ${SERVER_NAME} ${JBOSS_OPTIONS}"
    if [ ${su_req} -eq 1 ]; then
	su ${JBOSS_USER} -c "sh $CMD_START" >${JBOSS_CONSOLE} 2>&1 &
    else
	sh $CMD_START >${JBOSS_CONSOLE} 2>&1 &
    fi
    echo "Starting JBoss AS for user ${JBOSS_USER} at ${JBOSS_HOME} with instance ${SERVER_NAME}."
    
    # wait a few seconds then check if it started ok
    #isJBossRunning ${JBOSS_USER} ${JBOSS_HOME} ${SERVER_NAME} || sleep 2 ||
    #isJBossRunning ${JBOSS_USER} ${JBOSS_HOME} ${SERVER_NAME} || sleep 3 ||
    #echo "... server not started yet. Check the log files for errors"
}

# Usage: stopJBoss
stopJBoss()
{
    # return silently if JBoss AS is not running
    isJBossRunning ${JBOSS_USER} ${JBOSS_HOME} ${SERVER_NAME} || return 0

    # JBoss is running and $isJBossRunningPid is set to the process id
    #  SIGTERM does a graceful shutdown like ^C
    echo "Stopping JBoss AS PID $isJBossRunningPid for user ${JBOSS_USER} at ${JBOSS_HOME} with instance ${SERVER_NAME}"
    /usr/bin/kill -s TERM $isJBossRunningPid

    # wait for up to 30 seconds for the process to terminate gracefully
    isJBossRunning ${JBOSS_USER} ${JBOSS_HOME} ${SERVER_NAME} && printf "please wait " && sleep 5 &&
    isJBossRunning ${JBOSS_USER} ${JBOSS_HOME} ${SERVER_NAME} && printf "." && sleep 10 &&
    isJBossRunning ${JBOSS_USER} ${JBOSS_HOME} ${SERVER_NAME} && printf "." && sleep 15 &&
    isJBossRunning ${JBOSS_USER} ${JBOSS_HOME} ${SERVER_NAME} && 
    echo " NOT Terminated!\nWait a moment then check to see if the process has shut down gracefully,\nor terminate it now with:\n  \"/usr/bin/kill -s KILL $isJBossRunningPid\"" >&2 && return 1
}

# Usage: stopJBoss
statusJBoss()
{
    isJBossRunning ${JBOSS_USER} ${JBOSS_HOME} ${SERVER_NAME}
    if [ $? = 0 ]; then
	echo "JBoss AS PID $isJBossRunningPid is running for user ${JBOSS_USER} at ${JBOSS_HOME} with instance ${SERVER_NAME}" 1>&2
	return 0
    else
	echo "JBoss AS is not running for user ${JBOSS_USER} at ${JBOSS_HOME} with instance ${SERVER_NAME}" 1>&2
	return 1
    fi
}


case "$1" in
    start_msg)
	echo "Starting JBoss"
	;;
    start)
	startJBoss
	;;
    stop_msg)
	echo "Stopping JBoss"
	;;
    stop)
	stopJBoss
	;;
    restart)
	stopJBoss
	startJBoss
	;;
    status)
	statusJBoss
	;;
    *)
	echo "usage: $0 (start|stop|restart|status)" 1>&2
	exit 1
esac

exit $?
