// ;warn:1:1: [FILE_NAME_MATCH_CLASS] file name is incorrect - it should match with the class described in it if there is the only one class declared: DefaultPackageExpected.kt vs Example{{.*}}
// ;warn:3:1: [FILE_INCORRECT_BLOCKS_ORDER] general structure of kotlin source file is wrong, parts are in incorrect order: @file:Suppress{{.*}}
@file:Suppress(
    "PACKAGE_NAME_MISSING",
    "PACKAGE_NAME_INCORRECT_PATH",
    "PACKAGE_NAME_INCORRECT_PREFIX"
)

/**
 * Dolor sit amet
 */
class Example
