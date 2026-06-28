@echo off
set DIRNAME=%~dp0
set APP_HOME=%DIRNAME%
if not exist "%APP_HOME%gradle\wrapper\gradle-wrapper.jar" (
    powershell -Command "Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/org/gradle/gradle-wrapper/8.2/gradle-wrapper-8.2.jar' -OutFile '%APP_HOME%gradle\wrapper\gradle-wrapper.jar'"
)
"%JAVA_HOME%\bin\java" -jar "%APP_HOME%gradle\wrapper\gradle-wrapper.jar" %*
