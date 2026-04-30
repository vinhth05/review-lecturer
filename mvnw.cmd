@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License.  You may obtain a copy of the License at
@REM
@REM    http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied.  See the License for the
@REM specific language governing permissions and limitations
@REM under the License.

@echo off
setlocal enabledelayedexpansion

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%

@REM Resolve any "." and ".." in APP_HOME to make it shorter.
for %%i in ("%APP_HOME%") do set APP_HOME=%%~fi

if not exist "%APP_HOME%\.mvn\wrapper\maven-wrapper.jar" (
    echo Downloading maven-wrapper.jar...
    powershell -Command "& {Invoke-WebRequest -Uri 'https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar' -OutFile '%APP_HOME%\.mvn\wrapper\maven-wrapper.jar'}"
)

if not exist "%APP_HOME%\.mvn\wrapper\maven-wrapper.jar" (
    echo Error: Could not download maven-wrapper.jar
    exit /b 1
)

setlocal enabledelayedexpansion

if exist "%JAVA_HOME%\bin\java.exe" (
    set JAVA_EXE=%JAVA_HOME%\bin\java.exe
) else (
    for /f "tokens=*" %%A in ('where java 2^>nul') do (
        set JAVA_EXE=%%A
        goto :found_java
    )
    echo Error: JAVA_HOME is not set and java could not be found in PATH
    exit /b 1
    :found_java
)

set MAVEN_PROJECTBASEDIR=%APP_HOME%
set CLASSWORLDS_JAR=%APP_HOME%\.mvn\wrapper\maven-wrapper.jar
set WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain

"%JAVA_EXE%" ^
  -classpath "%CLASSWORLDS_JAR%" ^
  "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" ^
  %WRAPPER_LAUNCHER% %*
