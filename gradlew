#!/bin/sh
DIRNAME=$(dirname "$0")
APP_HOME=$DIRNAME
if [ ! -f "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" ]; then
    curl -sL "https://repo1.maven.org/maven2/org/gradle/gradle-wrapper/8.2/gradle-wrapper-8.2.jar" -o "$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
fi
java -jar "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" "$@"
