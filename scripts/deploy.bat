@echo off
REM TodoApp build & deploy: compile Java -> mirror web/ to Tomcat deploy dir
setlocal
set PROJECT=D:\apache-tomcat-8.5.16\webapps\TodoApp
set DEPLOY=D:\apache-tomcat-8.5.16\deploy\TodoApp

echo [1/3] Compiling Java sources...
if not exist "%PROJECT%\web\WEB-INF\classes" mkdir "%PROJECT%\web\WEB-INF\classes"
dir /s /b "%PROJECT%\src\*.java" > "%TEMP%\todoapp-sources.txt"
javac -encoding UTF-8 -cp "%PROJECT%\web\WEB-INF\lib\gson-2.10.1.jar;%PROJECT%\web\WEB-INF\lib\javax.servlet-api-4.0.1.jar;%PROJECT%\web\WEB-INF\lib\mysql-connector-java-8.0.15.jar" -d "%PROJECT%\web\WEB-INF\classes" @"%TEMP%\todoapp-sources.txt"
if errorlevel 1 ( echo COMPILE FAILED & exit /b 1 )

echo [2/3] Syncing to %DEPLOY% ...
if not exist "%DEPLOY%" mkdir "%DEPLOY%"
robocopy "%PROJECT%\web" "%DEPLOY%" /MIR /XD uploads /NFL /NDL /NJH /NJS /NC /NS
if errorlevel 8 ( echo SYNC FAILED & exit /b 1 )

echo [3/3] Done. Restart Tomcat, then open http://localhost:8081/TodoApp/
endlocal
