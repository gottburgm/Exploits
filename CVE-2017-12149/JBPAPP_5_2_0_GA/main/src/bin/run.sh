#!/bin/sh
### ====================================================================== ###
##                                                                          ##
##  JBoss Bootstrap Script                                                  ##
##                                                                          ##
### ====================================================================== ###

### $Id: run.sh 113844 2012-11-23 19:57:10Z fnasser@redhat.com $ ###

DIRNAME=`dirname $0`
PROGNAME=`basename $0`
GREP="grep"

# Set conf if specified, else set to default
JBOSSCONF="default"
CONF_SPECIFIED=false

arg_count=1
eval SWITCH=\${$arg_count}
while [ ! -z "$SWITCH" ]
do

        if [ "$SWITCH" = "-c" ]; then
            eval JBOSSCONF=\${`expr $arg_count + 1`}
            CONF_SPECIFIED=true
        fi

        echo "$SWITCH" | grep "^\-\-configuration=" > /dev/null
        if [ $? -eq 0 ]; then
            JBOSSCONF=`echo $SWITCH|sed 's/\-\-configuration=//'`
        fi

        echo "$SWITCH" | grep "^\-Djboss.server.base.dir=" #> /dev/null
        if [ $? -eq 0 ]; then
            JBOSS_BASE_DIR=`echo $SWITCH|sed 's/\-Djboss.server.base.dir=//'`
        fi

	echo "$SWITCH" | grep "^--run-conf=" > /dev/null
	if [ $? -eq 0 ]; then
		RUN_CONF=`echo $SWITCH|sed 's/--run-conf=//'`

		#rebuild the parameter list to drop this one
		set -- `echo "$@" | sed "s!$SWITCH!!"`
		eval SWITCH=\${$arg_count}
		continue
	fi

        arg_count=`expr $arg_count + 1`
        eval SWITCH=\${$arg_count}
done

if [ x${CONF_SPECIFIED} = "xfalse" ]
then
   set -- "-c" ${JBOSSCONF} $@
fi

# Use the maximum available, or set MAX_FD != -1 to use that
MAX_FD="maximum"

#
# Helper to complain.
#
warn() {
    echo "${PROGNAME}: $*"
}

#
# Helper to puke.
#
die() {
    warn $*
    exit 1
}

# OS specific support (must be 'true' or 'false').
cygwin=false;
darwin=false;
linux=false;
sunos=false;
case "`uname`" in
    CYGWIN*)
        cygwin=true
        ;;

    Darwin*)
        darwin=true
        ;;

    Linux)
        linux=true
        ;;

    SunOS)
        sunos=true
        ;;
esac

# Solaris standard grep command  does not support "-e"
if $sunos ; then
    GREP=/usr/xpg4/bin/grep
fi

# Read an optional running configuration file
if [ "x$RUN_CONF" = "x" ]; then

    if [ ! -z "$JBOSSCONF" ] && [ -f "$JBOSS_BASE_DIR/$JBOSSCONF/run.conf" ]; then
        RUN_CONF="$JBOSS_BASE_DIR/$JBOSSCONF/run.conf"
    elif [ ! -z "$JBOSSCONF" ] && [ -f "$DIRNAME/../server/$JBOSSCONF/run.conf" ]; then
        RUN_CONF="$DIRNAME/../server/$JBOSSCONF/run.conf"
    else
        RUN_CONF="$DIRNAME/run.conf"
    fi
fi
if [ -r "$RUN_CONF" ]; then
    . "$RUN_CONF"
fi

# Force use of IPv4 stack
JAVA_OPTS="$JAVA_OPTS -Djava.net.preferIPv4Stack=true"

