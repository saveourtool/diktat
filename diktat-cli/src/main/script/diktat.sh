#!/usr/bin/env bash
#
# vim:ai et sw=4 si sta ts=4:
#
# External variables used:
#
# - JAVA_HOME
# - GITHUB_ACTIONS

# Bash strict mode,
# see http://redsymbol.net/articles/unofficial-bash-strict-mode/.
set -euo pipefail
IFS=$'\n'

function error() {
    local message
    message="$*"

    if [[ "${GITHUB_ACTIONS:=false}" == 'true' ]]
    then
        # Echoing to GitHub.
        echo "::error::${message}"
    elif [[ -t 1 ]]
    then
        # Echoing to a terminal.
        echo -e "\e[1m$(basename "$0"): \e[31merror:\e[0m ${message}" >&2
    else
        # Echoing to a pipe.
        echo "$(basename "$0"): error: ${message}" >&2
    fi
}

# Exit codes.
# The code of 1 is returned by ktlint in the event of failure.
declare -ir ERROR_JAVA_NOT_FOUND=2
declare -ir ERROR_INCOMPATIBLE_BASH_VERSION=3

if (( BASH_VERSINFO[0] < 4 ))
then
    error "bash version ${BASH_VERSION} is too old, version 4+ is required"
    exit ${ERROR_INCOMPATIBLE_BASH_VERSION}
fi

JAVA_ARGS=()
DIKTAT_JAR="$0"

# Locates Java, preferring JAVA_HOME.
#
# The 1st variable expansion prevents the "unbound variable" error if JAVA_HOME
# is unset.
function find_java() {
    if [[ -n "${JAVA_HOME:=}" ]]
    then
        case "$(uname -s)" in
            'MINGW32_NT-'* | 'MINGW64_NT-'* | 'MSYS_NT-'* )
                JAVA_HOME="$(cygpath "${JAVA_HOME}")"
                ;;
        esac

        JAVA="${JAVA_HOME}/bin/java"
        # Update the PATH, just in case
        export PATH="${JAVA_HOME}/bin:${PATH}"
    elif [[ -x "$(which java 2>/dev/null)" ]]
    then
        JAVA="$(which java 2>/dev/null)"
    else
        error 'Java is not found'
        exit ${ERROR_JAVA_NOT_FOUND}
    fi
}

# On Windows, converts a UNIX path to Windows. Should be invoked before a path
# is passed to any of the Windows-native tools (e.g.: `java`).
#
# On UNIX, just returns the 1st argument.
function native_path() {
    case "$(uname -s)" in
        'MINGW32_NT-'* | 'MINGW64_NT-'* | 'MSYS_NT-'* )
            cygpath --windows "$1"
            ;;
        *)
            echo "$1"
            ;;
    esac
}

find_java

JAVA_ARGS+=('-Xmx512m')
JAVA_ARGS+=('-jar' "$(native_path "${DIKTAT_JAR}")")

exec "${JAVA}" "${JAVA_ARGS[@]}" "$@"
