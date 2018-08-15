@REM #########################################################
@REM  Name: delete special dietary or files, put this script into you want to executor dietary
@REM  Desciption:
@REM  Author: amosryan
@REM  Date: 2010-11-01
@REM  Version: 1.0
@REM  Copyright: Up to you.
@REM platform: MOS5.0
@REM #########################################################

@echo off
setlocal enabledelayedexpansion

del /s *.project

del /s *.classpath

del /s *.iml

del /s *.rebel.xml.*

del /s build.properties

del target

del %~dp0\\logs

@REM special you want to delete directory
set WHAT_SHOULD_BE_DELETED=.settings .idea logs target bin

for /r . %%a in (!WHAT_SHOULD_BE_DELETED!) do (
  if exist %%a (
  echo "delete "%%a
  rd /s /q "%%a"
 )
)

pause
