#!/bin/bash

err=1
until [ $err == 0 ];
do
		[ -f log/error.log.0 ] && mv log/error.log.0 "log/`date +[%Y-%m-%d]_[%H:%M:%S]`_error.log"
        [ -f log/java.log.0 ] && mv log/java.log.0 "log/`date +[%Y-%m-%d]_[%H:%M:%S]`_java.log"
        [ -f log/chat.log ] && mv log/chat.log "log/`date +[%Y-%m-%d]_[%H:%M:%S]`-chat.log"
		[ -f log/item.log ] && mv log/item.log "log/`date +[%Y-%m-%d]_[%H:%M:%S]`-item.log"
		[ -f console.log ] && mv console.log "log/`date +[%Y-%m-%d]_[%H:%M:%S]`_console.log"
		
	java -Djava.awt.headless=true -Dfile.encoding=UTF-8 -Djava.util.logging.manager=org.l2jmobius.log.ServerLogManager -Dorg.slf4j.simpleLogger.log.com.zaxxer.hikari=error -Xmx8g -Xms1g -XX:PermSize=64m -jar ../libs/GameServer.jar > console.log 2>&1
	   err=$?
        sleep 10
done
