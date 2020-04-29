@echo off
COLOR 0b
title L2Server Console
:start
java -Dfile.encoding=UTF-8 -Djava.util.logging.manager=org.l2jserver.log.ServerLogManager -Dorg.slf4j.simpleLogger.log.com.zaxxer.hikari=error -Xmx1g -Xms512m -cp ../libs/GameServer.jar org.l2jserver.gameserver.GameServer

REM NOTE: If you have a powerful machine, you could modify/add some extra parameters for performance, like:
REM -Xms1536m
REM -Xmx3072m
REM -XX:+AggressiveOpts
REM Use this parameters carefully, some of them could cause abnormal behavior, deadlocks, etc.
REM More info here: http://www.oracle.com/technetwork/java/javase/tech/vmoptions-jsp-140102.html

if ERRORLEVEL 2 goto restart
if ERRORLEVEL 1 goto error
goto end

:restart
echo.
echo Admin Restarted Game Server.
echo.
goto start

:error
echo.
echo Game Server Terminated Abnormally!
echo.

:end
echo.
echo Game Server Terminated.
echo.
pause