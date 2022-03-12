@ECHO OFF

SET SQL_ROOT=H:\ckp\tools\sqlcl

SET ROOTDIR=H:\ckp\ServerData\InspectTTable
SET OUTDIR=%ROOTDIR%\EXTRACTS
SET DIFFREPORTDIR=%ROOTDIR%\DIFFREPORTS
SET WINMRGPATH=H:\ckp\tools\WinMerge

SET MASTERHOST=jpaigdbs02
SET MASTERSID=igptx22

::igprd31 = PRD31 PT ::igsit03 = UAT ::igsit01 = SIT Automation ::igptx31 = SIT/UAT/Manual ::igptx21 = SHI_PTX21 igft02: FT-ITR2? 
::SET TARGETHOSTLIST= jpaigdbp03 jpaigdbu01 jpaigdbu01 jpaigdbs03 jpaigdbs02
::SET TARGETSIDLIST= igprd31 igsit03 igsit01 igptx31 igptx21

SET TARGETHOSTLIST= jpaigdbu01 jpaigdbd01
SET TARGETSIDLIST= igsit03 igft02 