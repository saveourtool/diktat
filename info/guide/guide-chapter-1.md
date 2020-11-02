### <a name="c1"></a>1 Naming
In programming, it is difficult to meaningfully and appropriately name variables, functions, classes, etc. Good names clearly express the main ideas and functionality of your code, as well as avoid misinterpretation, unnecessary coding and decoding, magic numbers, and inappropriate abbreviations.

### <a name="r1.0.1"></a>Rule 1.0.1: file encoding format must be UTF-8 only
The source file encoding format (including comments) must be UTF-8 only. The ASCII horizontal space character (0x20, that is, space) is the only permitted white space character. Tabs should not be used for indentation.

### <a name="c1.1"></a>Identifiers naming
### <a name="r1.1.1"></a> Rule 1.1.1: Identifiers
1.	All identifiers should use only ASCII letters or digits, and the names should match regular expressions \w{2,64}.
Explanation: Each valid identifier name should match the regular expression \ w {2,64}.
{2,64} means that the name length is 2 to 64 characters, and the length of the variable name should be proportional to its life range, functionality, and responsibility.
Name lengths of less than 31 symbols are generally recommended, but this depends on the project. Otherwise, class declaration with generics or inheritance from a super class can cause line breaking for example. No special prefix or suffix should be used in the names. The following examples are inappropriate: name_, mName, s_name, and kName.

2.	For files, choose names that describe their content. Use camel case (PascalCase) and `.kt` extension.

3.	Typical examples of naming:

| Meaning | Correct |Incorrect|
| ---- | ---- | ---- |
| "XML Http Request" | XmlHttpRequest | XMLHTTPRequest |
| "new customer ID" | newCustomerId | newCustomerID |
| "inner stopwatch" | innerStopwatch | innerStopWatch |
| "supports IPv6 on iOS" | supportsIpv6OnIos | supportsIPv6OnIOS |
| "YouTube importer" | YouTubeImporter | YoutubeImporter |

4.	The usage of (``) and free naming for functions and identifiers are prohibited. For example - not recommended to use:
    
```kotlin
   val `my dummy name-with-minus` = "value" 
```

The only exception can be - is function names in Unit tests.

5.	Backticks (``) should not be used for identifiers, except the names of test methods (marked with @Test annotation):
```kotlin
     @Test fun `my test`() { /*...*/ }
``` 
6.  The following table contains some characters that cause confusion. Be careful when using them as identifiers, or use other names instead.
   
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

 **Exceptions**
 - The i,j,k variables used in loops are part of the industry standard. One symbol can be used for for such variables.
 - The `e` variable can be used to catch exceptions in catch block: `catch (e: Exception) {}`
 - The Java community generally does not recommend the use of prefixes; however, when developing Android code, you can use the s and m prefixes for static and non-public non-static fields, respectively. Note that prefixing can also negatively affect the style together with auto-generation of getters and setters.
 
| Type | Naming style |
| ---- | ---- |
| Interfaces, classes, annotations, enumerated types, and object type names | Camel case starting with a capital letter. Test classes have a Test suffix. The filename is 'TopClassName'.kt.  |
| Class fields, local variables, methods, and method parameters | Camel case starting with a small letter. Test methods may be underlines  with '_'., the only exception is [backing properties](#r6.1.7)
| Static constants and enumerated values | Only uppercase underlined with '_' |
| Generic type variable | Single capital letter, which can be followed by a number, for example: `E, T, U, X, T2` |
| Exceptions | Same as class names, but with a suffix Exception, for example: `AccessException` and `NullPointerException`|

### <a name="c1.2"></a>Packages naming
### <a name="r1.2.1"></a> Rule 1.2.1: Package names are in lower case and separated by dots. Code developed within your company should start with `your.company.domain`, and numbers are permitted in package names.
Package names are all written in lowercase, and consecutive words are simply concatenated together (no underscores). Package names should contain both the product and module names, as well as the department or team name to prevent conflicts with other teams.  Numbers are not permitted. For example: `org.apache.commons.lang3`, `xxx.yyy.v2`.

**Exceptions：** 

- In certain cases, such as open-source projects or commercial cooperation, package names should not start with `your.company.domain`.- In some cases, if the package name starts with a number or other characters, but these characters cannot be used at the beginning of the Java/Kotlin package name, or the package name contains reserved Java keywords, underscores are allowed.
- Underscores are sometimes permitted if the package name starts with a number or other character, which cannot be used at the beginning of the Java/Kotlin package name; or the package name contains reserved Java keywords. For example: org.example.hyphenated_name, int_.example, com.example._123name   
   For example: `org.example.hyphenated_name`,` int_.example`, `com.example._123name`

**Valid example**: 
 ```kotlin
package your.company.domain.mobilecontrol.views
 ```

### <a name="c1.3"></a> Classes, enumerations, interfaces
### <a name="r1.3.1"></a> Rule 1.3.1: Classes, enumerations, interface names use camel case nomenclature
1.	The class name is usually a noun or phrase with a noun using the camel case nomenclature, such as UpperCamelCase. For example: Character or ImmutableList. The name of an interface can also be a noun or phrase with a noun (such as List), or an adjective or phrase with adjectives (such as Readable). Note that verbs should not be used to name classes; however, nouns (such as Customer, WikiPage, and Account) can be used. Try to avoid vague words like Manager and Process.
2.	Test classes start with the name of the class they are testing and end with Test. For example: HashTest or HashIntegrationTest

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

### <a name="c1.4"></a>Functions
### <a name="r1.4.1"></a> Rule 1.4.1: function names should be in camel case

1.	Function names are usually verbs or verb phrases, and use the camel case nomenclature lowerCamelCase. For example: `sendMessage`, `stopProcess`, or `calculateValue`.
The format is as follows:

a) To get, modify, or calculate a certain value: get + non-boolean field(). However, note that getters are automatically generated by the Kotlin compiler for some classes, and the special get syntax is preferred for fields: kotlin private val field: String get() { }.
```kotlin
private val field: String
get() {
}
``` 
Additionally, calling property access syntax is preferred to calling getter directly. (In this case, the Kotlin compiler will automatically call the corresponding getter).

