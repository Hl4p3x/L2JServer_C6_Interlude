@echo off
REM Para configurar a database, apenas mude o caminho do MySQL para o seu.
set PATH=%PATH%;C:\Program Files\MySQL\MySQL Server 5.5\bin

REM Seu usuário do MYSQL
set USER=root
REM Seu password do MYSQL, caso não tenha, deixe em branco.
set PASS=root
REM O nome da sua database criada no seu gerenciador do MySQL (Navicat?)
set DBNAME=l2jdb
REM O ip da database, caso seja local, deixe como está.
set DBHOST=127.0.0.1
REM -----------------------------------
REM NÃO MODIFICAR NADA A PARTIR DAQUI.
REM -----------------------------------
set COMMAND="CREATE DATABASE IF NOT EXISTS "
mysql -h %DBHOST% -u %USER% --password=%PASS% -e "%COMMAND:"=%%DBNAME:"=%;"
for /r install %%f in (*.sql) do ( 
                echo Installing table %%~nf ...
		mysql -h %DBHOST% -u %USER% --password=%PASS% -D %DBNAME% < %%f
	)
:end
pause