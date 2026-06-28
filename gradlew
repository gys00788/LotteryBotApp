#!/bin/sh
cd "$(dirname "$0")"
JAR=gradle/wrapper/gradle-wrapper.jar
if [ ! -f "$JAR" ]; then
  curl -fsSL --connect-timeout 30 --max-time 60 "https://repo1.maven.org/maven2/org/gradle/gradle-wrapper/8.2/gradle-wrapper-8.2.jar" -o "$JAR" 2>/dev/null
fi
if [ -f "$JAR" ]; then
  java -jar "$JAR" "$@"
else
  echo "ERROR: cannot download gradle-wrapper.jar"
  exit 1
fi
