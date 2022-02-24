@ECHO OFF

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

:: To ensure we keep the fresh data delete the previous content and take new
rmdir /S /Q %OUTDIR%\%MASTERHOST%\%MASTERSID%
mkdir %OUTDIR%\%MASTERHOST%\%MASTERSID%

:: Refresh the report directory to remove old data.
rmdir /S /Q %DIFFREPORTDIR%
mkdir %DIFFREPORTDIR%

:: For MASTER Extract the hash code of GenArea of all items in the ITEMPF.
ECHO !time! MASTER:Extract the hashGenArea and stats  %MASTERHOST% sid=%MASTERSID% 
CALL TT_XTRACT_ITEMPF_GA_HASH.bat %MASTERHOST% %MASTERSID% 
CALL TT_XTRACT_ITEMPF_STATS.bat %MASTERHOST% %MASTERSID%

:: For each TARGET host and SID take the extracts.
for /L %%A in (1,1,%i%) do (
   ECHO !time! TARGET:Refresh dir and extract hashGenArea and stats !HOST_ARR[%%A]!\!SID_ARR[%%A]!
   rmdir /S /Q %OUTDIR%\!HOST_ARR[%%A]!\!SID_ARR[%%A]!
   mkdir %OUTDIR%\!HOST_ARR[%%A]!\!SID_ARR[%%A]!
   
   CALL TT_XTRACT_ITEMPF_GA_HASH.bat !HOST_ARR[%%A]! !SID_ARR[%%A]! 
   
   CALL TT_XTRACT_ITEMPF_STATS.bat !HOST_ARR[%%A]! !SID_ARR[%%A]! 
   
   ECHO !time! Taking the diff report for host=!HOST_ARR[%%A]! sid=!SID_ARR[%%A]! 
   FC %OUTDIR%\%MASTERHOST%\%MASTERSID%\* %OUTDIR%\!HOST_ARR[%%A]!\!SID_ARR[%%A]!\* > %DIFFREPORTDIR%\%MASTERHOST%_%MASTERSID%_VS_!HOST_ARR[%%A]!_!SID_ARR[%%A]!.csv 	

)

ECHO !time! Extract and taking the differences in T-Table GA completed!