# For Cygwin, ensure paths are in UNIX format before anything is touched
if $cygwin ; then
    [ -n "$JBOSS_HOME" ] &&
        JBOSS_HOME=`cygpath --unix "$JBOSS_HOME"`
    [ -n "$JAVA_HOME" ] &&
        JAVA_HOME=`cygpath --unix "$JAVA_HOME"`
    [ -n "$JAVAC_JAR" ] &&
        JAVAC_JAR=`cygpath --unix "$JAVAC_JAR"`
fi

# Setup JBOSS_HOME
if [ "x$JBOSS_HOME" = "x" ]; then
    # get the full path (without any relative bits)
    JBOSS_HOME=`cd $DIRNAME/..; pwd`
fi
export JBOSS_HOME

# Increase the maximum file descriptors if we can
if [ "$cygwin" = "false" ]; then
    MAX_FD_LIMIT=`ulimit -H -n`
    if [ "$?" -eq 0 ]; then
        # Darwin does not allow RLIMIT_INFINITY on file soft limit
        if [ "$darwin" = "true" -a "$MAX_FD_LIMIT" = "unlimited" ]; then
            MAX_FD_LIMIT=`/usr/sbin/sysctl -n kern.maxfilesperproc`
        fi

	if [ "$MAX_FD" = "maximum" -o "$MAX_FD" = "max" ]; then
	    # use the system max
	    MAX_FD="$MAX_FD_LIMIT"
	fi

	ulimit -n $MAX_FD
	if [ "$?" -ne 0 ]; then
	    warn "Could not set maximum file descriptor limit: $MAX_FD"
	fi
    else
	warn "Could not query system maximum file descriptor limit: $MAX_FD_LIMIT"
    fi
fi

# Setup the JVM
if [ "x$JAVA" = "x" ]; then
    if [ "x$JAVA_HOME" != "x" ]; then
	JAVA="$JAVA_HOME/bin/java"
    else
	JAVA="java"
    fi
fi

# Setup the classpath
runjar="$JBOSS_HOME/bin/run.jar"
if [ ! -f "$runjar" ]; then
    die "Missing required file: $runjar"
fi
JBOSS_BOOT_CLASSPATH="$runjar"

# Tomcat uses the JDT Compiler
# Only include tools.jar if someone wants to use the JDK instead.
# compatible distribution which JAVA_HOME points to
if [ "x$JAVAC_JAR" = "x" ]; then
    JAVAC_JAR_FILE="$JAVA_HOME/lib/tools.jar"
else
    JAVAC_JAR_FILE="$JAVAC_JAR"
fi
if [ ! -f "$JAVAC_JAR_FILE" ]; then
   # MacOSX does not have a seperate tools.jar
   if [ "$darwin" != "true" -a "x$JAVAC_JAR" != "x" ]; then
      warn "Missing file: JAVAC_JAR=$JAVAC_JAR"
      warn "Unexpected results may occur."
   fi
   JAVAC_JAR_FILE=
fi

if [ "x$JBOSS_CLASSPATH" = "x" ]; then
    JBOSS_CLASSPATH="$JBOSS_BOOT_CLASSPATH"
else
    JBOSS_CLASSPATH="$JBOSS_CLASSPATH:$JBOSS_BOOT_CLASSPATH"
fi
if [ "x$JAVAC_JAR_FILE" != "x" ]; then
    JBOSS_CLASSPATH="$JBOSS_CLASSPATH:$JAVAC_JAR_FILE"
fi

# Check for -d32/-d64 in JAVA_OPTS
JVM_OPTVERSION="-version"
JVM_D64_OPTION=`echo $JAVA_OPTS | $GREP "\-d64"`
JVM_D32_OPTION=`echo $JAVA_OPTS | $GREP "\-d32"`
test "x$JVM_D64_OPTION" != "x" && JVM_OPTVERSION="-d64 $JVM_OPTVERSION"
test "x$JVM_D32_OPTION" != "x" && JVM_OPTVERSION="-d32 $JVM_OPTVERSION"

