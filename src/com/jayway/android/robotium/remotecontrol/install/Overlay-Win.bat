
REM ***************************************************************************
REM This script is used to launch RobotiumRC installs on 64-bit Windows Systems.
REM It will attempt to detect the 32-bit versions of WSH and invoke that
REM instead of the default 64-bit version.
REM 
REM This script must exist in the same directory as associated WSH\VBS scripts
REM it will try to execute.
REM ***************************************************************************

IF EXIST %WINDIR%\SysWOW64\cscript.exe GOTO FORCE32
%WINDIR%\System32\cscript.exe Overlay-Win.WSF %1 %2 %3 %4 %5 %6
GOTO END

:FORCE32
%WINDIR%\SysWOW64\cscript.exe Overlay-Win.WSF %1 %2 %3 %4 %5 %6
:END