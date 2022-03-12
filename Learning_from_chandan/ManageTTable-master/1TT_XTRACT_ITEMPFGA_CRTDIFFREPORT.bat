@ECHO OFF
:::::::::::::::::::::::::::::::::::::::::::::::::::::: MAIN FLOW START :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
:: Prerequisite is that the difference report of the GenAreas of all T-Table has already been created.
::------------------------------------------------------------------------------------------------------------------------------------------------
CALL TT_INITCONF.bat

:: Convert hostlist into array
setlocal EnableDelayedExpansion
set i=0
for %%D in (%TARGETHOSTLIST%) do (
   set /A i+=1
   set "HOST_ARR[!i!]=%%D"
)

:: Convert sidlist into array
set i=0
for %%D in (%TARGETSIDLIST%) do (
   set /A i+=1
   set "SID_ARR[!i!]=%%D"
)

:: For each host and SID take the diff.
for /L %%A in (1,1,%i%) do (
   SET "FILENAME=%DIFFREPORTDIR%\%MASTERHOST%_%MASTERSID%_VS_!HOST_ARR[%%A]!_!SID_ARR[%%A]!.csv"	
   ECHO !time! Parsing the file !FILENAME! and extract GA of T-Table.
   CALL :GET_TTBL_FROM_DIFFREPORT !FILENAME! !HOST_ARR[%%A]! !SID_ARR[%%A]!
)
endlocal
exit /B %ERRORLEVEL%
::------------------------------------------------------------------------------------------------------------------------------------------------ 

:::::::::::::::::::::::::::::::::::::::::::::::::::::: FUNCTION GET_TTBL_FROM_DIFFREPORT START :::::::::::::::::::::::::::::::::::::::::::::::::::
:: Function to parse the csv file and get the T-Table names. Then for each of the T-Tables it extracts GenArea from MASTER and TARGET.
:: Then it uses winmergeu to compare the extracted GenArea from master and target db and creates the report. 
:: Input 1st parameter: The file to process and get T-Table names out of it.
:: Input 2nd parameter: The hostid of target DB
:: Input 3rd parameter: The sid of target DB
::------------------------------------------------------------------------------------------------------------------------------------------------
:GET_TTBL_FROM_DIFFREPORT
SETLOCAL EnableDelayedExpansion
SET "FILETOPROCESS=%~1"
SET "TARGETHOST=%~2"
SET "TARGETSID=%~3"
SET "PREVLINE="
SET "ISLINEIMP="

:: To ensure we keep the fresh data, delete the previous content and take new
IF EXIST %OUTDIR%\%MASTERSID%_%TARGETSID% (
   rmdir /S /Q %OUTDIR%\%MASTERSID%_%TARGETSID%
)
mkdir %OUTDIR%\%MASTERSID%_%TARGETSID%\%MASTERSID%
mkdir %OUTDIR%\%MASTERSID%_%TARGETSID%\%TARGETSID%

:: This loop is finding the unique T-Table names from the diff report file.
FOR /f "tokens=3 delims=, " %%A in ('FINDSTR "\<T.*" %FILETOPROCESS%') do (
   IF [%%~A] NEQ [!PREVLINE!] ( 
       SET "PREVLINE=%%~A"
	   CALL :EXTRACT_SAVE_GA %TARGETHOST% %TARGETSID% !PREVLINE!
   )   
)

ECHO !time! Comparing the %MASTERSID%_%TARGETSID% for GenArea diff. 
:: -cfg Settings/DiffContextV2=0 : This ensures only difference is shown. Also in the WinMerge GUI Edit>Options-->Editor select word level difference.	
%WINMRGPATH%\winmergeu %OUTDIR%\%MASTERSID%_%TARGETSID%\%MASTERSID% %OUTDIR%\%MASTERSID%_%TARGETSID%\%TARGETSID% ^
 -minimize -noninteractive -noprefs -cfg Settings/DirViewExpandSubdirs=1 -cfg ReportFiles/ReportType=2 -cfg ReportFiles/IncludeFileCmpReport=1 ^
 -cfg Settings/DiffContextV2=0 -r -u -or %DIFFREPORTDIR%\%MASTERSID%_%TARGETSID%_Diff.html
 
ECHO !time! Comparing done. Report is saved at %DIFFREPORTDIR%\%MASTERSID%_%TARGETSID%_Diff.html

ENDLOCAL
EXIT /B 0
::------------------------------------------------------------------------------------------------------------------------------------------------ 

:::::::::::::::::::::::::::::::::::::::::::::::::::::: FUNCTION EXTRACT_SAVE_GA START ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
:: Function to extract and save the GenArea of T-Table from MASTER DB and TARGET DB. This save two files for each T-Table in defined folders.
:: Input 1st parameter: The T-Table
:: Input 2nd parameter: The hostid of DB
:: Input 3rd parameter: The sid of DB
::------------------------------------------------------------------------------------------------------------------------------------------------ 
:EXTRACT_SAVE_GA
SETLOCAL EnableDelayedExpansion
SET "TARGETHOST=%~1"
SET "TARGETSID=%~2"
SET "TTBL=%~3"
SET "SQL_SCRIPT=%ROOTDIR%\SQL\ITEMPF_GA_VALUES.sql"
ECHO !time! Extracting GA %MASTERSID%_%TARGETSID% %MASTERSID% %TTBL%.csv
%SQL_ROOT%\bin\sql -silent userid/youpwd@%MASTERHOST%:1521:%MASTERSID% @%SQL_SCRIPT% %TTBL% > %OUTDIR%\%MASTERSID%_%TARGETSID%\%MASTERSID%\%TTBL%.csv
ECHO !time! Extracting GA %MASTERSID%_%TARGETSID% %TARGETSID% %TTBL%.csv
%SQL_ROOT%\bin\sql -silent userid/youpwd@%TARGETHOST%:1521:%TARGETSID% @%SQL_SCRIPT% %TTBL% > %OUTDIR%\%MASTERSID%_%TARGETSID%\%TARGETSID%\%TTBL%.csv

ENDLOCAL
EXIT /B 0
::------------------------------------------------------------------------------------------------------------------------------------------------  