# If -server not set in JAVA_OPTS, set it, if supported
SERVER_SET=`echo $JAVA_OPTS | $GREP "\-server"`
if [ "x$SERVER_SET" = "x" ]; then

    # Check for SUN(tm) JVM w/ HotSpot support
    if [ "x$HAS_HOTSPOT" = "x" ]; then
        HAS_HOTSPOT=`"$JAVA" $JVM_OPTVERSION -version 2>&1 | $GREP -i HotSpot`
    fi

    # Check for OpenJDK JVM w/server support
    if [ "x$HAS_OPENJDK_" = "x" ]; then
        HAS_OPENJDK=`"$JAVA" $JVM_OPTVERSION 2>&1 | $GREP -i OpenJDK`
    fi

    # Enable -server if we have Hotspot or OpenJDK, unless we can't
    if [ "x$HAS_HOTSPOT" != "x" -o "x$HAS_OPENJDK" != "x" ]; then
        # MacOS does not support -server flag
        if [ "$darwin" != "true" ]; then
            JAVA_OPTS="-server $JAVA_OPTS"
            JVM_OPTVERSION="-server $JVM_OPTVERSION"
        fi
    fi
else
    JVM_OPTVERSION="-server $JVM_OPTVERSION"
fi

# Setup JBoss Native library path
#
if [ -d "$JBOSS_HOME/../native/lib" ]; then
    JBOSS_NATIVE_DIR=`cd "$JBOSS_HOME/../native" && pwd`
elif [ -d "$JBOSS_HOME/native/lib" ]; then
    JBOSS_NATIVE_DIR=`cd "$JBOSS_HOME/native" && pwd`
elif [ -d "$JBOSS_HOME/../native/lib64" ]; then
    JBOSS_NATIVE_DIR=`cd "$JBOSS_HOME/../native" && pwd`
elif [ -d "$JBOSS_HOME/native/lib64" ]; then
    JBOSS_NATIVE_DIR=`cd "$JBOSS_HOME/native" && pwd`
elif [ -d "$JBOSS_HOME/../../jboss-ep-5.2/native" ]; then
    JBOSS_NATIVE_DIR=`cd "$JBOSS_HOME/../../jboss-ep-5.2/native" && pwd`
fi
if [ -d "$JBOSS_NATIVE_DIR" ]; then
    if $cygwin; then
        JBOSS_NATIVE_DIR="$JBOSS_NATIVE_DIR/bin"
        export PATH="$JBOSS_NATIVE_DIR:$PATH"
        JBOSS_NATIVE_LIBPATH=`cygpath --path --windows "$JBOSS_NATIVE_DIR"`
    else
        IS_64_BIT_JVM=`"$JAVA" $JVM_OPTVERSION 2>&1 | $GREP -i -e 64-bit -e amd64-64 -e ppc64-64`
        if [ "x$IS_64_BIT_JVM" != "x" ]; then
            JBOSS_NATIVE_DIR="$JBOSS_NATIVE_DIR/lib64"
        else
            JBOSS_NATIVE_DIR="$JBOSS_NATIVE_DIR/lib"
        fi
        if [ "x$LD_LIBRARY_PATH" = "x" ]; then
          LD_LIBRARY_PATH="$JBOSS_NATIVE_DIR"
        else
          LD_LIBRARY_PATH="$JBOSS_NATIVE_DIR:$LD_LIBRARY_PATH"
        fi
        export LD_LIBRARY_PATH
        JBOSS_NATIVE_LIBPATH=$LD_LIBRARY_PATH
    fi
    if [ "x$JAVA_OPTS" = "x" ]; then
        JAVA_OPTS="-Djava.library.path=$JBOSS_NATIVE_LIBPATH"
    else
        JAVA_OPTS="$JAVA_OPTS -Djava.library.path=$JBOSS_NATIVE_LIBPATH"
    fi
fi

# Setup JBoss specific properties
JAVA_OPTS="-Dprogram.name=$PROGNAME $JAVA_OPTS"

