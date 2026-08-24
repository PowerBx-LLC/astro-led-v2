#!/bin/sh

#
# Copyright 2015 the original author or authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

##############################################################################
##
##  Gradle start up script for UN*X
##
##############################################################################

# Attempt to set APP_HOME
# Resolve links: $0 may be a symlink
PRG="$0"
# Need this for relative symlinks.
while [ -h "$PRG" ] ; do
    ls -ld "$PRG"
    link=$(expr "$PRG" : '.*-> \(.*\)$')
    if expr "$link" : '/.*' > /dev/null; then
        PRG="$link"
    else
        PRG=$(dirname "$PRG")"/$link"
    fi
done
SAVED="$(cd "$(dirname "$PRG")" >/dev/null 2>&1 && pwd)"
cd "$SAVED" >/dev/null 2>&1 || exit

APP_HOME=$SAVED
APP_NAME="Gradle"
APP_BASE_NAME=$(basename "$0")

# Add default JVM options here.
DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'

# Use the maximum available, or set MAX_FD != maximum.
MAX_FD=maximum

warn () {
    echo "$*" >&2
}

die () {
    echo
    echo "$*"
    echo
    exit 1
}

cygwin=false
msys=false
nonstop=false
case "$(uname)" in
  CYGWIN* )
    cygwin=true
    ;;
  Darwin* )
    darwin=true
    ;;
  MSYS* | MINGW* )
    msys=true
    ;;
  NONSTOP* )
    nonstop=true
    ;;
esac

CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

if [ -z "$JAVA_HOME" ] ; then
    JAVA_HOME=$(/usr/libexec/java_home)
fi

if [ -z "$JAVA_HOME" ] ; then
    die "ERROR: JAVA_HOME is not set"
fi

if ! command -v java &> /dev/null; then
    die "ERROR: JAVA_HOME is set but java command not found"
fi

if [ "$(uname)" = "Darwin" ] && [ -z "$JAVA_HOME" ]; then
    JAVA_HOME=$(/usr/libexec/java_home)
fi

# Increase the maximum file descriptors if we can.
if [ "$cygwin" = "false" ] && [ "$msys" = "false" ] && [ -n "$MAX_FD" ] && [ "$MAX_FD" != "unlimited" ] ; then
    MAX_FD_LIMIT=$(ulimit -H -n)
    if [ $MAX_FD_LIMIT != 'unlimited' ]; then
        [ $MAX_FD -le $MAX_FD_LIMIT ] && MAX_FD=$MAX_FD_LIMIT
    fi
    ulimit -n $MAX_FD
fi

# For Darwin, add options to specify how the application appears in the dock
if $darwin; then
    DEFAULT_JVM_OPTS="$DEFAULT_JVM_OPTS \"-Xdock:name=$APP_NAME\" \"-Xdock:icon=$APP_HOME/media/gradle.icns\""
fi

for i do
    if [ "$i" = "-classpath" ] || [ "$i" = "-cp" ]; then
        user_classpath="${2:-}"
        i=""
    elif [ "$i" != "" ] ; then
        args="$args \"$i\""
    fi
done

if [ ! -d "$APP_HOME" ] ; then
    die "APP_HOME is not set to a valid directory: $APP_HOME"
fi

exec "$JAVA_HOME"/bin/java $DEFAULT_JVM_OPTS -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