b) `is` + boolean variable name()

c) `set` + field/attribute name(). However, note that the syntax and code generation for Kotlin are completely the same as those described for the getters in point a.

d) `has` + Noun / adjective ()

e) verb()
The verb is mainly used on the object of the action itself, such as `document.print ()`

f) verb + noun() 

g) Callback function allows preposition + verb form naming, such as: `onCreate()`, `onDestroy()`, `toString()`

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

2.	An underscore can be included in the JUnit test function name, and should be a logical component used to separate names. Each logical part is written in lowerCamelCase. For example: a typical pattern _, such as pop_emptyStack

### <a name="c1.5"></a> Constants 
### <a name="r1.5.1"></a> Rule 1.5.1 Constant names should be in UPPER case, words separated by underscore

1.	Constants are attributes created with the const keyword, or top-level/`val` local variables of an object that holds immutable data. In most cases, constants can be identified as a `const val` property from the `object`/`companion object`/file top level. These variables contain a fixed constant value that typically should never be changed by programmers. This includes basic types, strings, immutable types, and immutable collections of immutable types. If an object state can be changed, the value is not a constant.

2. Constant names should contain only uppercase letters separated by underscores. They should have a val or const val modifier to explicitly make them final. In most cases, if you need to specify a constant value, then you need to create it with the "const val" modifier. Note that not all `val` variables are constants.

3. Objects that have immutable content, such as `Logger` and `Lock`, can be in uppercase as constants or have camel case as regular variables.

4. Use meaningful constants instead of `magic numbers`. SQL or logging strings should not be treated as "magic numbers", nor should they be defined as string constants. "Magic constants" like `NUM_FIVE = 5` or `NUM_5 = 5` should not be treated as constants. This is because mistakes will easily be made if they are changed to `NUM_5 = 50` or 55.
Typically, these constants represent business logic values like measures, capacity, scope, location, tax rate, promotional discounts, and power base multiples in algorithms. 
Magic numbers can be avoided through the following methods:
- use library functions and APIs. For example, instead of checking that `size == 0` use `isEmpty()` function. To work with `time` use built-ins from `java.time API`.
- Enumerations can be used to name patterns. Refer to [Recommended usage scenario for enumeration in 3.9](#s3.9)
 
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

### <a name="c1.6"></a> Non-constant fields
### <a name="r1.6.1"></a> Rule 1.6.1: The name of the non-constant field should use camel case and start with a lowercase letter.

A local variable cannot be treated as a constant even if it is final and immutable. Therefore, it should not use the preceding rules. The name of variables with a type from collections (sets, lists, etc.) should contain plural nouns.
For example: `var namesList: List<String>`

Names of non-constant variables should use lower camel case. The name of the final immutable field used to store the singleton object can use the same notation with camel case.

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

### <a name="r1.6.2"></a> Recommendation 1.6.2: Avoid using Boolean variable names with negative meaning.

*Note*: When using a logical operator and name with negative meaning, the code may be difficult to understand, which is referred to as the "double negative". For instance, it is not easy to understand the meaning of !isNotError. The JavaBeans specification automatically generates isXxx() getters for attributes of Boolean classes. However, methods that return Boolean do not all have this notation. For Boolean local variables or methods, it is highly recommended that you add non-meaningful prefixes, including is (which is commonly used by JavaBeans), or has, can, should, must. Modern integrated development environments (IDEs) such as Intellij are already capable of doing this for you when you generate getters in Java. For Kotlin, this process is even easier as everything is on the byte-code level under the hood.

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