# Setup the java endorsed dirs
JBOSS_ENDORSED_DIRS="$JBOSS_HOME/lib/endorsed"

# For Cygwin, switch paths to Windows format before running java
if $cygwin; then
    JBOSS_HOME=`cygpath --path --windows "$JBOSS_HOME"`
    JAVA_HOME=`cygpath --path --windows "$JAVA_HOME"`
    JBOSS_CLASSPATH=`cygpath --path --windows "$JBOSS_CLASSPATH"`
    JBOSS_ENDORSED_DIRS=`cygpath --path --windows "$JBOSS_ENDORSED_DIRS"`
fi

# Display our environment
echo "========================================================================="
echo ""
echo "  JBoss Bootstrap Environment"
echo ""
echo "  JBOSS_HOME: $JBOSS_HOME"
echo ""
echo "  JAVA: $JAVA"
echo ""
echo "  JAVA_OPTS: $JAVA_OPTS"
echo ""
echo "  CLASSPATH: $JBOSS_CLASSPATH"
echo ""
echo "========================================================================="
echo ""

while true; do
   if [ "x$LAUNCH_JBOSS_IN_BACKGROUND" = "x" ]; then
      # Execute the JVM in the foreground
      eval \"$JAVA\" $JAVA_OPTS \
         -Djava.endorsed.dirs=\"$JBOSS_ENDORSED_DIRS\" \
         -classpath \"$JBOSS_CLASSPATH\" \
         org.jboss.Main "$@"
      JBOSS_STATUS=$?
   else
      # Execute the JVM in the background
      eval \"$JAVA\" $JAVA_OPTS \
         -Djava.endorsed.dirs=\"$JBOSS_ENDORSED_DIRS\" \
         -classpath \"$JBOSS_CLASSPATH\" \
         org.jboss.Main "$@" "&"
      JBOSS_PID=$!
      # Trap common signals and relay them to the jboss process
      trap "kill -HUP  $JBOSS_PID" HUP
      trap "kill -TERM $JBOSS_PID" INT
      trap "kill -QUIT $JBOSS_PID" QUIT
      trap "kill -PIPE $JBOSS_PID" PIPE
      trap "kill -TERM $JBOSS_PID" TERM
      if [ "x$JBOSS_PIDFILE" != "x" ]; then
        echo $JBOSS_PID > $JBOSS_PIDFILE
      fi
      # Wait until the background process exits
      WAIT_STATUS=128
      while [ "$WAIT_STATUS" -ge 128 ]; do
         wait $JBOSS_PID 2>/dev/null
         WAIT_STATUS=$?
         if [ "$WAIT_STATUS" -gt 128 ]; then
            SIGNAL=`expr $WAIT_STATUS - 128`
            SIGNAL_NAME=`kill -l $SIGNAL`
            echo "*** JBossAS process ($JBOSS_PID) received $SIGNAL_NAME signal ***" >&2
         fi
      done
      if [ "$WAIT_STATUS" -lt 127 ]; then
         JBOSS_STATUS=$WAIT_STATUS
      else
         JBOSS_STATUS=0
      fi
      if [ "$JBOSS_STATUS" -ne 10 ]; then
            # Wait for a complete shudown
            wait $JBOSS_PID 2>/dev/null
      fi
      if [ "x$JBOSS_PIDFILE" != "x" ]; then
            grep "$JBOSS_PID" $JBOSS_PIDFILE && rm $JBOSS_PIDFILE
      fi 
   fi
   # If restart doesn't work, check you are running JBossAS 4.0.4+
   #    http://jira.jboss.com/jira/browse/JBAS-2483
   # or the following if you're running Red Hat 7.0
   #    http://developer.java.sun.com/developer/bugParade/bugs/4465334.html
   if [ "$JBOSS_STATUS" -eq 10 ]; then
      echo "Restarting JBoss..."
   else
      exit $JBOSS_STATUS
   fi
done

