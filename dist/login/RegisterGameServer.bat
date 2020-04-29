@echo off
title Register Game Server
java -Djava.util.logging.config.file=console.cfg -cp ./../libs/* org.l2jserver.tools.gsregistering.BaseGameServerRegister -c
pause