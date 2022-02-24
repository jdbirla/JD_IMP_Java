@ECHO OFF

CALL TT_INITCONF.bat

SET SQL_SCRIPT=%ROOTDIR%\SQL\ITEMPF_STATS.sql
SET DBSRVHOSTNM=%1
SET DBSID=%2
SET SAVEFILENM=ITEMPF_STATS.csv

:: The sql command to extract the statistical data and save in out file.
%SQL_ROOT%\bin\sql -silent userid/youpwd@%DBSRVHOSTNM%:1521:%DBSID% @%SQL_SCRIPT% > %OUTDIR%\%DBSRVHOSTNM%\%DBSID%\%SAVEFILENM%
