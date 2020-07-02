## diKTat project
diKTat - is a rule set of Kotlin code style rules that are using [KTlint](https://ktlint.github.io/) framework.
In this project we are trying to define Kotlin code style rules and implement them as visitors for AST tree provided by Ktlint.

Main components are:
1) diktat-ruleset - number of rules that are supported by diKTat
2) diktat-test-framework - functional/unit test framework that can be used for running your code fixer on the initial code and compare it with the expected result
3) customized fork of ktlint project - temporary added until we will publish our ideas to main repository
4) also see our demo: diktat-demo in a separate repository

## What was hacked and changed in KTlint and why do we temporary need a fork of it?
Mainly we wanted to create a common configurable mechanism that will give us a chance to enable/disable and customize all rules.
That's why we added logic for:
1) Parsing .json file with configurations of rules and passing it to visitors
2) Passing information about properties to visitors. This information is very useful, when you are trying to get, for example, a filename of file where the code is stored.

## How to build the project
As Java/Kotlin community has a holywar about usage of Gradle vs Maven - we supported both. So you can:
a) Use Maven:
`mvn clean install`

b) Use Gradle:
`gradle build` 

## Which rules does diKTat supports now and how they can be configured.
|Rule name|Description|
| ---- | ---- |
|PACKAGE_NAME_MISSING|Check: warns if package name is missing in a file. Fix: automatically adds package directive with the name that starts from the domain name (in example - com.huawei) and contains the real directory|
|PACKAGE_NAME_INCORRECT_CASE|Check: warns if package name is in incorrect (non-lower) case. Fix: automatically update the case in package name|
|PACKAGE_NAME_INCORRECT_PREFIX|Check: warns if package name does not start with the company's domain. Fix: automatically update the prefix in the package name|
|PACKAGE_NAME_INCORRECT_SYMBOLS|Check: warns if package name has incorrect symbols like underscore or non-ASCII letters/digits. Exception: underscores that are used for differentiating of keywords in a name. Fix: no but will be|
|PACKAGE_NAME_INCORRECT_PATH|Check: warns if the path for a file does not match with a package name. Fix: replacing incorrect package name with the name constructed from a path to the file.|
|CLASS_NAME_INCORRECT|Check: warns if the Class/Enum/Interface name does not match Pascal case ("([A-Z][a-z0-9]+)+"). Fix: fixing the case: if the it is some fixed case (like snake/camel) - with word saving. If not - will restore PascalCase as is.|
|INCORRECT_PACKAGE_SEPARATOR|Check: warns if underscore is incorrectly used to split package naming. Fix: fixing all nodes in AST and the package name to remove all underscores
|OBJECT_NAME_INCORRECT|Check: warns if the object does not match Pascal case ("([A-Z][a-z0-9]+)+") Fix: fixing the case in the same way as for classes|
|VARIABLE_NAME_INCORRECT_FORMAT|Check: warns if the name of variable is not in lowerCamelCase or contains non-ASCII letters. Fix: fixing the case format to lowerCamelCase|
|VARIABLE_NAME_INCORRECT|Check: warns if variable contains one single letter, only exceptions are fixed names that used in industry like {i, j} Fix: no fix as we are not able to imagine good name by machine|
|CONSTANT_UPPERCASE|Check: warns if CONSTANT (treated as const val from companion object or class level) is in non UPPER_SNAKE_CASE. Fix: name is changed to UPPER_SNAKE_CASE|
|VARIABLE_HAS_PREFIX|Check: warns if variable has prefix (like mVariable or M_VARIABLE), generally it is a bad code style (Android - is the only exception. Fix: no fix as we are not able to imagine good name by machine|
|IDENTIFIER_LENGTH|Check: identifier length should be in range [2,64] except names that used in industry like {i, j} Fix: no fix as we are not able to imagine good name by machine|
|ENUM_VALUE|Check: warns if enum value is in non UPPER_SNAKE_CASE. Fix: automatically converting case to UPPER_SNAKE_CASE|
|GENERIC_NAME|Check: warns if generic name contains more than 1 letter (capital). It can be followed by numbers, example: T12, T. Fix: no but will be|
|FUNCTION_NAME_INCORRECT_CASE|Check: function/method name should be in lowerCamelCase. Fix:|
|FUNCTION_BOOLEAN_PREFIX|Check: functions/methods that return boolean should have special prefix like "is/should/e.t.c" Fix:|
|FILE_NAME_INCORRECT|Check: warns if file name does not have extension .kt/.kts Fix: no|
|FILE_NAME_MATCH_CLASS|Check: warns if in file there is only one class that does not match the file name| Fix: no|
|EXCEPTION_SUFFIX|Check: warns if class that extends any Exception class does not have Exception suffix. Fix: Adding suffix "Exception" to a class name|
|MISSING_KDOC_TOP_LEVEL|Check: warns on file level internal or public class or function has missing KDoc. Fix: no|
|MISSING_KDOC_CLASS_ELEMENTS|Check: warns if accessible internal elements (protected, public, internal) in a class are not documented. Fix: no|
|KDOC_WITHOUT_PARAM_TAG|Check: warns if accessible method has parameters and they are not documented in KDoc. Fix: If accessible method has no KDoc, KDoc template is added|
|KDOC_WITHOUT_RETURN_TAG|Check: warns if accessible method has explicit return type they it is not documented in KDoc. Fix: If accessible method has no KDoc, KDoc template is added|
|KDOC_WITHOUT_THROWS_TAG|Check: warns if accessible method has throw keyword and it is not documented in KDoc. Fix: If accessible method has no KDoc, KDoc template is added|
|BLANK_LINE_AFTER_KDOC|Check: warns if Kdoc is incorrectly separated with newlines. Fix: removing incorrect newlines between Kdoc and it's declaration|
|KDOC_NO_EMPTY_TAG|Check: warns if KDoc tags have empty content|
|KDOC_WRONG_SPACES_AFTER_TAG|Check: warns if there is more than one space after KDoc tag. Fix: removes redundant spaces|
|KDOC_WRONG_TAGS_ORDER|Check: warns if basic KDoc tags are not oredered properly.Fix: reorders them `@param`, `@return`, `@throws`|
|KDOC_NO_NEWLINES_BETWEEN_BASIC_TAGS|Check: if there is newline of empty KDoc line (with leading asterisk) between `@param`, `@return`, `@throws` tags. Fix: removes line|
|KDOC_NO_NEWLINE_AFTER_SPECIAL_TAG|Check: warns if special tags `@apiNote`, `@implNote`, `@implSpec` don't have exactly one empty line after. Fix: removes redundant lines or adds one|
|KDOC_NO_DEPRECATED_TAG|Check: warns if `@deprecated` is used in KDoc. Fix: adds `@Deprecated` annotation with message, removes tag|
|HEADER_WRONG_FORMAT|Checks: warns if there is no newline after header KDoc. Fix: adds newline|
|HEADER_MISSING_OR_WRONG_COPYRIGHT|Checks: copyright exists on top of file and is properly formatted (as a block comment). Fix: adds copyright if it is missing and required|
|HEADER_CONTAINS_DATE_OR_AUTHOR|Checks: header KDoc contains `@author` tag|
|HEADER_MISSING_IN_NON_SINGLE_CLASS_FILE|Check: warns if file with zero or >1 classes doesn't have header KDoc|
|COMMENTED_OUT_CODE|Check: warns if valid kotlin code is detected in commented blocks (both single-line and block comments)|
