# _diktat-cli_, the command-line client for [_diktat_](https://github.com/saveourtool/diktat)

---

# Table of contents
1. [Features](#features)
2. [Usage](#usage)
3. [Option summary](#option-summary)
4. [Exit code](#exit-codes)

---

## Features

* Self-executable JAR in _UNIX Shell_ (requires installed _JAVA_)
* BSD-compatible
* Also works in Windows (_Git Bash_, _Cygwin_, or _MSys2_) via the dedicated _diktat.cmd_
* Can be used as a regular uber JAR

## Usage

```shell
diktat [OPTION]... [FILE]...
```

## Option summary

| Command-line switch                  | Meaning                                                                                                                                                                                                                |
|:-------------------------------------|:-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `-c CONFIG`, `--config=CONFIG`       | Specify the location of the YAML configuration file. By default, `diktat-analysis.yml` in the current directory is used.                                                                                               |
| `-m MODE`, `--mode MODE`             | Mode of `diktat` controls that `diktat` fixes or only finds any deviations from the code style.                                                                                                                        |
| `-r REPORTER`, `--reporter=REPORTER` | The reporter to use to errors to `output`, one of: `plain`, `plain_group_by_file`, `json`, `sarif`, `checkstyle`, `html`.                                                                                              |
| `-o OUTPUT`, `--output=OUTPUT`       | Redirect the reporter output to a file. Must be provided when the reporter is provided.                                                                                                                                |
| `--group-by-file`                    | A flag to group found errors by files.                                                                                                                                                                                 |
| `--color COLOR`                      | Colorize the output, one of: `BLACK`, `RED`, `GREEN`, `YELLOW`, `BLUE`, `MAGENTA`, `CYAN`, `LIGHT_GRAY`, `DARK_GRAY`, `LIGHT_RED`, `LIGHT_GREEN`, `LIGHT_YELLOW`, `LIGHT_BLUE`, `LIGHT_MAGENTA`, `LIGHT_CYAN`, `WHITE` |
| `-l`, `--log-level`                  | Control the log level.                                                                                                                                                                                                 |
| `-h`, `--help`                       | Display the help text and exit.                                                                                                                                                                                        |
| `-l`, `--license`                    | Display the license and exit.                                                                                                                                                                                          |
| `-v`, `--verbose`                    | Enable the verbose output.                                                                                                                                                                                             |
| `-V`, `--version`                    | Output version information and exit.                                                                                                                                                                                   |

## Exit codes

| Exit code | Meaning                                                                                                             |
|:----------|:--------------------------------------------------------------------------------------------------------------------|
| 0         | _diKTat_ found no errors in your code                                                                               |
| 1         | _diKTat_ reported some errors in your code                                                                          |
| 2         | The JVM was not found (probably, you need to set up the JVM explicitly, using the `JAVA_HOME` environment variable) |
| 3         | Incompatible _Bash_ version                                                                                         |
