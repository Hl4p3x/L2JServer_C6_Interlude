@echo off
COLOR 0b
:start
title AuthD Console
java -Dfile.encoding=UTF-8 -Xmx256m -Dorg.slf4j.simpleLogger.log.com.zaxxer.hikari=error -cp ../libs/LoginServer.jar org.l2jserver.loginserver.LoginServer
if ERRORLEVEL 2 goto restart
if ERRORLEVEL 1 goto error
goto end
:restart
echo.
echo Admin Restarted Login Server.
echo.
goto start
:error
echo.
echo Login Server terminated abnormally!
echo.
:end
echo.
echo Login Server Terminated.
echo.
pause