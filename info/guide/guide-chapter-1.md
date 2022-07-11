# <a name="c1"></a> 1. Naming
It is not always easy to meaningfully and appropriately name variables, functions, classes, and so on. Using meaningful names in programming helps to clearly express the main ideas and functionality of your code and avoid its misinterpretation, unnecessary coding and decoding, "magic" numbers, and inappropriate abbreviations.

Note: Source code files (incl. comments) must be UTF-8 encoded, no exceptions. The ASCII horizontal space (`0x20`, `U+0020`) is the only permitted whitespace character. Tab character (`0x09`, `U+0009`) should never be used for indentation.

<!-- =============================================================================== -->
### <a name="c1.1"></a> 1.1 Identifiers
This section describes the general rules for naming identifiers.
#### <a name="r1.1.1"></a> 1.1.1 Identifiers naming conventions

For identifiers, use the following naming conventions:
1.	All identifiers should use only ASCII letters or digits, and the names should match regular expressions `\w{2,64}`.
Explanation: Each valid identifier name should match the regular expression `\w{2,64}`, which means that the name length is 2 to 64 characters, and the length of the variable name should be proportional to its life range and describe its functionality and responsibility.
Length of names depends on the project. Nevertheless, name lengths of less than 31 characters are generally recommended. Otherwise, a class declaration with generics or inheritance from a superclass can cause line breaking.
No special prefix or suffix should be used in names. The examples of inappropriate names are: name_, mName, s_name, and kName.

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

The only exceptions are function names in `Unit tests.`

5.	Backticks (``) must not be used for identifiers, except for the names of test methods (marked with @Test annotation):
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
- The `i`,`j`,`k` variables used in loops are part of the industry standard. A single character can be used for such variables.
- The `e` variable can be used to catch exceptions in the catch block: `catch (e: Exception) {}`
- The Java community normally does not recommend the use of prefixes. However, when developing the Android code, you can use the "s" and "m" prefixes for static and non-public non-static fields, respectively.
Note that prefixing can also negatively affect the style and the auto-generation of getters and setters.

| Type | Naming style |
| ---- | ---- |
| Interfaces, classes, annotations, enumerated types, and object type names | Camel case, starting with a capital letter. Test classes have a Test suffix. The filename is 'TopClassName'.kt.  |
| Class fields, local variables, methods, and method parameters | Camel case, starting with a low case letter. Test methods can be underlined with '_'; the only exception is [backing properties](#r6.1.7).
| Static constants and enumerated values | Only the uppercase underlined with '_' |
| Generic type variable | Single capital letter, which can be followed by a number, for example: `E, T, U, X, T2` |
| Exceptions | Same as class names, but with a suffix Exception, for example: `AccessException` and `NullPointerException`|

<!-- =============================================================================== -->
### <a name="c1.2"></a> 1.2 Packages

#### <a name="r1.2.1"></a> Rule 1.2.1 Package names dots
Package names are in the lower case and separated by dots. The code developed within your company should start with `your.company.domain.` Numbers are permitted in package names.
Each file should have a `package` directive.
Package names are all written in lowercase, and consecutive words are concatenated (no underscores). Package names should contain both the product or module names and the department (or team) name to prevent conflicts with other teams.  Numbers are not permitted. For example: `org.apache.commons.lang3`, `xxx.yyy.v2`.

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
1.	A class name is usually a noun (or a noun phrase) denoted using the camel case nomenclature, such as the UpperCamelCase. For example: `Character` or `ImmutableList`.
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
Note: Verb are primarily used for the action objects, such as `document.print ()`

f) verb + noun() 

g) The Callback function allows the names that use the preposition + verb format, such as `onCreate()`, `onDestroy()`, `toString()`.

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
Constant names should be in the UPPER case, words separated by an underscore. The general constant naming conventions are listed below:
1. Constants are attributes created with the `const` keyword or top-level/`val` local variables of an object that holds immutable data. In most cases, constants can be identified as a `const val` property from the `object`/`companion object`/file top level. These variables contain fixed constant values that typically should never be changed by programmers. This includes basic types, strings, immutable types, and immutable collections of immutable types. The value is not constant for the object, which state can be changed.
2. Constant names should contain only uppercase letters separated by underscores. They should have a val or const val modifier to make them final explicitly. In most cases, if you need to specify a constant value, then you need to create it with the "const val" modifier. Note that not all `val` variables are constants.
3. Objects with immutable content, such as `Logger` and `Lock`, they can be in the uppercase as constants or have the Camel case as regular variables.
4. Use meaningful constants instead of `magic numbers`. SQL or logging strings should not be treated as magic numbers, nor should they be defined as string constants.
Magic constants, such as `NUM_FIVE = 5` or `NUM_5 = 5`, should not be treated as constants. This is because mistakes will easily be made if they are changed to `NUM_5 = 50` or 55.
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
Non-constant field names should use the Camel case and start with a lowercase letter.
A local variable cannot be treated as constant, even if it is final and immutable. Therefore, it should not use the preceding rules. Names of collection type variables (sets, lists, etc.) should contain plural nouns.
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
However, not all methods returning the Boolean type have this notation.
For Boolean local variables or methods, it is highly recommended that you add non-meaningful prefixes, including is (commonly used by JavaBeans), has, can, should, and must. Modern integrated development environments (IDEs), such as Intellij, can doing this when you generate getters in Java. For Kotlin, this process is even more straightforward as everything is on the byte-code level under the hood.

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
