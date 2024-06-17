<img src="logo.svg" width="64px"/>

## Kotlin Coding Style Guide (Diktat Code Style), v.1.0.0

I [Preface](#c0)
* [I.I Purpose of this document](#c0.1)
* [I.II General principles](#c0.2)
* [I.III Terminology](#c0.3)
* [I.IV Exceptions](#c0.4)

[1. Naming](#c1)

* [1.1 Identifiers](#c1.1)
* [1.2 Packages](#c1.2)
* [1.3 Classes, enumerations, interfaces](#c1.3)
* [1.4 Functions](#c1.4)
* [1.5 Constants](#c1.5)
* [1.6 Non-constant fields (variables)](#c1.6)
    * [1.6.1 Non-constant field name](#r1.6.1)
    * [1.6.2 Boolean variable names with negative meaning](#r1.6.2)

[2. Comments](#c2)
* [2.1 General form of Kdoc](#c2.1)
    * [2.1.1 Using KDoc for the public, protected, or internal code elements](#r2.1.1)
    * [2.1.2 Describing methods that have arguments, a return value, or can throw an exception in the KDoc block](#r2.1.2)
    * [2.1.3 Only one space between the Kdoc tag and content. Tags are arranged in the order.](#r2.1.3)
* [2.2 Adding comments on the file header](#c2.2)
* [2.3 Comments on the function header](#c2.3)
* [2.4 Code comments](#c2.4)
    * [2.4.1 Add a blank line between the body of the comment and Kdoc tag-blocks](#r2.4.1)
    * [2.4.2 Do not comment on unused code blocks](#r2.4.2)
    * [2.4.3 Code delivered to the client should not contain TODO/FIXME comments](#r2.4.3)

[3. General formatting (typesetting)](#c3)
* [3.1 File-related rules](#c3.1)
    * [3.1.1 Avoid files that are too long](#r3.1.1)
    * [3.1.2 Code blocks in the source file should be separated by one blank line](#r3.1.2)
    * [3.1.3 Import statements order](#r3.1.3)
    * [3.1.4 Order of declaration parts of class-like code structures](#r3.1.4)
    * [3.1.5 Order of declaration of top-level code structures](#r3.1.5)
* [3.2 Braces](#c3.2)
    * [3.2.1 Using braces in conditional statements and loop blocks](#r3.2.1)
    * [3.2.2 Opening braces are placed at the end of the line in *non-empty* blocks and block structures](#r3.2.2)
* [3.3 Indentation](#c3.3)
* [3.4 Empty blocks](#c3.4)
* [3.5 Line length](#c3.5)
* [3.6 Line breaks (newlines)](#c3.6)
    * [3.6.1 Each line can have a maximum of one statement](#r3.6.1)
    * [3.6.2 Rules for line-breaking](#r3.6.2)
* [3.7 Using blank lines](#c3.7)
* [3.8 Horizontal space](#c3.8)
    * [3.8.1 Usage of whitespace for code separation](#r3.8.1)
    * [3.8.2 No spaces for horizontal alignment](#r3.8.2)
* [3.9 Enumerations](#c3.9)
* [3.10 Variable declaration](#c3.10)
    * [3.10.1 Declare one variable per line](#r3.10.1)
    * [3.10.2 Variables should be declared near the line where they are first used](#r3.10.2)
* [3.11 'When' expression](#c3.11)
* [3.12 Annotations](#c3.12)
* [3.13 Block comments](#c3.13)
* [3.14 Modifiers and constant values](#c3.14)
    * [3.14.1 Declaration with multiple modifiers](#r3.14.1)
    * [3.14.2 Separate long numerical values with an underscore](#r3.14.2)
 * [3.15 Strings](#c3.15)
     * [3.15.1 Concatenation of Strings](#r3.15.1)
     * [3.15.2 String template format](#r3.15.2)
 * [3.16 Conditional statements](#c3.16)
     * [3.16.1 Collapsing redundant nested if-statements](#r3.16.1)
     * [3.16.2 Too complex conditions](#r3.16.2)

[4. Variables and types](#c4)
* [4.1 Variables](#c4.1)
    * [4.1.1 Do not use Float and Double types when accurate calculations are needed](#r4.1.1)
    * [4.1.2 Comparing numeric float type values](#r4.1.2)
    * [4.1.3 Try to use 'val' instead of 'var' for variable declaration [SAY_NO_TO_VAR]](#r4.1.3)
* [4.2 Types](#c4.2)
    * [4.2.1 Use Contracts and smart cast as much as possible](#r4.2.1)
    * [4.2.2 Try to use type alias to represent types making code more readable](#r4.2.2)
* [4.3 Null safety and variable declarations](#c4.3)
    * [4.3.1 Avoid declaring variables with nullable types, especially from Kotlin stdlib](#r4.3.1)
    * [4.3.2 Variables of generic types should have an explicit type declaration](#r4.3.2)
    * [4.3.3 Null-safety](#r4.3.3)

[5. Functions](#c5)
* [5.1 Function design](#c5.1)
    * [5.1.1 Avoid functions that are too long ](#r5.1.1)
    * [5.1.2 Avoid deep nesting of function code blocks, limiting to four levels](#r5.1.2)
    * [5.1.3 Avoid using nested functions](#r5.1.3)
    * [5.1.4 Negated function calls](#r5.1.4)
* [5.2 Function arguments](#c5.2)
    * [5.2.1 The lambda parameter of the function should be placed at the end of the argument list](#r5.2.1)
    * [5.2.2 Number of function parameters should be limited to five](#r5.2.2)
    * [5.2.3 Use default values for function arguments instead of overloading them](#r5.2.3)
    * [5.2.4 Synchronizing code inside asynchronous code](#r5.2.4)
    * [5.2.5 Long lambdas should have explicit parameters](#r5.2.5)
    * [5.2.6 Avoid using unnecessary, custom label](#r5.2.6)

[6. Classes, interfaces, and extension functions](#c6)
* [6.1 Classes](#c6.1)
    * [6.1.1 Denoting a class with a single constructor](#r6.1.1)
    * [6.1.2 Prefer data classes instead of classes without any functional logic](#r6.1.2)
    * [6.1.3 Do not use the primary constructor if it is empty or useless](#r6.1.3)
    * [6.1.4 Do not use redundant init blocks in your class](#r6.1.4)
    * [6.1.5 Explicit supertype qualification](#r6.1.5)
    * [6.1.6 Abstract class should have at least one abstract method](#r6.1.6)
    * [6.1.7 When using the "implicit backing property" scheme, the name of real and back property should be the same](#r6.1.7)
    * [6.1.8 Avoid using custom getters and setters](#r6.1.8)
    * [6.1.9 Never use the name of a variable in the custom getter or setter (possible_bug)](#r6.1.9)
    * [6.1.10 No trivial getters and setters are allowed in the code](#r6.1.10)
    * [6.1.11 Use 'apply' for grouping object initialization](#r6.1.11)
    * [6.1.12 Prefer Inline classes when a class has a single property](#r6.1.12)
* [6.2 Extension functions](#c6.2)
    * [6.2.1 Use extension functions for making logic of classes less coupled](#r6.2.1)
    * [6.2.2 No extension functions with the same name and signature if they extend base and inheritor classes (possible_bug)](#r6.2.2)
    * [6.2.3 Don't use extension functions for the class in the same file](#r6.2.3)
* [6.3 Interfaces](#c6.3)
* [6.4 Objects](#c6.4)
    * [6.4.1 Instead of using utility classes/objects, use extensions](#r6.4.1)
    * [6.4.2 Objects should be used for Stateless Interfaces](#r6.4.2)
* [6.5 Kts Files](#c6.5)
    * [6.5.1 kts files should wrap logic into top-level scope](#r6.5.1)

## <a name="c0"></a> Preface
 <!-- =============================================================================== -->
### <a name="c0.1"></a> Purpose of this document

The purpose of this document is to provide a specification that software developers could reference to enhance their ability to write consistent, easy-to-read, and high-quality code.
Such a specification will ultimately improve software development efficiency and product competitiveness.
For the code to be considered high-quality, it must entail the following characteristics:
1.	Simplicity
2.	Maintainability
3.	Reliability
4.	Testability
5.	Efficiency
6.	Portability
7.	Reusability


<!-- =============================================================================== -->
### <a name="c0.2"></a> General principles

Like other modern programming languages, Kotlin is an advanced programming language that complies with the following general principles:
1.	Clarity — a necessary feature of programs that are easy to maintain and refactor.
2.	Simplicity — a code is easy to understand and implement.
3.	Consistency — enables a code to be easily modified, reviewed, and understood by the team members. Unification is particularly important when the same team works on the same project, utilizing similar styles enabling a code to be easily modified, reviewed, and understood by the team members.

Also, we need to consider the following factors when programming on Kotlin:

1. Writing clean and simple Kotlin code

    Kotlin combines two of the main programming paradigms: functional and object-oriented.
    Both of these paradigms are trusted and well-known software engineering practices.
    As a young programming language, Kotlin is built on top of well-established languages such as Java, C++, C#, and Scala.
    This enables Kotlin to introduce many features that help a developer write cleaner, more readable code while also reducing the number of complex code structures. For example, type and null safety, extension functions, infix syntax, immutability, val/var differentiation, expression-oriented features, "when" statements, much easier work with collections, type auto conversion, and other syntactic sugar.

2. Following Kotlin idioms

    The author of Kotlin, Andrey Breslav, mentioned that Kotlin is both pragmatic and practical, but not academic.
    Its pragmatic features enable ideas to be transformed into real working software easily. Kotlin is closer to natural languages than its predecessors, and it implements the following design principles: readability, reusability, interoperability, security, and tool-friendliness (https://blog.jetbrains.com/kotlin/2018/10/kotlinconf-2018-announcements/).

3. Using Kotlin efficiently

    Some Kotlin features can help you to write higher-performance code: including rich coroutine library, sequences, inline functions/classes, arrays of basic types, tailRec, and CallsInPlace of contract.

<!-- =============================================================================== -->
### <a name="c0.3"></a> Terminology

**Rules** — conventions that should be followed when programming.

**Recommendations** — conventions that should be considered when programming.

**Explanation** — necessary explanations of rules and recommendations.

**Valid Example** — recommended examples of rules and recommendations.

**Invalid Example** — not recommended examples of rules and recommendations.

Unless otherwise stated, this specification applies to versions 1.3 and later of Kotlin.

<!-- =============================================================================== -->
### <a name="c0.4"></a> Exceptions

Even though exceptions may exist, it is essential to understand why rules and recommendations are needed.
Depending on a project situation or personal habits, you can break some of the rules. However, remember that one exception may lead to many and eventually can destroy code consistency. As such, there should be very few exceptions.
When modifying open-source code or third-party code, you can choose to use the code style from this open-source project (instead of using the existing specifications) to maintain consistency.
Software that is directly based on the Android native operating system interface, such as the Android Framework, remains consistent with the Android style.
# <a name="c1"></a> 1. Naming
In programming, it is not always easy to meaningfully and appropriately name variables, functions, classes, etc. Using meaningful names helps to clearly express your code's main ideas and functionality and avoid misinterpretation, unnecessary coding and decoding, "magic" numbers, and inappropriate abbreviations.

Note: The source file encoding format (including comments) must be UTF-8 only. The ASCII horizontal space character (0x20, that is, space) is the only permitted whitespace character. Tabs should not be used for indentation.

<!-- =============================================================================== -->
### <a name="c1.1"></a> 1.1 Identifiers
This section describes the general rules for naming identifiers.
#### <a name="r1.1.1"></a> 1.1.1 Identifiers naming conventions

For identifiers, use the following naming conventions:
1.	All identifiers should use only ASCII letters or digits, and the names should match regular expressions `\w{2,64}`.
Explanation: Each valid identifier name should match the regular expression `\w{2,64}`.
`{2,64}` means that the name length is 2 to 64 characters, and the length of the variable name should be proportional to its life range, functionality, and responsibility.
Name lengths of less than 31 characters are generally recommended. However, this depends on the project. Otherwise, a class declaration with generics or inheritance from a superclass can cause line breaking.
No special prefix or suffix should be used in names. The following examples are inappropriate names: name_, mName, s_name, and kName.

2.	Choose file names that would describe the content. Use camel case (PascalCase) and `.kt` extension.

3.	Typical examples of naming:

| Meaning | Correct |Incorrect|
| ---- | ---- | ---- |
| "XML Http Request" | XmlHttpRequest | XMLHTTPRequest |
| "new customer ID" | newCustomerId | newCustomerID |
| "inner stopwatch" | innerStopwatch | innerStopWatch |
| "supports IPv6 on iOS" | supportsIpv6OnIos | supportsIPv6OnIOS |
| "YouTube importer" | YouTubeImporter | YoutubeImporter |

4.	The usage of (``) and free naming for functions and identifiers are prohibited. For example, the following code is not recommended:

```kotlin
val `my dummy name-with-minus` = "value"
```

The only exception is function names in `Unit tests.`

5.	Backticks (``) should not be used for identifiers, except the names of test methods (marked with @Test annotation):
```kotlin
 @Test fun `my test`() { /*...*/ }
```
6.  The following table contains some characters that may cause confusion. Be careful when using them as identifiers. To avoid issues, use other names instead.

| Expected      | Confusing name           | Suggested name   |
| ------------- | ------------------------ | ---------------- |
| 0 (zero)      | O, D                     | obj, dgt         |
| 1 (one)       | I, l                     | it, ln, line     |
| 2 (two)       | Z                        | n1, n2           |
| 5 (five)      | S                        | xs, str          |
| 6 (six)       | e                        | ex, elm          |
| 8 (eight)     | B                        | bt, nxt          |
| n,h           | h,n                      | nr, head, height |
| rn, m         | m,rn                     | mbr, item        |

**Exceptions:**
- The i,j,k variables used in loops are part of the industry standard. One symbol can be used for such variables.
- The `e` variable can be used to catch exceptions in catch block: `catch (e: Exception) {}`
- The Java community generally does not recommend the use of prefixes. However, when developing Android code, you can use the s and m prefixes for static and non-public non-static fields, respectively.
Note that prefixing can also negatively affect the style and the auto-generation of getters and setters.

| Type | Naming style |
| ---- | ---- |
| Interfaces, classes, annotations, enumerated types, and object type names | Camel case, starting with a capital letter. Test classes have a Test suffix. The filename is 'TopClassName'.kt.  |
| Class fields, local variables, methods, and method parameters | Camel case starting with a low case letter. Test methods can be underlined with '_'; the only exception is [backing properties](#r6.1.7).
| Static constants and enumerated values | Only uppercase underlined with '_' |
| Generic type variable | Single capital letter, which can be followed by a number, for example: `E, T, U, X, T2` |
| Exceptions | Same as class names, but with a suffix Exception, for example: `AccessException` and `NullPointerException`|

<!-- =============================================================================== -->
### <a name="c1.2"></a> 1.2 Packages

#### <a name="r1.2.1"></a> Rule 1.2.1 Package names dots
Package names are in lower case and separated by dots. Code developed within your company should start with `your.company.domain.` Numbers are permitted in package names.
Each file should have a `package` directive.
Package names are all written in lowercase, and consecutive words are concatenated together (no underscores). Package names should contain both the product or module names and the department (or team) name to prevent conflicts with other teams.  Numbers are not permitted. For example: `org.apache.commons.lang3`, `xxx.yyy.v2`.

**Exceptions:**

- In certain cases, such as open-source projects or commercial cooperation, package names should not start with `your.company.domain.`
- If the package name starts with a number or other character that cannot be used at the beginning of the Java/Kotlin package name, then underscores are allowed. For example: `com.example._123name`.
- Underscores are sometimes permitted if the package name contains reserved Java/Kotlin keywords, such as `org.example.hyphenated_name`, `int_.example`.

**Valid example**:
```kotlin
package your.company.domain.mobilecontrol.views
```

<!-- =============================================================================== -->
### <a name="c1.3"></a> 1.3 Classes, enumerations, typealias, interfaces
This section describes the general rules for naming classes, enumerations, and interfaces.
### <a name="r1.3.1"></a> 1.3.1 Classes, enumerations, typealias, interface names use Camel case
Classes, enumerations, and interface names use `UpperCamelCase` nomenclature. Follow the naming rules described below:
1.	A class name is usually a noun (or a noun phrase) denoted using the camel case nomenclature, such as UpperCamelCase. For example: `Character` or `ImmutableList`.
An interface name can also be a noun or noun phrase (such as `List`) or an adjective or adjective phrase (such as `Readable`).
Note that verbs are not used to name classes. However, nouns (such as `Customer`, `WikiPage`, and `Account`) can be used. Try to avoid using vague words such as `Manager` and `Process`.

2.	Test classes start with the name of the class they are testing and end with 'Test'. For example, `HashTest` or `HashIntegrationTest`.

**Invalid example**:
```kotlin
class marcoPolo {}
class XMLService {}
interface TAPromotion {}
class info {}
```

**Valid example**:
```kotlin
class MarcoPolo {}
class XmlService {}
interface TaPromotion {}
class Order {}
```

<!-- =============================================================================== -->
### <a name="c1.4"></a> 1.4 Functions
This section describes the general rules for naming functions.
### <a name="r1.4.1"></a> 1.4.1 Function names should be in camel case
Function names should use `lowerCamelCase` nomenclature. Follow the naming rules described below:
1.	Function names are usually verbs or verb phrases denoted with the camel case nomenclature (`lowerCamelCase`).
For example: `sendMessage`, `stopProcess`, or `calculateValue`.
To name functions, use the following formatting rules:

a) To get, modify, or calculate a certain value: get + non-boolean field(). Note that the Kotlin compiler automatically generates getters for some classes, applying the special syntax preferred for the 'get' fields: kotlin private val field: String get() { }. kotlin private val field: String get() { }.
```kotlin
private val field: String
get() {
}
```
Note: The calling property access syntax is preferred to call getter directly. In this case, the Kotlin compiler automatically calls the corresponding getter.

b) `is` + boolean variable name()

c) `set` + field/attribute name(). However, note that the syntax and code generation for Kotlin are completely the same as those described for the getters in point a.

d) `has` + Noun / adjective ()

e) verb()
Note: Note: Verb are primarily used for the action objects, such as `document.print ()`

f) verb + noun()

g) The Callback function allows the names that use the preposition + verb format, such as: `onCreate()`, `onDestroy()`, `toString()`.

**Invalid example**:

```kotlin
fun type(): String
fun Finished(): Boolean
fun visible(boolean)
fun DRAW()
fun KeyListener(Listener)
```

**Valid example**:

```kotlin
fun getType(): String
fun isFinished(): Boolean
fun setVisible(boolean)
fun draw()
fun addKeyListener(Listener)
```

2.	An underscore (`_`) can be included in the JUnit test function name and should be used as a separator. Each logical part is denoted in `lowerCamelCase`, for example, a typical pattern of using underscore: `pop_emptyStack`.
<!-- =============================================================================== -->
### <a name="c1.5"></a> 1.5 Constants
This section describes the general rules for naming constraints.
### <a name="r1.5.1"></a> 1.5.1 Using UPPER case and underscore characters in a constraint name
Constant names should be in UPPER case, words separated by underscore. The general constant naming conventions are listed below:
1. Constants are attributes created with the `const` keyword or top-level/`val` local variables of an object that holds immutable data. In most cases, constants can be identified as a `const val` property from the `object`/`companion object`/file top level. These variables contain fixed constant values that typically should never be changed by programmers. This includes basic types, strings, immutable types, and immutable collections of immutable types. The value is not constant for the object, which state can be changed.
2. Constant names should contain only uppercase letters separated by an underscores. They should have a val or const val modifier to make them final explicitly. In most cases, if you need to specify a constant value, then you need to create it with the "const val" modifier. Note that not all `val` variables are constants.
3. Objects with immutable content, such as `Logger` and `Lock`, can be in uppercase as constants or have camel case as regular variables.
4. Use meaningful constants instead of `magic numbers`. SQL or logging strings should not be treated as magic numbers, nor should they be defined as string constants.
Magic constants, such as `NUM_FIVE = 5` or `NUM_5 = 5` should not be treated as constants. This is because mistakes will easily be made if they are changed to `NUM_5 = 50` or 55.
These constants typically represent business logic values, such as measures, capacity, scope, location, tax rate, promotional discounts, and power base multiples in algorithms.
You can avoid using magic numbers with the following method:
- Using library functions and APIs. For example, instead of checking that `size == 0`, use `isEmpty()` function. To work with `time`, use built-ins from `java.time API`.
- Enumerations can be used to name patterns. Refer to [Recommended usage scenario for enumeration in 3.9](#c3.9).

**Invalid example**:

```kotlin
var int MAXUSERNUM = 200;
val String sL = "Launcher";
```

**Valid example**:

```kotlin
const val int MAX_USER_NUM = 200;
const val String APPLICATION_NAME = "Launcher";
```

<!-- =============================================================================== -->
### <a name="c1.6"></a> 1.6 Non-constant fields (variables)
This section describes the general rules for naming variables.
### <a name="r1.6.1"></a> 1.6.1 Non-constant field name
Non-constant field names should use camel case and start with a lowercase letter.
A local variable cannot be treated as constant even if it is final and immutable. Therefore, it should not use the preceding rules. Names of collection type variables (sets, lists, etc.) should contain plural nouns.
For example: `var namesList: List<String>`

Names of non-constant variables should use `lowerCamelCase`. The name of the final immutable field used to store the singleton object can use the same camel case notation.

**Invalid example**:
```kotlin
customername: String
user: List<String> = listof()
```

**Valid example**:
```kotlin
var customerName: String
val users: List<String> = listOf();
val mutableCollection: MutableSet<String> = HashSet()
```

### <a name="r1.6.2"></a> 1.6.2 Boolean variable names with negative meaning

Avoid using Boolean variable names with a negative meaning. When using a logical operator and name with a negative meaning, the code may be difficult to understand, which is referred to as the "double negative".
For instance, it is not easy to understand the meaning of !isNotError.
The JavaBeans specification automatically generates isXxx() getters for attributes of Boolean classes.
However, not all methods returning Boolean type have this notation.
For Boolean local variables or methods, it is highly recommended that you add non-meaningful prefixes, including is (commonly used by JavaBeans), has, can, should, and must. Modern integrated development environments (IDEs) such as Intellij are already capable of doing this for you when you generate getters in Java. For Kotlin, this process is even more straightforward as everything is on the byte-code level under the hood.

**Invalid example**:
```kotlin
val isNoError: Boolean
val isNotFound: Boolean
fun empty()
fun next();
```

**Valid example**:
```kotlin
val isError: Boolean
val isFound: Boolean
val hasLicense: Boolean
val canEvaluate: Boolean
val shouldAbort: Boolean
fun isEmpty()
fun hasNext()
```
# <a name="c2"></a> 2. Comments

The best practice is to begin your code with a summary, which can be one sentence.
Try to balance between writing no comments at all and obvious commentary statements for each line of code.
Comments should be accurately and clearly expressed, without repeating the name of the class, interface, or method.
Comments are not a solution to the wrong code. Instead, you should fix the code as soon as you notice an issue or plan to fix it (by entering a TODO comment, including a Jira number).
Comments should accurately reflect the code's design ideas and logic and further describe its business logic.
As a result, other programmers will be able to save time when trying to understand the code.
Imagine that you are writing the comments to help yourself to understand the original ideas behind the code in the future.

### <a name="c2.1"></a> 2.1 General form of Kdoc

KDoc is a combination of JavaDoc's block tags syntax (extended to support specific constructions of Kotlin) and Markdown's inline markup.
The basic format of KDoc is shown in the following example:

```kotlin
 /**
 * There are multiple lines of KDoc text,
 * Other ...
 */
fun method(arg: String) {
    // ...
}
```

It is also shown in the following single-line form:

```kotlin
 /** Short form of KDoc. */
```
Use a single-line form when you store the entire KDoc block in one line (and there is no KDoc mark @XXX). For detailed instructions on how to use KDoc, refer to [Official Document](https://docs.oracle.com/en/Kotlin/Kotlinse/11/tools/KDoc.html).

#### <a name="r2.1.1"></a> 2.1.1 Using KDoc for the public, protected, or internal code elements

At a minimum, KDoc should be used for every public, protected, or internal decorated class, interface, enumeration, method, and member field (property).
Other code blocks can also have KDocs if needed.
Instead of using comments or KDocs before properties in the primary constructor of a class - use `@property` tag in a KDoc of a class.
All properties of the primary constructor should also be documented in a KDoc with a `@property` tag.


**Incorrect example:**
```kotlin
/**
 * Class description
 */
class Example(
 /**
  * property description
  */
  val foo: Foo,
  // another property description
  val bar: Bar
)
```

**Correct example:**
```kotlin
/**
 * Class description
 * @property foo property description
 * @property bar another property description
 */
class Example(
  val foo: Foo,
  val bar: Bar
)
```
- Don't use Kdoc comments inside code blocks as block comments

**Incorrect Example:**

```kotlin
class Example {
  fun doGood() {
    /**
     * wrong place for kdoc
     */
    1 + 2
  }
}
```

**Correct Example:**

```kotlin
class Example {
  fun doGood() {
    /*
     * right place for block comment
    */
    1 + 2
  }
}
```

**Exceptions:**

* For setters/getters of properties, obvious comments (like `this getter returns field`) are optional. Note that Kotlin generates simple `get/set` methods under the hood.

* It is optional to add comments for simple one-line methods, such as shown in the example below:
```kotlin
val isEmpty: Boolean
    get() = this.size == 0
```

or

```kotlin
fun isEmptyList(list: List<String>) = list.size == 0
```

**Note:** You can skip KDocs for a method's override if it is almost the same as the superclass method.

#### <a name="r2.1.2"></a> 2.1.2 Describing methods that have arguments, a return value, or can throw an exception in the KDoc block

When the method has such details as arguments, return value, or can throw exceptions, it must be described in the KDoc block (with @param, @return, @throws, etc.).

**Valid examples:**

 ```kotlin
/**
 * This is the short overview comment for the example interface.
 *     / * Add a blank line between the comment text and each KDoc tag underneath * /
 * @since 1.6
 */
 protected abstract class Sample {
    /**
     * This is a long comment with whitespace that should be split in
     * comments on multiple lines if the line comment formatting is enabled.
     *     / * Add a blank line between the comment text and each KDoc tag underneath * /
     * @param fox A quick brown fox jumps over the lazy dog
     * @return battle between fox and dog
     */
    protected abstract fun foo(Fox fox)
     /**
      * These possibilities include: Formatting of header comments
      *     / * Add a blank line between the comment text and each KDoc tag underneath * /
      * @return battle between fox and dog
      * @throws ProblemException if lazy dog wins
      */
    protected fun bar() throws ProblemException {
        // Some comments / * No need to add a blank line here * /
        var aVar = ...

        // Some comments  / * Add a blank line before the comment * /
        fun doSome()
    }
 }
 ```

#### <a name="r2.1.3"></a>2.1.3 Only one space between the Kdoc tag and content. Tags are arranged in the order.

There should be only one space between the Kdoc tag and content. Tags are arranged in the following order: @param, @return, and @throws.

Therefore, Kdoc should contain the following:
- Functional and technical description, explaining the principles, intentions, contracts, API, etc.
- The function description and @tags (`implSpec`, `apiNote`, and `implNote`) require an empty line after them.
- `@implSpec`: A specification related to API implementation, and it should let the implementer decide whether to override it.
- `@apiNote`: Explain the API precautions, including whether to allow null and whether the method is thread-safe, as well as the algorithm complexity, input, and output range, exceptions, etc.
- `@implNote`: A note related to API implementation, which implementers should keep in mind.
- One empty line, followed by regular `@param`, `@return`, `@throws`, and other comments.
- The conventional standard "block labels" are arranged in the following order: `@param`, `@return`, `@throws`.
Kdoc should not contain:
- Empty descriptions in tag blocks. It is better not to write Kdoc than waste code line space.
- There should be no empty lines between the method/class declaration and the end of Kdoc (`*/` symbols).
- `@author` tag. It doesn't matter who originally created a class when you can use `git blame` or VCS of your choice to look through the changes history.
Important notes:
- KDoc does not support the `@deprecated` tag. Instead, use the `@Deprecated` annotation.
- The `@since` tag should be used for versions only. Do not use dates in `@since` tag, it's confusing and less accurate.

If a tag block cannot be described in one line, indent the content of the new line by *four spaces* from the `@` position to achieve alignment (`@` counts as one + three spaces).

**Exception:**

When the descriptive text in a tag block is too long to wrap, you can indent the alignment with the descriptive text in the last line. The descriptive text of multiple tags does not need to be aligned.
See [3.8 Horizontal space](#c3.8).

In Kotlin, compared to Java, you can put several classes inside one file, so each class should have a Kdoc formatted comment (as stated in rule 2.1).
This comment should contain @since tag. The right style is to write the application version when its functionality is released. It should be entered after the `@since` tag.

**Examples:**

```kotlin
/**
 * Description of functionality
 *
 * @since 1.6
 */
```

Other KDoc tags (such as @param type parameters and @see.) can be added as follows:
```kotlin
/**
 * Description of functionality
 *
 * @apiNote: Important information about API
 *
 * @since 1.6
 */
```

### <a name="c2.2"></a> 2.2 Adding comments on the file header

This section describes the general rules of adding comments on the file header.

### <a name="r2.2.1"></a> 2.2.1 Formatting of comments in the file header

Comments on the file header should be placed before the package name and imports. If you need to add more content to the comment, subsequently add it in the same format.

Comments on the file header must include copyright information, without the creation date and author's name (use VCS for history management).
Also, describe the content inside files that contain multiple or no classes.

The following examples for Huawei describe the format of the *copyright license*: \
Chinese version: `版权所有 (c) 华为技术有限公司 2012-2020` \
English version: `Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.`
`2012` and `2020` are the years the file was first created and the current year, respectively.

Do not place **release notes** in header, use VCS to keep track of changes in file. Notable changes can be marked in individual KDocs using `@since` tag with version.

Invalid example:
```kotlin
/**
 * Release notes:
 * 2019-10-11: added class Foo
 */

class Foo
```

Valid example:
```kotlin
/**
 * @since 2.4.0
 */
class Foo
```

- The **copyright statement** can use your company's subsidiaries, as shown in the below examples: \
Chinese version: `版权所有 (c) 海思半导体 2012-2020` \
English version: `Copyright (c) Hisilicon Technologies Co., Ltd. 2012-2020. All rights reserved.`

- The copyright information should not be written in KDoc style or use single-line comments. It must start from the beginning of the file.
The following example is a copyright statement for Huawei, without other functional comments:

```kotlin
/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 */
```

The following factors should be considered when writing the file header or comments for top-level classes:
- File header comments must start from the top of the file. If it is a top-level file comment, there should be a blank line after the last Kdoc `*/` symbol. If it is a comment for a top-level class, the class declaration should start immediately without using a newline.
- Maintain a unified format. The specific format can be formulated by the project (for example, if you use an existing opensource project), and you need to follow it.
- A top-level file-Kdoc must include a copyright and functional description, especially if there is more than one top-level class.
- Do not include empty comment blocks. If there is no content after the option `@apiNote`, the entire tag block should be deleted.
- The industry practice is not to include historical information in the comments. The corresponding history can be found in VCS (git, svn, etc.). Therefore, it is not recommended to include historical data in the comments of the Kotlin source code.

### <a name="c2.3"></a> 2.3 Comments on the function header

Comments on the function header are placed above function declarations or definitions. A newline should not exist between a function declaration and its Kdoc. Use the preceding <<c2.1,KDoc>> style rules.

As stated in Chapter 1, the function name should reflect its functionality as much as possible. Therefore, in the Kdoc, try to describe the functionality that is not mentioned in the function name.
Avoid unnecessary comments on dummy coding.

The function header comment's content is optional, but not limited to function description, return value, performance constraints, usage, memory conventions, algorithm implementation, reentrant requirements, etc.

### <a name="c2.4"></a> 2.4 Code comments

This section describes the general rules of adding code comments.

#### <a name="r2.4.1"></a> 2.4.1 Add a blank line between the body of the comment and Kdoc tag-blocks.

It is a good practice to add a blank line between the body of the comment and Kdoc tag-blocks. Also, consider the following rules:
- There must be one space between the comment character and the content of the comment.
- There must be a newline between a Kdoc and the presiding code.
- An empty line should not exist between a Kdoc and the code it is describing. You do not need to add a blank line before the first comment in a particular namespace (code block) (for example, between the function declaration and first comment in a function body).

**Valid Examples:**

```kotlin
/**
 * This is the short overview comment for the example interface.
 *
 * @since 1.6
 */
 public interface Example {
    // Some comments  /* Since it is the first member definition in this code block, there is no need to add a blank line here */
    val aField: String = ...
                     /* Add a blank line above the comment */
    // Some comments
    val bField: String = ...
                      /* Add a blank line above the comment */
    /**
     * This is a long comment with whitespace that should be split in
     * multiple line comments in case the line comment formatting is enabled.
     *                /* blank line between description and Kdoc tag */
     * @param fox A quick brown fox jumps over the lazy dog
     * @return the rounds of battle of fox and dog
     */
    fun foo(Fox fox)
                      /* Add a blank line above the comment */
     /**
      * These possibilities include: Formatting of header comments
      *
      * @return the rounds of battle of fox and dog
      * @throws ProblemException if lazy dog wins
      */
    fun bar() throws ProblemException {
        // Some comments  /* Since it is the first member definition in this range, there is no need to add a blank line here */
        var aVar = ...

        // Some comments  /* Add a blank line above the comment */
        fun doSome()
    }
 }
```

- Leave one single space between the comment on the right side of the code and the code.
If you use conditional comments in the `if-else-if` scenario, put the comments inside the `else-if` branch or in the conditional block, but not before the `else-if`. This makes the code more understandable.
When the if-block is used with curly braces, the comment should be placed on the next line after opening the curly braces.
Compared to Java, the `if` statement in Kotlin statements returns a value. For this reason, a comment block can describe a whole `if-statement`.

**Valid examples:**

```kotlin

val foo = 100  // right-side comment
val bar = 200  /* right-side comment */

// general comment for the value and whole if-else condition
val someVal = if (nr % 15 == 0) {
    // when nr is a multiple of both 3 and 5
    println("fizzbuzz")
} else if (nr % 3 == 0) {
    // when nr is a multiple of 3, but not 5
    // We print "fizz", only.
    println("fizz")
} else if (nr % 5 == 0) {
    // when nr is a multiple of 5, but not 3
    // we print "buzz" only.
    println("buzz")
} else {
    // otherwise, we print the number.
    println(x)
}
```

- Start all comments (including KDoc) with a space after the first symbol (`//`, `/*`, `/**` and `*`)

**Valid example:**

```kotlin
val x = 0  // this is a comment
```

#### <a name="r2.4.2"></a> 2.4.2 Do not comment on unused code blocks

Do not comment on unused code blocks, including imports. Delete these code blocks immediately.
A code is not used to store history. Git, svn, or other VCS tools should be used for this purpose.
Unused imports increase the coupling of the code and are not conducive to maintenance. The commented out code cannot be appropriately maintained.
In an attempt to reuse the code, there is a high probability that you will introduce defects that are easily missed.
The correct approach is to delete the unnecessary code directly and immediately when it is not used anymore.
If you need the code again, consider porting or rewriting it as changes could have occurred since you first commented on the code.

#### <a name="r2.4.3"></a>2.4.3 Code delivered to the client should not contain TODO/FIXME comments

The code officially delivered to the client typically should not contain TODO/FIXME comments.
`TODO` comments are typically used to describe modification points that need to be improved and added. For example, refactoring FIXME comments are typically used to describe known defects and bugs that will be subsequently fixed and are not critical for an application.
They should all have a unified style to facilitate unified text search processing.

**Example:**

```kotlin
// TODO(<author-name>): Jira-XXX - support new json format
// FIXME: Jira-XXX - fix NPE in this code block
```

At a version development stage, these annotations can be used to highlight the issues in the code, but all of them should be fixed before a new product version is released.
# <a name="c3"></a>3. General formatting (typesetting)
<!-- =============================================================================== -->
### <a name="c3.1"></a> 3.1 File-related rules
This section describes the rules related to using files in your code.
#### <a name="r3.1.1"></a> 3.1.1 Avoid files that are too long

If the file is too long and complicated, it should be split into smaller files, functions, or modules. Files should not exceed 2000 lines (non-empty and non-commented lines).
It is recommended to horizontally or vertically split the file according to responsibilities or hierarchy of its parts.
The only exception to this rule is code generation - the auto-generated files that are not manually modified can be longer.

#### <a name="r3.1.2"></a> 3.1.2 Code blocks in the source file should be separated by one blank line
A source file contains code blocks in the following order: copyright, package name, imports, and top-level classes. They should be separated by one blank line.

a) Code blocks should be in the following order:
1.	Kdoc for licensed or copyrighted files
2.	`@file` annotation
3.	Package name
4.	Import statements
5.	Top-class header and top-function header comments
6.	Top-level classes or functions

b) Each of the preceding code blocks should be separated by a blank line.

c) Import statements are alphabetically arranged, without using line breaks and wildcards ( wildcard imports - `*`).

d) **Recommendation**: One `.kt` source file should contain only one class declaration, and its name should match the filename

e) Avoid empty files that do not contain the code or contain only imports/comments/package name

f) Unused imports should be removed
#### <a name="r3.1.3"></a> 3.1.3 Import statements order

From top to bottom, the order is the following:
1. Android
2. Imports of packages used internally in your organization
3. Imports from other non-core dependencies
4. Java core packages
5. kotlin stdlib

Each category should be alphabetically arranged. Each group should be separated by a blank line. This style is compatible with  [Android import order](https://source.android.com/setup/contribute/code-style#order-import-statements).

**Valid example**:
```kotlin
import android.* // android
import androidx.* // android
import com.android.* // android

import com.your.company.* // your company's libs
import your.company.* // your company's libs

import com.fasterxml.jackson.databind.ObjectMapper // other third-party dependencies
import org.junit.jupiter.api.Assertions

import java.io.IOException // java core packages
import java.net.URL

import kotlin.system.exitProcess  // kotlin standard library
import kotlinx.coroutines.*  // official kotlin extension library
```

#### <a name="r3.1.4"></a> 3.1.4 Order of declaration parts of class-like code structures
The declaration parts of class-like code structures (class, interface, etc.) should be in the following order: compile-time constants (for objects), class properties, late-init class properties, init-blocks, constructors, public methods, internal methods, protected methods, private methods, and companion object. Blank lines should separate their declaration.
Notes:
1.	There should be no blank lines between properties with the following **exceptions**: when there is a comment before a property on a separate line or annotations on a separate line.
2.	Properties with comments/Kdoc should be separated by a newline before the comment/Kdoc.
3.	Enum entries and constant properties (`const val`) in companion objects should be alphabetically arranged.

The declaration part of a class or interface should be in the following order:
- Compile-time constants (for objects)
- Properties
- Late-init class properties
- Init-blocks
- Constructors
- Methods or nested classes. Put nested classes next to the code they are used by.
If the classes are meant to be used externally, and are not referenced inside the class, put them after the companion object.
- Companion object

**Exception:**
All variants of a `private val` logger should be placed at the beginning of the class (`private val log`, `LOG`, `logger`, etc.).

#### <a name="r3.1.5"></a> 3.1.5 Order of declaration of top-level code structures
Kotlin allows several top-level declaration types: classes, objects, interfaces, properties and functions.
When declaring more than one class or zero classes (e.g. only functions), as per rule [2.2.1](#r2.2.1), you should document the whole file in the header KDoc.
When declaring top-level structures, keep the following order:
1. Top-level constants and properties (following same order as properties inside a class: `const val`,`val`, `lateinit var`, `var`)
2. typealiases (grouped by their visibility modifiers)
2. Interfaces, classes and objects (grouped by their visibility modifiers)
3. Extension functions
4. Other functions

**Note**:
Extension functions shouldn't have receivers declared in the same file according to [rule 6.2.3](#r6.2.3)

Valid example:
```kotlin
package com.saveourtool.diktat.example

const val CONSTANT = 42

val topLevelProperty = "String constant"

internal typealias ExamplesHandler = (IExample) -> Unit

interface IExample

class Example : IExample

private class Internal

fun Other.asExample(): Example { /* ... */ }

private fun Other.asInternal(): Internal { /* ... */ }

fun doStuff() { /* ... */ }
```

**Note**:
kotlin scripts (.kts) allow arbitrary code to be placed on the top level. When writing kotlin scripts, you should first declare all properties, classes
and functions. Only then you should execute functions on top level. It is still recommended wrapping logic inside functions and avoid using top-level statements
for function calls or wrapping blocks of code in top-level scope functions like `run`.

Example:
```kotlin
/* class declarations */
/* function declarations */
run {
    // call functions here
}
```

<!-- =============================================================================== -->
### <a name="c3.2"></a> 3.2 Braces
This section describes the general rules of using braces in your code.
#### <a name="r3.2.1"></a> 3.2.1 Using braces in conditional statements and loop blocks

Braces should always be used in `if`, `else`, `for`, `do`, and `while` statements, even if the program body is empty or contains only one statement. In special Kotlin `when` statements, you do not need to use braces for single-line statements.

**Valid example:**

```kotlin
when (node.elementType) {
    FILE -> {
        checkTopLevelDoc(node)
        checkSomething()
     }
    CLASS -> checkClassElements(node)
}
```
**Exception:** The only exception is ternary operator in Kotlin (a single line `if () <> else <>` )

**Invalid example:**

```kotlin
val value = if (string.isEmpty())  // WRONG!
                0
            else
                1
```

**Valid example**:

```kotlin
val value = if (string.isEmpty()) 0 else 1  // Okay
```

```kotlin
if (condition) {
    println("test")
} else {
    println(0)
}
```

#### <a name="r3.2.2"></a> 3.2.2  Opening braces are placed at the end of the line in *non-empty* blocks and block structures
For *non-empty* blocks and block structures, the opening brace is placed at the end of the line.
Follow the K&R style (1TBS or OTBS) for *non-empty* code blocks with braces:
- The opening brace and first line of the code block are on the same line.
- The closing brace is on its own new line.
- The closing brace can be followed by a newline character. The only exceptions are `else`, `finally`, and `while` (from `do-while` statement), or `catch` keywords.
These keywords should not be split from the closing brace by a newline character.

**Exception cases**:

1) For lambdas, there is no need to put a newline character after the first (function-related) opening brace. A newline character should appear only after an arrow (`->`) (see [point 5 of Rule 3.6.2](#r3.6.2)).

```kotlin
arg.map { value ->
    foo(value)
}
```

2) for `else`/`catch`/`finally`/`while` (from `do-while` statement) keywords closing brace should stay on the same line:
 ```kotlin
do {
    if (true) {
        x++
    } else {
        x--
    }
} while (x > 0)
```

**Valid example:**

 ```kotlin
        return arg.map { value ->
            while (condition()) {
                method()
            }
            value
        }

        return MyClass() {
            @Override
              fun method() {
                if (condition()) {
                    try {
                        something()
                    } catch (e: ProblemException) {
                        recover()
                    }
                } else if (otherCondition()) {
                    somethingElse()
                } else {
                    lastThing()
                }
            }
        }
 ```

<!-- =============================================================================== -->
### <a name="c3.3"></a> 3.3 Indentation

Only spaces are permitted for indentation, and each indentation should equal `four spaces` (tabs are not permitted).
If you prefer using tabs, simply configure them to change to spaces in your IDE automatically.
These code blocks should be indented if they are placed on the new line, and the following conditions are met:
-	The code block is placed immediately after an opening brace.
-	The code block is placed after each operator, including the assignment operator (`+`/`-`/`&&`/`=`/etc.)
-	The code block is a call chain of methods:
```kotlin
someObject
    .map()
    .filter()
```
-  The code block is placed immediately after the opening parenthesis.
-  The code block is placed immediately after an arrow in lambda:

 ```kotlin
arg.map { value ->
    foo(value)
}
```

**Exceptions**:
1.	Argument lists: \
a) Eight spaces are used to indent argument lists (both in declarations and at call sites). \
b) Arguments in argument lists can be aligned if they are on different lines.

2.	Eight spaces are used if there is a newline after any binary operator.

3.	Eight spaces are used for functional-like styles when the newline is placed before the dot.

4.	Supertype lists: \
a) Four spaces are used if the colon before the supertype list is on a new line. \
b) Four spaces are used before each supertype, and eight spaces are used if the colon is on a new line.

**Note:** there should be an indentation after all statements such as `if`, `for`, etc. However, according to this code style, such statements require braces.

```kotlin
if (condition)
    foo()
```

**Exceptions**:
- When breaking the parameter list of a method/class constructor, it can be aligned with `8 spaces`. A parameter that was moved to a new line can be on the same level as the previous argument:

```kotlin
fun visit(
        node: ASTNode,
        autoCorrect: Boolean,
        params: KtLint.ExperimentalParams,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit
) {
}
```

- Such operators as `+`/`-`/`*` can be indented with `8 spaces`:

```kotlin
val abcdef = "my splitted" +
                " string"
```

- Opening and closing quotes in multiline string should have same indentation

```kotlin
lintMethod(
            """
                    |val q = 1
                    |
            """.trimMargin()
    )
```

- A list of supertypes should be indented with `4 spaces` if they are on different lines or with `8 spaces` if the leading colon is also on a separate line

```kotlin
class A :
    B()

class A
    :
        B()
```

<!-- =============================================================================== -->
### <a name="c3.4"></a> 3.4 Empty blocks

Avoid empty blocks, and ensure braces start on a new line. An empty code block can be closed immediately on the same line and the next line. However, a newline is recommended between opening and closing braces `{}` (see the examples below.)

Generally, empty code blocks are prohibited; using them is considered a bad practice (especially for catch block).
They are appropriate for overridden functions, when the base class's functionality is not needed in the class-inheritor, for lambdas used as a function and for empty function in implementation of functional interface.
```kotlin
override fun foo() {
}
```

**Valid examples** (note once again that generally empty blocks are prohibited):

```kotlin
fun doNothing() {}

fun doNothingElse() {
}

fun foo(bar: () -> Unit = {})
```

**Invalid examples:**
```kotlin
try {
  doSomething()
} catch (e: Some) {}
```

Use the following valid code instead:
```kotlin
try {
   doSomething()
} catch (e: Some) {
}
```

<!-- =============================================================================== -->
### <a name="c3.5"></a> 3.5 Line length

Line length should be less than 120 symbols. Otherwise, it should be split.

If `complex property` initializing is too long, It should be split into priorities: \
1. Logic Binary Expression (&&  ||) \
2. Comparison Binary Expression (> < == >= <= !=) \
3. Other types (Arithmetical and Bit operation) (+ - * / % >> << *= += -= /= %= ++ -- ! in !in etc)

**Invalid example:**
```kotlin
val complexProperty = 1 + 2 + 3 + 4
```
**Valid example:**
```kotlin
val complexProperty = 1 + 2 +
    3 + 4
```

**Invalid example:**
```kotlin
val complexProperty = (1 + 2 + 3 > 0) && ( 23 * 4 > 10 * 6)
```
**Valid example:**
```kotlin
val complexProperty = (1 + 2 + 3 > 0) &&
    (23 * 4 > 10 * 6)
```

If long line should be split in `Elvis Operator` (?:), it`s done like this

**Invalid example:**
```kotlin
val value = first ?: second
```
**Valid example:**
```kotlin
val value = first
    ?: second
```

If long line in `Dot Qualified Expression` or `Safe Access Expression`, it`s done like this:

**Invalid example:**
```kotlin
val value = This.Is.Very.Long.Dot.Qualified.Expression
```
**Valid example:**
```kotlin
val value = This.Is.Very.Long
    .Dot.Qualified.Expression
```

**Invalid example:**
```kotlin
val value = This.Is?.Very?.Long?.Safe?.Access?.Expression
```
**Valid example:**
```kotlin
val value = This.Is?.Very?.Long
    ?.Safe?.Access?.Expression
```

if `value arguments list` is too long, it also should be split:

**Invalid example:**
```kotlin
val result1 = ManyParamInFunction(firstArgument, secondArgument, thirdArgument, fourthArgument, fifthArguments)
```
**Valid example:**
```kotlin
val result1 = ManyParamInFunction(firstArgument,
 secondArgument, thirdArgument, fourthArgument,
 fifthArguments)
```

If `annotation` is too long, it also should be split:

**Invalid example:**
```kotlin
@Query(value = "select * from table where age = 10", nativeQuery = true)
fun foo() {}
```
**Valid example:**
```kotlin
@Query(
    value = "select * from table where age = 10",
    nativeQuery = true)
fun foo() {}
```

Long one line `function` should be split:

**Invalid example:**
```kotlin
fun foo() = goo().write("TooLong")
```
**Valid example:**
```kotlin
fun foo() =
    goo().write("TooLong")
```

Long `binary expression` should be split into priorities: \
1. Logic Binary Expression (**&&**  **||**) \
2. Comparison Binary Expression (**>** **<** **==** **>=** **<=** **!=**) \
3. Other types (Arithmetical and Bit operation) (**+** **-** * **/** **%** **>>** **<<** **/*=** **+=** **-=** **/=** **%=** **++** **--** **!** **in** **!in** etc)

**Invalid example:**
```kotlin
if (( x >  100) || y < 100 && !isFoo()) {}
```

**Valid example:**
```kotlin
if (( x >  100) ||
    y < 100 && !isFoo()) {}
```

`String template` also can be split in white space in string text

**Invalid example:**
```kotlin
val nameString = "This is very long string template"
```

**Valid example:**
```kotlin
val nameString = "This is very long" +
        " string template"
```

Long `Lambda argument` should be split:

**Invalid example:**
```kotlin
val variable = a?.filter { it.elementType == true } ?: null
```

**Valid example:**
```kotlin
val variable = a?.filter {
    it.elementType == true
} ?: null
```

Long one line `When Entry` should be split:

**Invalid example:**
```kotlin
when(elem) {
    true -> long.argument.whenEntry
}
```
**Valid example:**
```kotlin
when(elem) {
    true -> {
        long.argument.whenEntry
    }
}
```

If the examples above do not fit, but the line needs to be split and this in `property`, this is fixed like thisЖ

**Invalid example:**
```kotlin
val element = veryLongNameFunction(firstParam)
```
**Valid example:**
```kotlin
val element =
    varyLongNameFunction(firstParam)
```

`Eol comment` also can be split, but it depends on comment location.
If this comment is on the same line with code it should be on line before:

**Invalid example:**
```kotlin
fun foo() {
    val name = "Nick" // this comment is too long
}
```
**Valid example:**
```kotlin
fun foo() {
    // this comment is too long
    val name = "Nick"
}
```

But if this comment is on new line - it should be split to several lines:

**Invalid example:**
```kotlin
// This comment is too long. It should be on two lines.
fun foo() {}
```

**Valid example:**
```kotlin
// This comment is too long.
// It should be on two lines.
fun foo() {}
```

The international code style prohibits `non-Latin` (`non-ASCII`) symbols. (See [Identifiers](#r1.1.1)) However, if you still intend on using them, follow
the following convention:

- One wide character occupies the width of two narrow characters.
The "wide" and "narrow" parts of a character are defined by its [east Asian width Unicode attribute](https://unicode.org/reports/tr11/).
Typically, narrow characters are also called "half-width" characters.
All characters in the ASCII character set include letters (such as `a, A`), numbers (such as `0, 3`), and punctuation spaces (such as `,` , `{`), all of which are narrow characters.
Wide characters are also called "full-width" characters. Chinese characters (such as `中, 文`), Chinese punctuation (`，` , `；` ), full-width letters and numbers (such as `Ａ、３`) are "full-width" characters.
Each one of these characters represents two narrow characters.

- Any line that exceeds this limit (`120 narrow symbols`) should be wrapped, as described in the [Newline section](#c3.5).

**Exceptions:**

1.	The long URL or long JSON method reference in KDoc.
2.	The `package` and `import` statements.
3.	The command line in the comment, enabling it to be cut and pasted into the shell for use.

<!-- =============================================================================== -->
### <a name="c3.6"></a> 3.6 Line breaks (newlines)
This section contains the rules and recommendations on using line breaks.
#### <a name="r3.6.1"></a> 3.6.1 Each line can have a maximum of one statement
Each line can have a maximum of one code statement. This recommendation prohibits the use of code with `;` because it decreases code visibility.

**Invalid example:**
```kotlin
val a = ""; val b = ""
```

**Valid example:**
```kotlin
val a = ""
val b = ""
```

#### <a name="r3.6.2"></a> 3.6.2 Rules for line-breaking

1) Unlike Java, Kotlin allows you not to put a semicolon (`;`) after each statement separated by a newline character.
    There should be no redundant semicolon at the end of the lines.

When a newline character is needed to split the line, it should be placed after such operators as `&&`/`||`/`+`/etc. and all infix functions (for example, `xor`).
However, the newline character should be placed before operators such as `.`, `?.`, `?:`, and `::`.

Note that all comparison operators, such as `==`, `>`, `<`, should not be split.

**Invalid example**:
```kotlin
     if (node !=
             null && test != null) {}
```

**Valid example**:
```kotlin
         if (node != null &&
                 test != null) {
         }
```

**Note:** You need to follow the functional style, meaning each function call in a chain with `.` should start at a new line if the chain of functions contains more than one call:
```kotlin
  val value = otherValue!!
          .map { x -> x }
          .filter {
              val a = true
              true
          }
          .size
```
**Note:** The parser prohibits the separation of the `!!` operator from the value it is checking.

**Exception**: If a functional chain is used inside the branches of a ternary operator, it does not need to be split with newlines.

**Valid example**:
```kotlin
if (condition) list.map { foo(it) }.filter { bar(it) } else list.drop(1)
```

**Note:** If dot qualified expression is inside condition or passed as an argument - it should be replaced with new variable.

**Invalid example**:
```kotlin
 if (node.treeParent.treeParent?.treeParent.findChildByType(IDENTIFIER) != null) {}
```

**Valid example**:
```kotlin
        val grandIdentifier = node
            .treeParent
            .treeParent
            ?.treeParent
            .findChildByType(IDENTIFIER)
        if (grandIdentifier != null) {}
```
**Second valid example**:
```kotlin
        val grandIdentifier = node.treeParent
            .treeParent
            ?.treeParent
            .findChildByType(IDENTIFIER)
        if (grandIdentifier != null) {}
```

2)	Newlines should be placed after the assignment operator (`=`).
3)	In function or class declarations, the name of a function or constructor should not be split by a newline from the opening brace `(`.
    A brace should be placed immediately after the name without any spaces in declarations or at call sites.
4)	Newlines should be placed right after the comma (`,`).
5)	If a lambda statement contains more than one line in its body, a newline should be placed after an arrow if the lambda statement has explicit parameters.
    If it uses an implicit parameter (`it`), the newline character should be placed after the opening brace (`{`).
    The following examples illustrate this rule:


**Invalid example:**
```kotlin
    value.map { name -> foo()
        bar()
    }
```

**Valid example:**
```kotlin
value.map { name ->
    foo()
    bar()
}

val someValue = { node:String -> node }
```

6) When the function contains only a single expression, it can be written as [expression function](https://kotlinlang.org/docs/reference/functions.html#single-expression-functions).
   The below example shows the style that should not be used.

Instead of:
```kotlin
override fun toString(): String { return "hi" }
```
use:
```kotlin
override fun toString() = "hi"
```

7)  If an argument list in a function declaration (including constructors) or function call contains more than two arguments, these arguments should be split by newlines in the following style.

**Valid example:**
 ```kotlin
class Foo(val a: String,
          b: String,
          val c: String) {
}

fun foo(
        a: String,
        b: String,
        c: String
) {

}
 ```

If and only if the first parameter is on the same line as an opening parenthesis, all parameters can be horizontally aligned by the first parameter.
Otherwise, there should be a line break after an opening parenthesis.

Kotlin 1.4 introduced a trailing comma as an optional feature, so it is generally recommended to place all parameters on a separate line
and append [trailing comma](https://kotlinlang.org/docs/reference/whatsnew14.html#trailing-comma).
It makes the resolving of merge conflicts easier.

**Valid example:**
 ```kotlin
fun foo(
        a: String,
        b: String,
) {

}
 ```

same should be done for function calls/constructor arguments/e.t.c

Kotlin supports trailing commas in the following cases:

Enumerations
Value arguments
Class properties and parameters
Function value parameters
Parameters with optional type (including setters)
Indexing suffix
Lambda parameters
when entry
Collection literals (in annotations)
Type arguments
Type parameters
Destructuring declarations

8) If the supertype list has more than two elements, they should be separated by newlines.

**Valid example:**
```kotlin
class MyFavouriteVeryLongClassHolder :
    MyLongHolder<MyFavouriteVeryLongClass>(),
    SomeOtherInterface,
    AndAnotherOne { }
```

<!-- =============================================================================== -->
### <a name="c3.7"></a> 3.7 Using blank lines

Reduce unnecessary blank lines and maintain a compact code size. By reducing unnecessary blank lines, you can display more code on one screen, which improves code readability.
- Blank lines should separate content based on relevance and should be placed between groups of fields, constructors, methods, nested classes, `init` blocks, and objects (see [3.1.2](#r3.1.2)).
- Do not use more than one line inside methods, type definitions, and initialization expressions.
- Generally, do not use more than two consecutive blank lines in a row.
- Do not put newlines in the beginning or end of code blocks with curly braces.

**Valid example:**
```kotlin
fun baz() {

    doSomething()  // No need to add blank lines at the beginning and end of the code block
    // ...

}
```

<!-- =============================================================================== -->
### <a name="c3.8"></a> 3.8 Horizontal space
This section describes general rules and recommendations for using spaces in the code.
#### <a name="r3.8.1"></a> 3.8.1: Usage of whitespace for code separation

Follow the recommendations below for using space to separate keywords:

**Note:** These recommendations are for cases where symbols are located on the same line. However, in some cases, a line break could be used instead of a space.

1.  Separate keywords (such as `if`, `when`, `for`) from the opening parenthesis with single whitespace.
    The only exception is the `constructor` keyword, which should not be separated from the opening parenthesis.

2.  Separate keywords like `else` or `try` from the opening brace (`{`) with single whitespace.
    If `else` is used in a ternary-style statement without braces, there should be a single space between `else` and the statement after: `if (condition) foo() else bar()`

3.  Use a **single** whitespace before all opening braces (`{`). The only exception is the passing of a lambda as a parameter inside parentheses:
 ```kotlin
     private fun foo(a: (Int) -> Int, b: Int) {}
     foo({x: Int -> x}, 5) // no space before '{'
 ```

4.  Single whitespace should be placed on both sides of binary operators. This also applies to operator-like symbols.
    For example:

 - A colon in generic structures with the `where` keyword:  `where T : Type`
 - Arrow in lambdas: `(str: String) -> str.length()`

**Exceptions:**

- Two colons (`::`) are written without spaces:\
  `Object::toString`
- The dot separator (`.`) that stays on the same line with an object name:\
  `object.toString()`
- Safe access modifiers `?.` and `!!` that stay on the same line with an object name:\
  `object?.toString()`
- Operator `..` for creating ranges:\
  `1..100`

5.  Use spaces after (`,`), (`:`), and (`;`), except when the symbol is at the end of the line.
    However, note that this code style prohibits the use of (`;`) in the middle of a line ([see 3.3.2](#r3.2.2)).
    There should be no whitespaces at the end of a line.
    The only scenario where there should be no space after a colon is when the colon is used in the annotation to specify a use-site target (for example, `@param:JsonProperty`).
    There should be no spaces before `,` , `:` and `;`.

    **Exceptions** for spaces and colons:

    - When `:` is used to separate a type and a supertype, including an anonymous object (after object keyword)
    - When delegating to a superclass constructor or different constructor of the same class

**Valid example:**
```kotlin
  abstract class Foo<out T : Any> : IFoo { }

  class FooImpl : Foo() {
      constructor(x: String) : this(x) { /*...*/ }

      val x = object : IFoo { /*...*/ }
  }
```

6. There should be *only one space* between the identifier and its type: `list: List<String>`
If the type is nullable, there should be no space before `?`.

7. When using `[]` operator (`get/set`) there should be **no** spaces between identifier and `[` : `someList[0]`.

8. There should be no space between a method or constructor name (both at declaration and at call site) and a parenthesis:
   `foo() {}`. Note that this sub-rule is related only to spaces; the rules for whitespaces are described in [see 3.6.2](#r3.6.2).
    This rule does not prohibit, for example, the following code:
```kotlin
fun foo
(
    a: String
)
```

9. Never put a space after `(`, `[`, `<` (when used as a bracket in templates) or before `)`, `]`, `>` (when used as a bracket in templates).

10. There should be no spaces between a prefix/postfix operator (like `!!` or `++`) and its operand.

#### <a name="r3.8.2"></a> 3.8.2: No spaces for horizontal alignment

*Horizontal alignment* refers to aligning code blocks by adding space to the code. Horizontal alignment should not be used because:

- When modifying code, it takes much time for new developers to format, support, and fix alignment issues.
- Long identifier names will break the alignment and lead to less presentable code.
- There are more disadvantages than advantages in alignment. To reduce maintenance costs, misalignment (???) is the best choice.

Recommendation: Alignment only looks suitable for `enum class`, where it can be used in table format to improve code readability:
```kotlin
enum class Warnings(private val id: Int, private val canBeAutoCorrected: Boolean, private val warn: String) : Rule {
    PACKAGE_NAME_MISSING         (1, true,  "no package name declared in a file"),
    PACKAGE_NAME_INCORRECT_CASE  (2, true,  "package name should be completely in a lower case"),
    PACKAGE_NAME_INCORRECT_PREFIX(3, false, "package name should start from the company's domain")
    ;
}
```

**Valid example:**
 ```kotlin
 private val nr: Int // no alignment, but looks fine
 private var color: Color // no alignment
 ```

**Invalid example**:
 ```kotlin
 private val    nr: Int    // aligned comment with extra spaces
 private val color: Color  // alignment for a comment and alignment for identifier name
 ```

<!-- =============================================================================== -->
### <a name="c3.9"></a> 3.9 Enumerations
Enum values are separated by a comma and line break, with ';' placed on the new line.

1) The comma and line break characters separate enum values. Put `;` on the new line:
```kotlin
enum class Warnings {
    A,
    B,
    C,
    ;
}
```

This will help to resolve conflicts and reduce the number of conflicts during merging pull requests.
Also, use [trailing comma](https://kotlinlang.org/docs/reference/whatsnew14.html#trailing-comma).

2) If the enum is simple (no properties, methods, and comments inside), you can declare it in a single line:
```kotlin
enum class Suit { CLUBS, HEARTS, SPADES, DIAMONDS }
```

3) Enum classes take preference (if it is possible to use it). For example, instead of two boolean properties:

```kotlin
val isCelsius = true
val isFahrenheit = false
```

use enum class:

```kotlin
enum class TemperatureScale { CELSIUS, FAHRENHEIT }
```

- The variable value only changes within a fixed range and is defined with the enum type.
- Avoid comparison with magic numbers of `-1, 0, and 1`; use enums instead.

```kotlin
enum class ComparisonResult {
    ORDERED_ASCENDING,
    ORDERED_SAME,
    ORDERED_DESCENDING,
    ;
}
```

<!-- =============================================================================== -->
### <a name="c3.10"></a> 3.10 Variable declaration
This section describes rules for the declaration of variables.
#### <a name="r3.10.1"></a> 3.10.1 Declare one variable per line

Each property or variable must be declared on a separate line.

**Invalid example**:
```kotlin
val n1: Int; val n2: Int
```

#### <a name="r3.10.2"></a> 3.10.2 Variables should be declared near the line where they are first used
Declare local variables close to the point where they are first used to minimize their scope. This will also increase the readability of the code.
Local variables are usually initialized during their declaration or immediately after.
The member fields of the class should be declared collectively (see [Rule 3.1.2](#r3.1.2) for details on the class structure).

<!-- =============================================================================== -->
### <a name="c3.11"></a> 3.11 'When' expression

The `when` statement must have an 'else' branch unless the condition variable is enumerated or a sealed type.
Each `when` statement should contain an `else` statement group, even if it does not contain any code.

**Exception:** If 'when' statement of the `enum or sealed` type contains all enum values, there is no need to have an "else" branch.
The compiler can issue a warning when it is missing.

<!-- =============================================================================== -->
### <a name="c3.12"></a> 3.12 Annotations

Each annotation applied to a class, method or constructor should be placed on its own line. Consider the following examples:
1. Annotations applied to the class, method or constructor are placed on separate lines (one annotation per line).

**Valid example**:
```kotlin
@MustBeDocumented
@CustomAnnotation
fun getNameIfPresent() { /* ... */ }
```

2. A single annotation should be on the same line as the code it is annotating.

**Valid example**:
```kotlin
@CustomAnnotation class Foo {}
```

3. Multiple annotations applied to a field or property can appear on the same line as the corresponding field.

**Valid example**:
```kotlin
@MustBeDocumented @CustomAnnotation val loader: DataLoader
```

<!-- =============================================================================== -->
### <a name="c3.13"></a> 3.13 Block comments

Block comments should be placed at the same indentation level as the surrounding code. See examples below.

**Valid example**:

 ```kotlin
class SomeClass {
     /*
      * This is
      * okay
      */
      fun foo() {}
}
 ```

**Note**: Use `/*...*/` block comments to enable automatic formatting by IDEs.

<!-- =============================================================================== -->
### <a name="c3.14"></a> 3.14 Modifiers and constant values
This section contains recommendations regarding modifiers and constant values.
#### <a name="r3.14.1"></a> 3.14.1 Declaration with multiple modifiers
If a declaration has multiple modifiers, always follow the proper sequence.
**Valid sequence:**

```kotlin
public / internal / protected / private
expect / actual
final / open / abstract / sealed / const
external
override
lateinit
tailrec
crossinline
vararg
suspend
inner
out
enum / annotation
companion
inline / noinline
reified
infix
operator
data
```

#### <a name="r3.14.2"></a> 3.14.2: Separate long numerical values with an underscore
An underscore character should separate long numerical values.
**Note:** Using underscores simplifies reading and helps to find errors in numeric constants.
```kotlin
val oneMillion = 1_000_000
val creditCardNumber = 1234_5678_9012_3456L
val socialSecurityNumber = 999_99_9999L
val hexBytes = 0xFF_EC_DE_5E
val bytes = 0b11010010_01101001_10010100_10010010
```
#### <a name="r3.14.3"></a> 3.14.3: Magic number
Prefer defining constants with clear names describing what the magic number means.
**Valid example**:
```kotlin
class Person() {
    fun isAdult(age: Int): Boolean = age >= majority

    companion object {
        private const val majority = 18
    }
}
```
**Invalid example**:
```kotlin
class Person() {
    fun isAdult(age: Int): Boolean = age >= 18
}
```

<!-- =============================================================================== -->
### <a name="c3.15"></a> 3.15 Strings
This section describes the general rules of using strings.

#### <a name="r3.15.1"></a> 3.15.1 Concatenation of Strings
String concatenation is prohibited if the string can fit on one line. Use raw strings and string templates instead. Kotlin has significantly improved the use of Strings:
[String templates](https://kotlinlang.org/docs/reference/basic-types.html#string-templates), [Raw strings](https://kotlinlang.org/docs/reference/basic-types.html#string-literals).
Therefore, compared to using explicit concatenation, code looks much better when proper Kotlin strings are used for short lines, and you do not need to split them with newline characters.

**Invalid example**:
```kotlin
val myStr = "Super string"
val value = myStr + " concatenated"
```

**Valid example**:
```kotlin
val myStr = "Super string"
val value = "$myStr concatenated"
```

#### <a name="r3.15.2"></a> 3.15.2 String template format
**Redundant curly braces in string templates**

If there is only one variable in a string template, there is no need to use such a template. Use this variable directly.
**Invalid example**:
```kotlin
val someString = "${myArgument} ${myArgument.foo()}"
```

**Valid example**:
```kotlin
val someString = "$myArgument ${myArgument.foo()}"
```

**Redundant string template**

In case a string template contains only one variable - there is no need to use the string template. Use this variable directly.

**Invalid example**:
```kotlin
val someString = "$myArgument"
```

**Valid example**:
```kotlin
val someString = myArgument
```

<!-- =============================================================================== -->
### <a name="c3.16"></a> 3.16 Conditional Statements
This section describes the general rules related to the сonditional statements.

#### <a name="r3.16.1"></a> 3.16.1 Collapsing redundant nested if-statements
The nested if-statements, when possible, should be collapsed into a single one
by concatenating their conditions with the infix operator &&.

This improves the readability by reducing the number of the nested language constructs.

#### Simple collapse

**Invalid example**:
```kotlin
if (cond1) {
    if (cond2) {
        doSomething()
    }
}
```

**Valid example**:
```kotlin
if (cond1 && cond2) {
    doSomething()
}
```

#### Compound conditions

**Invalid example**:
```kotlin
if (cond1) {
    if (cond2 || cond3) {
        doSomething()
    }
}
```

**Valid example**:
```kotlin
if (cond1 && (cond2 || cond3)) {
    doSomething()
}
```
#### <a name="r3.16.2"></a> 3.16.2 Too complex conditions
Too complex conditions should be simplified according to boolean algebra rules, if it is possible.
The following rules are considered when simplifying an expression:
* boolean literals are removed (e.g. `foo() || false` -> `foo()`)
* double negation is removed (e.g. `!(!a)` -> `a`)
* expression with the same variable are simplified (e.g. `a && b && a` -> `a && b`)
* remove expression from disjunction, if they are subset of other expression (e.g. `a || (a && b)` -> `a`)
* remove expression from conjunction, if they are more broad than other expression (e.g. `a && (a || b)` -> `a`)
* de Morgan's rule (negation is moved inside parentheses, i.e. `!(a || b)` -> `!a && !b`)

**Valid example**
```kotlin
if (condition1 && condition2) {
    foo()
}
```

**Invalid example**
```kotlin
if (condition1 && condition2 && condition1) {
    foo()
}
```
# <a name="c4"></a> 4. Variables and types
This section is dedicated to the rules and recommendations for using variables and types in your code.
<!-- =============================================================================== -->
### <a name="c4.1"></a> 4.1 Variables
The rules of using variables are explained in the below topics.
#### <a name="r4.1.1"></a> 4.1.1 Do not use Float and Double types when accurate calculations are needed
Floating-point numbers provide a good approximation over a wide range of values, but they cannot produce accurate results in some cases.
Binary floating-point numbers are unsuitable for precise calculations because it is impossible to represent 0.1 or any other negative power of 10 in a `binary representation` with a finite length.

The following code example seems to be obvious:
```kotlin
    val myValue = 2.0 - 1.1
    println(myValue)
```

However, it will print the following value: `0.8999999999999999`

Therefore, for precise calculations (for example, in finance or exact sciences), using such types as `Int`, `Long`, `BigDecimal`are recommended.
The `BigDecimal` type should serve as a good choice.

**Invalid example**:
Float values containing more than six or seven decimal numbers will be rounded.
 ```kotlin
 val eFloat = 2.7182818284f // Float, will be rounded to 2.7182817
 ```

**Valid example**: (when precise calculations are needed):
 ```kotlin
    val income = BigDecimal("2.0")
    val expense = BigDecimal("1.1")
    println(income.subtract(expense)) // you will obtain 0.9 here
 ```

#### <a name="r4.1.2"></a> 4.1.2: Comparing numeric float type values
Numeric float type values should not be directly compared with the equality operator (==) or other methods, such as `compareTo()` and `equals()`. Since floating-point numbers involve precision problems in computer representation, it is better to use `BigDecimal` as recommended in [Rule 4.1.1](#r4.1.1) to make accurate computations and comparisons. The following code describes these problems.

**Invalid example**:
 ```kotlin
val f1 = 1.0f - 0.9f
val f2 = 0.9f - 0.8f
if (f1 == f2) {
    println("Expected to enter here")
} else {
    println("But this block will be reached")
}

val flt1 = f1;
val flt2 = f2;
if (flt1.equals(flt2)) {
    println("Expected to enter here")
} else {
    println("But this block will be reached")
}
 ```

**Valid example**:

```kotlin
val foo = 1.03f
val bar = 0.42f
if (abs(foo - bar) > 1e-6f) {
    println("Ok")
} else {
    println("Not")
}
```

#### <a name="r4.1.3"></a> 4.1.3 Try to use 'val' instead of 'var' for variable declaration [SAY_NO_TO_VAR]

Variables with the `val` modifier are immutable (read-only).
Using `val` variables instead of `var` variables increases code robustness and readability.
This is because `var` variables can be reassigned several times in the business logic.
However, in some scenarios with loops or accumulators, only `var`s are permitted.

<!-- =============================================================================== -->
### <a name="c4.2"></a> 4.2 Types
This section provides recommendations for using types.
#### <a name="r4.2.1"></a> 4.2.1: Use Contracts and smart cast as much as possible

The Kotlin compiler has introduced [Smart Casts](https://kotlinlang.org/docs/reference/typecasts.html#smart-casts) that help reduce the size of code.

**Invalid example**:
```kotlin
    if (x is String) {
        print((x as String).length) // x was already automatically cast to String - no need to use 'as' keyword here
    }
```

**Valid example**:
```kotlin
    if (x is String) {
        print(x.length) // x was already automatically cast to String - no need to use 'as' keyword here
    }
```

Also, Kotlin 1.3 introduced [Contracts](https://kotlinlang.org/docs/reference/whatsnew13.html#contracts) that provide enhanced logic for smart-cast.
Contracts are used and are very stable in `stdlib`, for example:


```kotlin
fun bar(x: String?) {
    if (!x.isNullOrEmpty()) {
        println("length of '$x' is ${x.length}") // smartcasted to not-null
    }
}
```

Smart cast and contracts are a better choice because they reduce boilerplate code and features forced type conversion.

**Invalid example**:
```kotlin
fun String?.isNotNull(): Boolean = this != null

fun foo(s: String?) {
    if (s.isNotNull()) s!!.length // No smartcast here and !! operator is used
}
```

**Valid example**:
```kotlin
fun foo(s: String?) {
    if (s.isNotNull()) s.length // We have used a method with contract from stdlib that helped compiler to execute smart cast
}
```

#### <a name="r4.2.2"></a> 4.2.2: Try to use type alias to represent types making code more readable

Type aliases provide alternative names for existing types.
If the type name is too long, you can replace it with a shorter name, which helps to shorten long generic types.
For example, code looks much more readable if you introduce a `typealias` instead of a long chain of nested generic types.
We recommend using a `typealias` if the type contains **more than two** nested generic types and is longer than **25 chars**.

**Invalid example**:
```kotlin
val b: MutableMap<String, MutableList<String>>
```

**Valid example**:
```kotlin
typealias FileTable = MutableMap<String, MutableList<String>>
val b: FileTable
```

You can also provide additional aliases for function (lambda-like) types:
```kotlin
typealias MyHandler = (Int, String, Any) -> Unit

typealias Predicate<T> = (T) -> Boolean
```

<!-- =============================================================================== -->
### <a name="c4.3"></a> 4.3 Null safety and variable declarations
Kotlin is declared as a null-safe programming language. However, to achieve compatibility with Java, it still supports nullable types.

#### <a name="r4.3.1"></a> 4.3.1: Avoid declaring variables with nullable types, especially from Kotlin stdlib
To avoid `NullPointerException` and help the compiler prevent Null Pointer Exceptions, avoid using nullable types (with `?` symbol).

**Invalid example**:
```kotlin
val a: Int? = 0
```

**Valid example**:
```kotlin
val a: Int = 0
```

Nevertheless, when using Java libraries extensively, you have to use nullable types and enrich the code with `!!` and `?` symbols.
Avoid using nullable types for Kotlin stdlib (declared in [official documentation](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/)).
Try to use initializers for empty collections. For example, if you want to initialize a list instead of `null`, use `emptyList()`.

**Invalid example**:
```kotlin
val a: List<Int>? = null
```

**Valid example**:
```kotlin
val a: List<Int> = emptyList()
```

#### <a name="r4.3.2"></a> 4.3.2: Variables of generic types should have an explicit type declaration
Like in Java, classes in Kotlin may have type parameters. To create an instance of such a class, we typically need to provide type arguments:

```kotlin
val myVariable: Map<Int, String> = emptyMap<Int, String>()
```

However, the compiler can inherit type parameters from the r-value (value assigned to a variable). Therefore, it will not force users to declare the type explicitly.
These declarations are not recommended because programmers would need to find the return value and understand the variable type by looking at the method.

**Invalid example**:
```kotlin
val myVariable = emptyMap<Int, String>()
```

**Valid example**:
```kotlin
val myVariable: Map<Int, String> = emptyMap()
```

#### <a name="r4.3.3"></a> 4.3.3 Null-safety

Try to avoid explicit null checks (explicit comparison with `null`)
Kotlin is declared as [Null-safe](https://kotlinlang.org/docs/reference/null-safety.html) language.
However, Kotlin architects wanted Kotlin to be fully compatible with Java; that's why the `null` keyword was also introduced in Kotlin.

There are several code-structures that can be used in Kotlin to avoid null-checks. For example: `?:`,  `.let {}`, `.also {}`, e.t.c

**Invalid example:**
```kotlin
// example 1
var myVar: Int? = null
if (myVar == null) {
    println("null")
    return
}

// example 2
if (myVar != null) {
    println("not null")
    return
}

// example 3
val anotherVal = if (myVar != null) {
                     println("not null")
                     1
                 } else {
                     2
                 }
// example 4
if (myVar == null) {
    println("null")
} else {
    println("not null")
}
```

**Valid example:**
```kotlin
// example 1
var myVar: Int? = null
myVar?: run {
    println("null")
    return
}

// example 2
myVar?.let {
    println("not null")
    return
}

// example 3
val anotherVal = myVar?.also {
                     println("not null")
                     1
                 } ?: 2

// example 4
myVar?.let {
    println("not null")
} ?: run { println("null") }
```

**Exceptions:**

In the case of complex expressions, such as multiple `else-if` structures or long conditional statements, there is common sense to use explicit comparison with `null`.

**Valid examples:**

```kotlin
if (myVar != null) {
    println("not null")
} else if (anotherCondition) {
    println("Other condition")
}
```

```kotlin
if (myVar == null || otherValue == 5 && isValid) {}
```

Please also note, that instead of using `require(a != null)` with a not null check - you should use a special Kotlin function called `requireNotNull(a)`.

# <a name="c5"></a> 5. Functions
This section describes the rules of using functions in your code.
<!-- =============================================================================== -->
### <a name="c5.1"></a> 5.1 Function design
Developers can write clean code by gaining knowledge of how to build design patterns and avoid code smells.
You should utilize this approach, along with functional style, when writing Kotlin code.
The concepts behind functional style are as follows:
Functions are the smallest unit of combinable and reusable code.
They should have clean logic, **high cohesion**, and **low coupling** to organize the code effectively.
The code in functions should be simple and not conceal the author's original intentions.

Additionally, it should have a clean abstraction, and control statements should be used straightforwardly.
The side effects (code that does not affect a function's return value but affects global/object instance variables) should not be used for state changes of an object.
The only exceptions to this are state machines.

Kotlin is [designed](https://www.slideshare.net/abreslav/whos-more-functional-kotlin-groovy-scala-or-java) to support and encourage functional programming, featuring the corresponding built-in mechanisms.
Also, it supports standard collections and sequences feature methods that enable functional programming (for example, `apply`, `with`, `let`, and `run`), Kotlin Higher-Order functions, function types, lambdas, and default function arguments.
As [previously discussed](#r4.1.3), Kotlin supports and encourages the use of immutable types, which in turn motivates programmers to write pure functions that avoid side effects and have a corresponding output for specific input.
The pipeline data flow for the pure function comprises a functional paradigm. It is easy to implement concurrent programming when you have chains of function calls, where each step features the following characteristics:
1.	Simplicity
2.	Verifiability
3.	Testability
4.	Replaceability
5.	Pluggability
6.	Extensibility
7.	Immutable results

There can be only one side effect in this data stream, which can be placed only at the end of the execution queue.

#### <a name="r5.1.1"></a> 5.1.1 Avoid functions that are too long

The function should be displayable on one screen and only implement one certain logic.
If a function is too long, it often means complex and could be split or simplified. Functions should consist of 30 lines (non-empty and non-comment) in total.

**Exception:** Some functions that implement complex algorithms may exceed 30 lines due to aggregation and comprehensiveness.
Linter warnings for such functions **can be suppressed**.

Even if a long function works well, new problems or bugs may appear due to the function's complex logic once it is modified by someone else.
Therefore, it is recommended to split such functions into several separate and shorter functions that are easier to manage.
This approach will enable other programmers to read and modify the code properly.
#### <a name="r5.1.2"></a> 5.1.2 Avoid deep nesting of function code blocks, limiting to four levels

The nesting depth of a function's code block is the depth of mutual inclusion between the code control blocks in the function (for example: if, for, while, and when).
Each nesting level will increase the amount of effort needed to read the code because you need to remember the current "stack" (for example, entering conditional statements and loops).
**Exception:** The nesting levels of the lambda expressions, local classes, and anonymous classes in functions are calculated based on the innermost function. The nesting levels of enclosing methods are not accumulated.
Functional decomposition should be implemented to avoid confusion for the developer who reads the code.
This will help the reader switch between contexts.

#### <a name="r5.1.3"></a> 5.1.3 Avoid using nested functions
Nested functions create a more complex function context, thereby confusing readers.
With nested functions, the visibility context may not be evident to the code reader.

**Invalid example**:
```kotlin
fun foo() {
    fun nested():String {
        return "String from nested function"
    }
    println("Nested Output: ${nested()}")
}
```
#### <a name="r5.1.4"></a> 5.1.4 Negated function calls
Don't use negated function calls if it can be replaced with negated version of this function

**Invalid example**:
```kotlin
fun foo() {
    val list = listOf(1, 2, 3)

    if (!list.isEmpty()) {
        // Some cool logic
    }
}
```

**Valid example**:
```kotlin
fun foo() {
    val list = listOf(1, 2, 3)

    if (list.isNotEmpty()) {
        // Some cool logic
    }
}
```

<!-- =============================================================================== -->
### <a name="c5.2"></a> 5.2 Function arguments
The rules for using function arguments are described in the below topics.
#### <a name="r5.2.1"></a> 5.2.1 The lambda parameter of the function should be placed at the end of the argument list

With such notation, it is easier to use curly brackets, leading to better code readability.

**Valid example**:
```kotlin
// declaration
fun myFoo(someArg: Int, myLambda: () -> Unit) {
// ...
}

// usage
myFoo(1) {
println("hey")
}
```

#### <a name="r5.2.2"></a> 5.2.2 Number of function parameters should be limited to five

A long argument list is a [code smell](https://en.wikipedia.org/wiki/Code_smell) that leads to less reliable code.
It is recommended to reduce the number of parameters. Having **more than five** parameters leads to difficulties in maintenance and conflicts merging.
If parameter groups appear in different functions multiple times, these parameters are closely related and can be encapsulated into a single Data Class.
It is recommended that you use Data Classes and Maps to unify these function arguments.

#### <a name="r5.2.3"></a> 5.2.3 Use default values for function arguments instead of overloading them
In Java, default values for function arguments are prohibited. That is why the function should be overloaded when you need to create a function with fewer arguments.
In Kotlin, you can use default arguments instead.

**Invalid example**:
```kotlin
private fun foo(arg: Int) {
    // ...
}

private fun foo() {
    // ...
}
```

**Valid example**:
```kotlin
 private fun foo(arg: Int = 0) {
     // ...
 }
```
#### <a name="r5.2.4"></a> 5.2.4 Synchronizing code inside asynchronous code
Try to avoid using `runBlocking` in asynchronous code

**Invalid example**:
```kotlin
GlobalScope.async {
    runBlocking {
        count++
    }
}
```
#### <a name="r5.2.5"></a> 5.2.5 Long lambdas should have explicit parameters
The lambda without parameters shouldn't be too long.
If a lambda is too long, it can confuse the user. Lambda without parameters should consist of 10 lines (non-empty and non-comment) in total.

#### <a name="r5.2.6"></a> 5.2.6 Avoid using unnecessary, custom label
Expressions with unnecessary, custom labels generally increase complexity and worsen the maintainability of the code.

**Invalid example**:
```kotlin
run lab@ {
    list.forEach {
        return@lab
    }
}
```

**Valid example**:
```kotlin
list.forEachIndexed { index, i ->
    return@forEachIndexed
}

lab@ for(i: Int in q) {
    for (j: Int in q) {
        println(i)
        break@lab
    }
}
```
# <a name="c6"></a> 6. Classes, interfaces, and extension functions
<!-- =============================================================================== -->
### <a name="c6.1"></a> 6.1 Classes
This section describes the rules of denoting classes in your code.
#### <a name="r6.1.1"></a> 6.1.1  Denoting a class with a single constructor
When a class has a single constructor, it should be defined as a primary constructor in the declaration of the class. If the class contains only one explicit constructor, it should be converted to a primary constructor.

**Invalid example**:
```kotlin
class Test {
    var a: Int
    constructor(a: Int) {
        this.a = a
    }
}
```

**Valid example**:
```kotlin
class Test(var a: Int) {
    // ...
}

// in case of any annotations or modifiers used on a constructor:
class Test private constructor(var a: Int) {
    // ...
}
```

#### <a name="r6.1.2"></a> 6.1.2 Prefer data classes instead of classes without any functional logic
Some people say that the data class is a code smell. However, if you need to use it (which makes your code more simple), you can utilize the Kotlin `data class`. The main purpose of this class is to hold data,
but also `data class` will automatically generate several useful methods:
- equals()/hashCode() pair;
- toString()
- componentN() functions corresponding to the properties in their order of declaration;
- copy() function

Therefore, instead of using `normal` classes:

```kotlin
class Test {
    var a: Int = 0
        get() = field
        set(value: Int) { field = value}
}

class Test {
    var a: Int = 0
    var b: Int = 0

    constructor(a:Int, b: Int) {
        this.a = a
        this.b = b
    }
}

// or
class Test(var a: Int = 0, var b: Int = 0)

// or
class Test() {
    var a: Int = 0
    var b: Int = 0
}
```

**prefer data classes:**
```kotlin
data class Test1(var a: Int = 0, var b: Int = 0)
```

**Exception 1**: Note that data classes cannot be abstract, open, sealed, or inner; that is why these types of classes cannot be changed to a data class.

**Exception 2**: No need to convert a class to a data class if this class extends some other class or implements an interface.

#### <a name="r6.1.3"></a> 6.1.3 Do not use the primary constructor if it is empty or useless
The primary constructor is a part of the class header; it is placed after the class name and type parameters (optional) but can be omitted if it is not used.

**Invalid example**:
```kotlin
// simple case that does not need a primary constructor
class Test() {
    var a: Int = 0
    var b: Int = 0
}

// empty primary constructor is not needed here
// it can be replaced with a primary contructor with one argument or removed
class Test() {
    var a  = "Property"

    init {
        println("some init")
    }

    constructor(a: String): this() {
        this.a = a
    }
}
```

**Valid example**:
```kotlin
// the good example here is a data class; this example also shows that you should get rid of braces for the primary constructor
class Test {
    var a: Int = 0
    var b: Int = 0
}
```

#### <a name="r6.1.4"></a> 6.1.4 Do not use redundant init blocks in your class
Several init blocks are redundant and generally should not be used in your class. The primary constructor cannot contain any code. That is why Kotlin has introduced `init` blocks.
These blocks store the code to be run during the class initialization.
Kotlin allows writing multiple initialization blocks executed in the same order as they appear in the class body.
Even when you follow (rule 3.2)[#r3.2], this makes your code less readable as the programmer needs to keep in mind all init blocks and trace the execution of the code.
Therefore, you should try to use a single `init` block to reduce the code's complexity. If you need to do some logging or make some calculations before the class property assignment, you can use powerful functional programming. This will reduce the possibility of the error if your `init` blocks' order is accidentally changed and
make the code logic more coupled. It is always enough to use one `init` block to implement your idea in Kotlin.

**Invalid example**:
```kotlin
class YourClass(var name: String) {
    init {
        println("First initializer block that prints ${name}")
    }

    val property = "Property: ${name.length}".also(::println)

    init {
        println("Second initializer block that prints ${name.length}")
    }
}
```

**Valid example**:
```kotlin
class YourClass(var name: String) {
    init {
        println("First initializer block that prints ${name}")
    }

    val property = "Property: ${name.length}".also { prop ->
        println(prop)
        println("Second initializer block that prints ${name.length}")
    }
}
```

The `init` block was not added to Kotlin to help you initialize your properties; it is needed for more complex tasks.
Therefore if the `init` block contains only assignments of variables - move it directly to properties to be correctly initialized near the declaration.
In some cases, this rule can be in clash with [6.1.1](#r6.1.1), but that should not stop you.

**Invalid example**:
```kotlin
class A(baseUrl: String) {
    private val customUrl: String
    init {
        customUrl = "$baseUrl/myUrl"
    }
}
```

**Valid example**:
```kotlin
class A(baseUrl: String) {
    private val customUrl = "$baseUrl/myUrl"
}
```

#### <a name="r6.1.5"></a> 6.1.5 Explicit supertype qualification
The explicit supertype qualification should not be used if there is no clash between called methods. This rule is applicable to both interfaces and classes.

**Invalid example**:
```kotlin
open class Rectangle {
    open fun draw() { /* ... */ }
}

class Square() : Rectangle() {
    override fun draw() {
        super<Rectangle>.draw() // no need in super<Rectangle> here
    }
}
```

#### <a name="r6.1.6"></a> 6.1.6 Abstract class should have at least one abstract method
Abstract classes are used to force a developer to implement some of its parts in their inheritors.
When the abstract class has no abstract methods, it was set `abstract` incorrectly and can be converted to a regular class.

**Invalid example**:
```kotlin
abstract class NotAbstract {
    fun foo() {}

    fun test() {}
}
```

**Valid example**:
```kotlin
abstract class NotAbstract {
    abstract fun foo()

    fun test() {}
}

// OR
class NotAbstract {
    fun foo() {}

    fun test() {}
}
```


#### <a name="r6.1.7"></a> 6.1.7 When using the "implicit backing property" scheme, the name of real and back property should be the same
Kotlin has a mechanism of [backing properties](https://kotlinlang.org/docs/reference/properties.html#backing-properties).
In some cases, implicit backing is not enough and it should be done explicitly:
```kotlin
private var _table: Map<String, Int>? = null
val table: Map<String, Int>
    get() {
        if (_table == null) {
            _table = HashMap() // Type parameters are inferred
        }
        return _table ?: throw AssertionError("Set to null by another thread")
    }
```

In this case, the name of the backing property (`_table`) should be the same as the name of the real property (`table`) but should have an underscore (`_`) prefix.
It is one of the exceptions from the [identifier names rule](#r1.2)

#### <a name="r6.1.8"></a> 6.1.8 Avoid using custom getters and setters
Kotlin has a perfect mechanism of [properties](https://kotlinlang.org/docs/reference/properties.html#properties-and-fields).
Kotlin compiler automatically generates `get` and `set` methods for properties and can override them.

**Invalid example:**
```kotlin
class A {
    var size: Int = 0
        set(value) {
            println("Side effect")
            field = value
        }
        // user of this class does not expect calling A.size receive size * 2
        get() = field * 2
}
```

From the callee code, these methods look like access to this property: `A().isEmpty = true` for setter and `A().isEmpty` for getter.

However, when `get` and `set` are overridden, it  isn't very clear for a developer who uses this particular class.
The developer expects to get the property value but receives some unknown value and some extra side-effect hidden by the custom getter/setter.
Use extra functions instead to avoid confusion.



**Valid example**:
```kotlin
class A {
    var size: Int = 0
    fun initSize(value: Int) {
        // some custom logic
    }

    // this will not confuse developer and he will get exactly what he expects
    fun goodNameThatDescribesThisGetter() = this.size * 2
}
```

**Exception:** `Private setters` are only exceptions that are not prohibited by this rule.

#### <a name="r6.1.9"></a> 6.1.9 Never use the name of a variable in the custom getter or setter (possible_bug)
If you ignored [recommendation 6.1.8](#r6.1.8), be careful with using the name of the property in your custom getter/setter
as it can accidentally cause a recursive call and a `StackOverflow Error`. Use the `field` keyword instead.

**Invalid example (very bad)**:
```kotlin
var isEmpty: Boolean
    set(value) {
        println("Side effect")
        isEmpty = value
    }
    get() = isEmpty
```

#### <a name="r6.1.10"></a> 6.1.10 No trivial getters and setters are allowed in the code
In Java, trivial getters - are the getters that are just returning the field value.
Trivial setters - are merely setting the field with a value without any transformation.
However, in Kotlin, trivial getters/setters are generated by default. There is no need to use it explicitly for all types of data structures in Kotlin.

**Invalid example**:
```kotlin
class A {
    var a: Int = 0
    get() = field
    set(value: Int) { field = value }

    //
}
```

**Valid example**:
```kotlin
class A {
    var a: Int = 0
    get() = field
    set(value: Int) { field = value }

    //
}
```

#### <a name="r6.1.11"></a> 6.1.11 Use 'apply' for grouping object initialization
In Java, before functional programming became popular, many classes from common libraries used the configuration paradigm.
To use these classes, you had to create an object with the constructor with 0-2 arguments and set the fields needed to run the object.
In Kotlin, to reduce the number of dummy code line and to group objects [`apply` extension](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/apply.html) was added:

**Invalid example**:
```kotlin
class HttpClient(var name: String) {
    var url: String = ""
    var port: String = ""
    var timeout = 0

    fun doRequest() {}
}

fun main() {
    val httpClient = HttpClient("myConnection")
    httpClient.url = "http://example.com"
    httpClient.port = "8080"
    httpClient.timeout = 100

    httpCLient.doRequest()
}

```

**Valid example**:
```kotlin
class HttpClient(var name: String) {
    var url: String = ""
    var port: String = ""
    var timeout = 0

    fun doRequest() {}
}

fun main() {
    val httpClient = HttpClient("myConnection")
            .apply {
                url = "http://example.com"
                port = "8080"
                timeout = 100
            }
    httpClient.doRequest()
}
```

### <a name="r6.1.12"></a> 6.1.12 Prefer Inline classes when a class has a single property
If a class has only one immutable property, then it can be converted to the inline class.

Sometimes it is necessary for business logic to create a wrapper around some type. However, it introduces runtime overhead due to additional heap allocations. Moreover, if the wrapped type is primitive, the performance hit is terrible, because primitive types are usually heavily optimized by the runtime, while their wrappers don't get any special treatment.

**Invalid example**:
```kotlin
class Password {
    val value: String
}
```

**Valid example**:
```kotlin
inline class Password(val value: String)
```

<!-- =============================================================================== -->
### <a name="c6.2"></a>6.2 Extension functions
This section describes the rules of using extension functions in your code.

[Extension functions](https://kotlinlang.org/docs/reference/extensions.html) is a killer-feature in Kotlin.
It gives you a chance to extend classes that were already implemented in external libraries and helps you to make classes less heavy.
Extension functions are resolved statically.

#### <a name="r6.2.1"></a> 6.2.1 Use extension functions for making logic of classes less coupled
It is recommended that for classes, the non-tightly coupled functions, which are rarely used in the class, should be implemented as extension functions where possible.
They should be implemented in the same class/file where they are used. This is a non-deterministic rule, so the code cannot be checked or fixed automatically by a static analyzer.

#### <a name="r6.2.2"></a> 6.2.2 No extension functions with the same name and signature if they extend base and inheritor classes (possible_bug)
You should avoid declaring extension functions with the same name and signature if their receivers are base and inheritor classes (possible_bug),
as extension functions are resolved statically. There could be a situation when a developer implements two extension functions: one is for the base class and
another for the inheritor. This can lead to an issue when an incorrect method is used.

**Invalid example**:
```kotlin
open class A
class B: A()

// two extension functions with the same signature
fun A.foo() = "A"
fun B.foo() = "B"

fun printClassName(s: A) { println(s.foo()) }

// this call will run foo() method from the base class A, but
// programmer can expect to run foo() from the class inheritor B
fun main() { printClassName(B()) }
```

#### <a name="r6.2.3"></a> 6.2.3 Don't use extension functions for the class in the same file
You should not use extension functions for the class in the same file, where it is defined.

**Invalid example**:
```kotlin
class SomeClass {

}

fun SomeClass.deleteAllSpaces() {

}
```

#### <a name="r6.2.4"></a> 6.2.4 Use 'lastIndex' in case you need to get latest element of a collection
You should not use property length with operation - 1, you can change this to lastIndex

**Invalid example**:
```kotlin
val A = "name"
val B = A.length - 1
val C = A[A.length - 1]
```

**Valid example**:
```kotlin
val A = "name"
val B = A.lastIndex
val C = A[A.lastIndex]
```



<!-- =============================================================================== -->
### <a name="c6.3"></a> 6.3 Interfaces
An `Interface` in Kotlin can contain declarations of abstract methods, as well as method implementations. What makes them different from abstract classes is that interfaces cannot store state.
They can have properties, but these need to be abstract or to provide accessor implementations.

Kotlin's interfaces can define attributes and functions.
In Kotlin and Java, the interface is the main presentation means of application programming interface (API) design and should take precedence over the use of (abstract) classes.

<!-- =============================================================================== -->
### <a name="c6.4"></a> 6.4 Objects
This section describes the rules of using objects in code.
#### <a name="r6.4.1"></a> 6.4.1 Instead of using utility classes/objects, use extensions
Avoid using utility classes/objects; use extensions instead. As described in [6.2 Extension functions](#c6.2), using extension functions is a powerful method.
This enables you to avoid unnecessary complexity and class/object wrapping and use top-level functions instead.

**Invalid example**:
```kotlin
object StringUtil {
    fun stringInfo(myString: String): Int {
        return myString.count{ "something".contains(it) }
    }
}
StringUtil.stringInfo("myStr")
```

**Valid example**:
```kotlin
fun String.stringInfo(): Int {
    return this.count{ "something".contains(it) }
}

"myStr".stringInfo()
```

#### <a name="r6.4.2"></a> 6.4.2 Objects should be used for Stateless Interfaces
Kotlin’s objects are extremely useful when you need to implement some interface from an external library that does not have any state.
There is no need to use classes for such structures.

**Valid example**:
```
interface I {
    fun foo()
}

object O: I {
    override fun foo() {}
}
```
### <a name="c6.5"></a> 6.5 Kts Files
This section describes general rules for `.kts` files
#### <a name="r6.5.1"></a> 6.5.1 kts files should wrap logic into top-level scope
It is still recommended wrapping logic inside functions and avoid using top-level statements for function calls or wrapping blocks of code
in top-level scope functions like `run`.

**Valid example**:
```
run {
    // some code
}

fun foo() {

}
```
