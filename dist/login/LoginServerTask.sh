#!/bin/bash

err=1
until [ $err == 0 ]; 
do
	[ -f log/java.log.0 ] && mv log/java.log.0 "log/`date +[%Y-%m-%d]_[%H:%M:%S]`_java.log"
    [ -f console.log ] &&  mv console.log "log/`date +[%Y-%m-%d]_[%H:%M:%S]`_console.log"
		
	java -Djava.awt.headless=true -server -Dorg.slf4j.simpleLogger.log.com.zaxxer.hikari=error -Xms128m -Xmx512m -jar ../libs/LoginServer.jar > console.log 2>&1
	err=$?
	sleep 10;
done
