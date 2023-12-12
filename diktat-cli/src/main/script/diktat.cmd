@echo off

rem
rem diKTat command-line client for Windows
rem
rem Uses Git Bash, so requires Git to be installed.
rem

set "git_install_location=%ProgramFiles%\Git"
set "git_url=https://github.com/git-for-windows/git/releases/latest"

if exist "%git_install_location%" (
	setlocal
	set "PATH=%git_install_location%\usr\bin;%PATH%"
	for /f "usebackq tokens=*" %%p in (`cygpath "%~dpn0"`) do bash --noprofile --norc %%p %*
) else (
	echo Expecting Git for Windows at %git_install_location%; please install it from %git_url%
	start %git_url%
)
