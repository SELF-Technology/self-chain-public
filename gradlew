#!/usr/bin/env sh

##############################################################################
#
#  Gradle start up script for UNIX
#
##############################################################################

# Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
DEFAULT_JVM_OPTS=""

APP_NAME="Gradle"
APP_BASE_NAME=`basename "$0"`

# Use the maximum available, or set MAX_FD != -1 to use that value.
MAX_FD=maximum

warn ( ) {
    echo "$*"
}

die ( ) {
    echo
    echo "$*"
    echo
    exit 1
}

# OS specific support (must be 'true' or 'false').
cygwin=false
msys=false
darwin=false
case "`uname`" in
  CYGWIN* )
    cygwin=true
    ;;
  Darwin* )
    darwin=true
    ;;
  MSYS* )
    msys=true
    ;;
esac

classpath=$(cat "gradle/wrapper/gradle-wrapper.jar")

# For Cygwin or MSYS, when running in Windows mode the path will start
# with a drive letter followed by a colon. This will break a lot of
# tools, so we need to remove it. This is essential: if this is run
# from the command line and the path is not fixed, there will be
# no error message at ALL. In Cygwin, since the default is to mount
# all drive letters as Cygwin paths, the first盘符 will be a slash,
# so this sequence won't be necessary.
containsDriveLetterPathPrefix () {
    case $1 in
        *:/*) return 0 ;;
        *)    return 1 ;;
    esac
}

fixDriveLetterPathPrefix () {
    echo $1 | sed 's/\([a-zA-Z]\):\(.*\)/\1\2/'
}

# Determine the Java command to use to start the JVM.
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
        # IBM's JDK on AIX uses strange locations for the executables
        JAVACMD=$JAVA_HOME/jre/sh/java
    else
        JAVACMD=$JAVA_HOME/bin/java
    fi
    if [ ! -x "$JAVACMD" ] ; then
        die "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
    fi
else
    JAVACMD=java
    which java >/dev/null 2>&1 || die "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
fi

# Increase the maximum file descriptors if we can.
if [ "$cygwin" = "false" -a "$darwin" = "false" ] ; then
    case $MAX_FD in
      maximum)
        # In Solaris 8 and later, use /etc/project for the soft limit
        if [ "$OS_NAME" = "SunOS" ] ; then
          if [ -x /usr/bin/psrinfo ] ; then
            for proc in `psrinfo -p`; do
              numproc=`expr $numproc + 1`
            done
            # Add RAM as well to the heap size (32MB max)
            physmem=`/usr/sbin/prtconf | awk '/Memory/{print $3}'`
            if [ "${physmem}32" -gt "3232" ]; then
              physmem=32
            fi
            totalmem=`expr $numproc + $physmem`
            MAX_FD=`expr $totalmem \* 1024`
          fi
        else
          MAX_FD=65535
        fi
        ;;
      *)
        ;;
    esac
fi

if [ "$cygwin" = "true" ]
then
    [ -n "$JAVA_HOME" ] && JAVA_HOME=`cygpath --unix "$JAVA_HOME"`
    [ -n "$CLASSPATH" ] && CLASSPATH=`cygpath --path --unix "$CLASSPATH"`
fi

# For Cygwin, switch paths to Windows format before running java
if [ "$cygwin" = "true" ]
then
    APP_HOME=`cygpath --path --mixed "$APP_HOME"`
    APP_BASE=`cygpath --path --mixed "$APP_BASE"`

    # We build the pattern for arguments to be converted via cygpath
    ROOTDIRSRAW=`find -L / -maxdepth 1 -mindepth 1 -type d 2>/dev/null`
    SEP=""
    for dir in $ROOTDIRSRAW ; do
        ROOTDIRS="${ROOTDIRS}${SEP}/^$dir"
        SEP="|"
    done
    ROOTDIRS="${ROOTDIRS}"

    # Collect all .jar files as $JAVACMD arguments
    for file in `ls "$APP_HOME"/gradle/wrapper/*.jar"
    do
        file=`cygpath --path --ignore --mixed "$file"`
        cygpath -m "$file"
        echo ""
    done

    # Add all .jar files to classpath
    for file in `ls "$APP_HOME"/gradle/wrapper/*.jar"
    do
        file=`cygpath --path --ignore --mixed "$file"`
        if [ -n "$CLASSPATH" ] ; then
            CLASSPATH="$CLASSPATH;"
        fi
        CLASSPATH="$CLASSPATH`cygpath --path --ignore --mixed "$file"`"
    done
fi

# Split application arguments.
# The real $APP_ARGS will be set in a minute.
SPLIT "$*"

# Collect all .jar files as $JAVACMD arguments.
for file in `ls "$APP_HOME"/gradle/wrapper/*.jar"
  do
    file=`cygpath --path --ignore --mixed "$file"`
    cygpath -m "$file"
    echo ""
  done

# Add all .jar files to classpath.
for file in `ls "$APP_HOME"/gradle/wrapper/*.jar"
  do
    file=`cygpath --path --ignore --mixed "$file"`
    if [ -n "$CLASSPATH" ] ; then
      CLASSPATH="$CLASSPATH;"
    fi
    CLASSPATH="$CLASSPATH`cygpath --path --ignore --mixed "$file"`"
  done

# Prepend default JVM options to user-supplied options.
DEFAULT_JVM_OPTS=$(echo $DEFAULT_JVM_OPTS | sed 's/ /" "/g')
JAVA_OPTS=$(echo $JAVA_OPTS | sed 's/ /" "/g')
GRADLE_OPTS=$(echo $GRADLE_OPTS | sed 's/ /" "/g')

# Collect the command line arguments.
while [ $# -gt 0 ]; do
  case $1 in
    -D*)
      JAVA_OPTS="$JAVA_OPTS $1"
      shift
      ;;
    -J*)
      JAVA_OPTS="${JAVA_OPTS} ${1#-J}"
      shift
      ;;
    -agentlib:*)
      JAVA_OPTS="$JAVA_OPTS $1"
      shift
      ;;
    -javaagent:*)
      JAVA_OPTS="$JAVA_OPTS $1"
      shift
      ;;
    *)
      APP_ARGS="$APP_ARGS $1"
      shift
      ;;
  esac
done

# We must use exec to keep the process id.
exec "$JAVACMD" $DEFAULT_JVM_OPTS $JAVA_OPTS $GRADLE_OPTS -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$APP_ARGS"
