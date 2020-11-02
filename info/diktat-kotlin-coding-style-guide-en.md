
![img](diktat.jpg)

# Content

| Chapter             | Content                                                      |
| ------------------- | ------------------------------------------------------------ |
| [0 Intro](#c0.1)    | [Purpose](#c0.1), [General principles](#c0.2), [Terminology](#c0.3), [Scope](#c0.4), [Exception](#c0.5) |
| [1 Naming](#c1)     | [Identifiers](#c1.1), [Package naming](#c1.2), [Classes, enumeration and interfaces](#c1.3), [Functions](#c1.4), [Constants](#c1.5), [Variables](#c1.6) |
| [2 Comments](#c2)   | [Kdoc](#c2.1), [File header](#c2.2), [Function header comments](#c2.3), [Code comments](#c2.4) |
| [3 General format](#c3)   | [File-related rules](#c3.1), [Indentation](#c3.2), [Empty blocks](#c3.3), [Line width](#c3.4), [Line breaks (newlines)](#c3.5), [Blank lines](#c3.6), [Horizontal alignment](#c3.7), [Enumerations](#c3.8), [Variable declaration](#c3.9), [When expression](#c3.10), [Annotations](#c3.11), [Comment layout](#c3.12), [Modifiers](#c3.13), [Strings](#c3.14)|
| [4 Variables and types](#c4) | [Variables](#c4.1), [Types](#c4.2), [Null safety and variable declarations](#4.3)|
| [5 Functions](#c5)      | [Function design](#c5.1) [Function parameters](#c5.2)|

 ### <a name="c0"></a> Foreword
 ### <a name="c0.1"></a>Purpose of this document   

For code to be considered "good", it must entail the following characteristics:
1.	Simplicity
2.	Maintainability
3.	Reliability
4.	Testability
5.	Efficiency
6.	Portability
7.	Reusability
Programming is a profession that involves creativity.
Software developers can reference this specification, which will enhance their ability to write consistent, easy-to-read, and high-quality code.
This will ultimately improve product competitiveness and software development efficiency.

### <a name="c0.2"></a> General principles

As a very modern and advanced programming language (completely like other languages), Kotlin complies with the following general principles:
1.	Clarity: Clarity is a necessary feature of programs that are easy to maintain and refactor.
2.	Simplicity: Simple code is easy to understand and implement.
3.	Consistency: Unification is particularly important when the same team works on the same project, utilizing similar styles. It enables code to be easily modified, reviewed, and understood by the team members.

In addition, we need to consider the following factors when programming on Kotlin:
1. Write clean and simple Kotlin code

Kotlin combines two of the main programming paradigms: functional and object-oriented.
Both of these paradigms are trusted, well-known software engineering practices.
As a young programming language, Kotlin builds on well-established languages such as Java, C++, C#, and Scala.
This is why Kotlin introduces many features that help you write cleaner, more readable code, while also reducing the number of complex code structures. For example: type and null safety, extension functions, infix syntax, immutability, val/var differentiation, expression-oriented features, when statements, much easier work with collections, type auto conversion, and other syntactic sugar.

  
2. Follow Kotlin idioms

The author of Kotlin, Andrey Breslav, mentioned that it is both pragmatic and practical, but not academic.
Its pragmatic features enable ideas to easily be transformed into real working software. This programming language is closer to natural languages than its predecessors, and it implements the following design principles: readability, reusability, interoperability, security, and tool-friendliness (https://blog.jetbrains.com/kotlin/2018/10/kotlinconf-2018-announcements/).

3.Use Kotlin efficiently

Some Kotlin features help you write higher-performance code: including rich coroutine library, sequences, inline functions/classes, arrays of basic types, tailRec, and CallsInPlace of contract.
 
### <a name="c0.3"></a> Terminology   

**Rules**: conventions that should be followed when programming.

**Recommendations**: conventions that should be considered when programming.

**Explanation**: necessary explanations of rules and recommendations.

**Example**: examples (recommended and not recommended) of the rules and recommendations.

Unless otherwise stated, this specification applies to versions 1.3 and later of Kotlin.

### <a name="c0.4"></a>Scope

This specification applies to all software coded in Kotlin within the company.

### <a name="c0.5"></a>Exception

Even though exceptions may exist, it is important to understand why rules and recommendations are needed.
Depending on your project situation or personal habits, you can break some of the rules. However, remember that one exception leads to many and can completely destroy the consistency of code. As such, there should be very few exceptions.
When modifying open-source code or third-party code, you can choose to implement the style used by the code (as opposed to using the existing specifications) to maintain consistency.
Software that is directly based on the interface of the Android native operating system, such as the Android Framework, remains consistent with the Android style.

### <a name="c1"></a>1 Naming
In programming, it is difficult to meaningfully and appropriately name variables, functions, classes, etc. Good names clearly express the main ideas and functionality of your code, as well as avoid misinterpretation, unnecessary coding and decoding, magic numbers, and inappropriate abbreviations.

### <a name="r1.1"></a>Rule 1.1: 
The source file encoding format (including comments) must be UTF-8 only. The ASCII horizontal space character (0x20, that is, space) is the only permitted white space character. Tabs are not used for indentation.

### <a name="c1.1"></a>Identifiers
### <a name="r1.2"></a> Rule 1.2:
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
### <a name="r1.3"></a> Package names are in lower case and separated by dots. Code developed within your company should start with `your.company.domain`, and numbers are permitted in package names.
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
### <a name="r1.4"></a> Rule 1.4: Classes, enumerations, interface names use camel case nomenclature
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
### <a name="r1.5"></a> Rule 1.5: function names should be in camel case

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
### <a name="r1.6"></a> Rule 1.6 Constant names should be in UPPER case, words separated by underscore

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
### <a name="r1.7"></a> Rule 1.7: The name of the non-constant field should use camel case and start with a lowercase letter.

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

### <a name="s1.1"></a> Recommendation 1.1: Avoid using Boolean variable names with negative meaning.

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

### <a name="c2"></a>Chapter 2  comments

  The best practice is to begin your comment with a short summary, it can be an abstract in one sentence and it can be detailed later.
  You should balance between writing no comments at all and obvious comments for most each line of code.
  Comments should be accurate, express clearly, they should not simply repeat the name of the class / interface / method.  
  Do not think that commenting of bad code will fix it. Fix it immediately when you see an issue or plan to fix it (at least put TODO with a number of Jira where you plan to fix it).. 
  Comments should first accurately reflect the design ideas and code logic; second, they should describe the business logic, so that other programmers can quickly understand the information behind the code.
  Imagine that you are writting comments for yourself from the future. It will help you even after a long time when you will return to the code to understand the ideas.
  Also comments are also very useful for your successors, who will be able easily get into your code.
 
### <a name="c2.1"></a> General form of Kdoc 
 
KDoc is a combination of JavaDoc's block tags syntax (extended to support specific constructions of Kotlin) and Markdown's inline markup.
The basic format of KDoc is shown in the following example:

```kotlin
 /\*\*
 \* There are multiple lines of KDoc text,
 \* Other ...
 \*/
fun method(arg: String) {
    // …
}
```

Or the following single line form:
```kotlin
 / \ * \ * Short form of KDoc. \ * /
```
 When the entire KDoc block can be stored in one line (and there is no KDoc mark @XXX), a single line form can be used.
 For detailed usage instructions of KDoc, please refer to [Official Document] (https://docs.oracle.com/en/Kotlin/Kotlinse/11/tools/KDoc.html).

### <a name="r2.1"></a> Rule 2.1: KDoc is used for each public, protected or internal code element

 At a minimum, KDoc should be used for every public, protected or internal decorated class, interface, enumeration, method, and member field (property).
 Other code blocks can have KDocs also if needed.

Exceptions:

1. For setters / getters of properties, that are obvious comments are optional (please note that simple get/set methods are generated by Kotlin under the hood)
   For example, getFoo, if there is really nothing else worth saying, it can also be "return foo".
   
2. It is optional to add comments for simple one line methods like:
```kotlin
val isEmpty: Boolean
    get() = this.size == 0
```

or

```kotlin
fun isEmptyList(list: List<String>) = list.size == 0
```

3. You can skip KDocs for method's override if the method is not so different from the method from super class

###  <a name="r2.2"></a>Rule 2.2: When the method has arguments, return value, can throw exceptions, etc., it must be described in the KDoc block: with @param, @return, @throws, etc.

Good examples：

 ```kotlin
/** 
 * This is the short overview comment for the example interface.
 *     / * Add a blank line between the comment text and each KDoc tag underneath * /
 * @since 2019-01-01
 */
 protected abstract class Sample {
    /**
     * This is a long comment with whitespace that should be split in 
     * multiple line comments in case the line comment formatting is enabled.
     *     / * Add a blank line between the comment text and each KDoc tag underneath * /
     * @param fox A quick brown fox jumps over the lazy dog
     * @return the rounds of battle of fox and dog 
     */
    protected abstract fun foo(Fox fox)
                      /* 注释上面加1个空行 */ 
     /**
      * These possibilities include: Formatting of header comments
      *     / * Add a blank line between the comment text and each KDoc tag underneath * /
      * @return the rounds of battle of fox and dog
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

### <a name="r2.3"></a>Rule 2.3: There is only one space between the Kdoc tag and the content, tags are arranged in the following order: @param, @return, @throws
This is how Kdoc should look like and what it should contain:
 - Functional and technical description, explaining the principles, intentions, contracts, API, etc.
 - The function description and @tags (implSpec, apiNote, implNote) **require an empty line** after them.
 - @implSpec, a specification related to API implementation, it should let the implementer decide whether to override it.
 - @apiNote, explain the API precautions, including whether to allow null, whether the method is thread safe, algorithm complexity, input and output range, exceptions, etc.
 - @implNote, a some note related to API implementation, that implementers should keep in mind.
 - **Then empty 1 line**, followed by regular @param, @return, @throws and other comments.
 - These conventional standard "block labels" are arranged in order: @param, @return, @throws;
 - no empty descriptions in tag blocks are allowed, better not to write Kdoc at all than to waste code line on empty tags
 - there should be no empty lines between the method/class declaration and the end of Kdoc (*/ symbols)
 - (!) KDoc does not support the @deprecated tag. Instead, please use the @Deprecated annotation.
 
  If the description of a tag block cannot fit a single line and is split to several lines, then the content of the new line should be indented by 4 spaces from the '@' position to align ('@' itself counts as 1, plus 3).
  **Exception: ** When the description text in a tag block is too long to wrap, it is also possible to indent the alignment with the previous line of description text.
   The description text of multiple tags does not need to be aligned, see [Recommendation 3.8 should not insert spaces horizontally aligned] (#s3.8).     

In Kotlin compared to Java you are able to put several classes inside one file so each class should have a Kdoc 
formatted comment (this is also stated in rule 2.1).
This comment should contain @since tag. The good style is to write the version when this functionality was released after a since tag.
Examples：

```java
/**
 * Description of functionality
 *
 * @since 1.6
 */
```

Other KDoc tags (such as @param type parameters, @see, etc.) can be added as follow:
```java
/**
 * Description of functionality
 *
 * @apiNote: 特别需要注意的信息
 *
 * @since 1.6
 */
```
### <a name="c2.2"></a>Comments to the file header

### <a name="r2.4"></a>Rule 2.4 The file header comments must include copyright information, should NOT contain creation date and author name (it is antipattern - use VCS for history management). Files that contain multiple or no classes should also contain some description of what is inside of this file.
File header comments should be stored BEFORE package name and imports. 
If you need to add other content to the file header comment, you can add it later in the same format.

The content and format of the copyright license must be as follows, the Chinese version. For example if your company is Huawei:
`版权所有 (c) 华为技术有限公司 2012-2020` 
English version:
`Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.`

Regarding the release notes, see examples below:

-2012-2020 can be modified according to actual needs. 2012 is the year the file was first created, and 2020 is the year the file was last modified. The two can be the same, such as "2020-2020".
  When there are major changes to the file such as feature extensions, major refactorings, etc, then the subsequent years must be updated.
- -The copyright statement can use your company's subsidiaries. For example:
  On Chinese：版权所有 (c) 海思半导体 2012-2020
  On English：Copyright (c) Hisilicon Technologies Co., Ltd. 2012-2020. All rights reserved.

Copyright should not use KDoc style or single line style comments, it must start from the beginning of the file.
For example if your company is Huawei - below is a minimal Copyright comment without other functional comments:

```java
/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2018. All rights reserved.
 */
```

Note the following when writing file header or comments for top-level classes:
- File header comments must start from the top of the file. If it is a top-level file comment - there should be a blank line after ending Kdoc '*/' symbol.
  If it is the comment for top-level class - then class declaration should start immediately without any newline.

- Maintain a unified format. The specific format can be formulated by the project (for example in opensource), need to follow it.

- In a top-level file Kdoc need to include copyright and functional description, especially if the number of top-level classes in a file is not equal to 1.

- It is forbidden to have empty comment blocks.
     As in the above example, if there is no content after the option `@apiNote`, the entire tag block should be deleted.

- Industry is not using any history information in comments. History can be found in VCS (git/svn/e.t.c). It is not recommended to include historical data in the comments of the Kotlin source code.


### <a name="c2.3"></a>Function header comments
### <a name="r2.5"></a>Rule 2.5 Prohibit empty or useless function comments

Function header comments are placed above function declarations or definitions. There should be no newline between a function declaration and it's Kdoc. 
Use the above [KDoc](#c2.1) style rules.  

In Chapter 1 of current code style we stated that function name should self commend it's functionality as much as possible. So in the Kdoc try to mention things that are not stored in function name.
Avoid dummy useless comments. 

The function header comment content is optional, but not limited to: function description, return value, performance constraints, usage, memory conventions, algorithm implementation, reentrant requirements, etc.
The module's external interface declaration and its comments should clearly convey important and useful information.

### <a name="c2.4"></a>Code comments

### <a name="r2.6"></a>Rule 2.6 Add a blank line between the body of the comment and Kdoc tag-blocks; there must be 1 space between the comment character and the content of the comment; there must be a newline between a Kdoc and the previous code above
1. There must be 1 space between the comment character and the content of the comment; there must be a newline between a Kdoc and the previous code above; there should be no empty line between Kdoc and code it is describing.
 No need to add a blank line before a first comment in this particular name space (code block), for example between function declaration and first comment in a function body.  

Examples: 
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

2. Leave one single space between the comment on the right side of the code and the code.
 Conditional comments in the `if-else-if` scenario:
 For a better understanding, put the comments inside `else if` branch or in the conditional block, but not before the `else if`. 
 When the if-block is used with curly braces - the comment should be on the next line after opening curly brace.
 
In Kotlin compared to Java the if statement returns value, that's why there can be a comment block that is describing whole if statement.  
  
Use the following style:
```kotlin
val foo = 100  // right-side comment
val bar = 200  /* right-side comment */

// general comment for the value and for the whole if-else condition
val someVal = if (nr % 15 == 0) {
    // when nr is a multiple of both 3 and 5
    println("fizzbuzz")
} else if (nr % 3 == 0) {
    // when nr is a multiple of 3, but not 5
    // We print "fizz", only.
    println("fizz")
} else if (nr % 5 == 0) {
    // when nr is a multiple of 5, but not 3
    // We print "buzz", only.
    println("buzz")
} else {
    // Otherwise we print the number.
    println(x)
}
```

3. Start all comments (including KDoc) with a space after leading symbol (`//`, `/*`, `/**` and `*`)
   Good example:
   ```kotlin
   val x = 0  // this is a comment
   ```

### <a name="r2.7"></a>Rule 2.7 Do not comment unused code blocks (including imports). Delete them immediately.

Code - is not a history storage. For history use git, svn or other VCS tools.
Unused imports increase the coupling of the code and are not conducive to maintenance. The commented out code cannot be maintained normally; when attempting to resume using this code, it is very likely to introduce defects that can be easily missed.
The correct approach is to delete the unnecessary code directly and immediately when it becomes unused. If you need it again, consider porting or rewriting this code. Things could have changed during the time when code was commented.

### <a name="s2.1"></a>Recommendation 2.1 The code formally delivered to the client generally should not contain TODO / FIXME comments

TODO notes are generally used to describe known modification points that need to be improved and added. For example refactoring
FIXME comments are generally used to describe known defects and bugs that will be fixed later and now are not critical for an application.
They should all have a unified style to facilitate the unified processing of text search. For example:

```java
// TODO(<author-name>): Jira-XXX - support new json format
// FIXME: Jira-XXX - fix NPE in this code block
```

In the version development stage, such annotations can be used to highlight the issues in code, but all of them should be fixed before release of a new production version.

### <a name="c3"></a>3 Typesetting

### <a name="c3.1"></a> File-related rules

### <a name="r3.1"></a> Rule 3.1 Avoid files that are too long. Files should not exceed 2000 lines (non-empty and non-commented lines)

Having too long files often means that the file is too complicated and can be split into smaller files/functions/modules.
It is recommended to split horizontally according to responsibilities, or vertically layer according to hierarchy.
The only exception that can be in this case - codeGen. Autogenerated files that are not modified manually can be longer.

### <a name="r3.2"></a> Rule 3.2 A source file contains code blocks in the following order: copyright, package name, imports, top-level classes. They should be separated by 1 blank line.

a) The order should be the following:
1. Kdoc for the file with license or copyright
2. @file annotation 
3. package name
4. Import statements
5. Top class header and top function header comments
6. A top-level classes or top-level functions;

b) Each of the above code blocks should be separated by a blank line.
c) Import statements are ordered alphabetically, without line breaks, and are not using wildcards *
d) Recommendation: in one .kt source file there should be only one class declaration and its name should match with the filename

### <a name="s3.1"></a>Recommendation 3.1: import statements should appear in the following order: Android, internal company imports, external imports, java core dependencies and finally, kotlin standard library. Each group should be separated by a blank line.

From top to bottom, the order is the following:
1. Android
2. Imports of packages used internally in your organization
3. Imports from other non-core dependencies
4. java core packages
5. kotlin stdlib

Each category should be sorted in alphabetical order. This style is compatible with [Android import order](https://source.android.com/setup/contribute/code-style#order-import-statements).

Recommended example:
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

### <a name="s3.2"></a>>Recommendation 3.2: The declaration part of a class-like code structures (class/interface/e.t.c) should be in the following order: compile-time constants (for objects), class properties, late-init class properties, init-blocks, constructors, public methods, internal methods, protected methods, private methods, companion object. Their declaration should be separated by blank lines.

Notes:
1. There should be no blank lines between properties. Exceptions: when there is a comment before property on a separate line or annotations on a separate line.
2. Properties with comments/Kdoc should be separated by a newline before the comment/Kdoc
3. Enum entries and constant properties (`const val`) in companion objects should be sorted alphabetically.

The declaration part of a class or interface should be in the following order:
 - compile-time constants (for objects)
 - Properties
 - late-init class properties
 - init-blocks
 - constructors
 - methods or nested classes. Put nested classes next to the code that uses those classes. If the classes are intended to be used externally and aren't referenced inside the class, put them in the end, after the companion object.
 - companion object

**Exception：**
All variants of a (private) val logger should be placed in the beginning of the class ((private) val log/LOG/logger/e.t.c)

### <a name="c3.1"></a> Braces
### <a name="r3.3"></a>Rule 3.3 Braces must be used in conditional statements and loop blocks

In `if`, `else`, `for`, `do`, and `while` statements, even if the program body is empty or contains only one statement, braces should be used.
In special Kotlin `when` statement no need to use braces for statements with 1 line. Valid example:

    ```kotlin
    when (node.elementType) {
        FILE -> {
            checkTopLevelDoc(node)
            checkSomething()
         }
        CLASS -> checkClassElements(node)
    }
    ```
**Exception:** *Only* The only exception is ternary operator in Kotlin (it is a single line `if () <> else <>` ) 
When the entire expression can be 

Bad example:

```kotlin
val value = if (string.isEmpty())  // WRONG!
                0
            else
                1
```

Valid example: 

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

### <a name="r3.4"></a> Rule 3.4 For *non-empty* blocks and block structures, the opening brace is placed at the end of the line

For *non-empty* code blocks with braces, they should follow the K&R style (1TBS or OTBS style):
 - The opening brace is on the same same line with the first line of the code block
 - The closing brace is on it's new line
 - The closing brace can be followed by a new line. Only exceptions are: `else`, `finally`, `while` (from do-while statement) or `catch` keywords. These keywords should not be split from the closing brace by a newline.
 
 Exception cases: 
 1) for lambdas no need to put a newline after first (function related) opening brace. Newline should appear only after an arrow (see [rule 3.6, point 5](#r3.6)):
```kotlin
arg.map { value ->
    foo(value)
}
```
2) for else/catch/finally/while (from do-while statement) keywords closing brace should stay on the same line:
 ```kotlin
do {
    if (true) {
        x++
    } else {
        x--
    }
} while (x > 0) 
```
 
Good example：

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
                    } catch (ProblemException e) {
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

### <a name="c3.2"></a> Indentation
### <a name="r3.5"></a>Rule 3.5: Use spaces for indentation, indenting is equal to 4 spaces

Only spaces are allowed for indentation and each indentation should equal to 4 spaces (tabs are not allowed). 
In case you prefer using tabs - just simply configure auto change of tabs to spaces in your IDE.

These code blocks should be indented if they are placed on the newline and:
1) this code block goes right after an opening brace 
2) this code block goes after each and every operator, including assignment operator (+/-/&&/=/e.t.c)
3) this code block is a call chain of methods: 
```kotlin
someObject
    .map()
    .filter()
``` 
4) this code block goes right after the opening parenthesis 
5) this code block goes right after an arrow in lambda: 
 ```kotlin
arg.map { value ->
    foo(value)
}
```
    
Exceptions cases:
1) Argument lists: \
    a) 8 spaces are used for indenting in argument list (both in declaration and at call site) \
    b) Arguments in argument list can be aligned if they are on different lines
2) 8 spaces are used if newline is after any binary operator
3) 8 spaces are used for functional-like style when the newline is placed before the dot
4) Supertype lists: \
   a) 4 spaces if colon before supertype list is on new line \
   b) 4 spaces before each supertype; 8 if colon is on new line
    
Please note, that indenting should be also done after all statements like if/for/e.t.c when they don't have braces, though this code style always requires braces for them. See: [3.4](r3.4)
    
Example:

    ```kotlin
    if (condition)
        foo()
    ```

Exceptions:
    - When breaking parameter list of a method/class constructor it can be aligned with 8 spaces or a parameter that was moved to a newline can be on the same level as the previous argument:
    
    ```kotlin
    fun visit(
            node: ASTNode,
            autoCorrect: Boolean,
            params: KtLint.Params,
            emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit
    ) {   
    ```
    
   - Operators like +/-/*/e.t.c can be indented with 8 spaces:
    
    ```kotlin
    val abcdef = "my splitted" +
                    " string"
    ```
    
   - List of super types should be indented with 4 spaces if they are on different lines, or with 8 spaces if leading colon is also on a separate line
    
    ```kotlin
    class A :
        B()
        
    class A
        :
            B()
    ```
### <a name="c3.3"></a> Empty blocks
### <a name="s3.3"></a>Recommendation 3.3: try to avoid empty blocks; multiple braces should start a new line

An empty code block can be closed immediately on the same line as well as the block can be closed on the next line.
But it is preferred to have a newline between opening and closing braces `{}` (see examples below).

**Generally, empty code blocks are prohibited** and are very bad practice (especially for catch block).
 The only code structures where it is appropriate are overridden functions, when functionality of the base class is not needed in the class-inheritor. 
```kotlin
override fun foo() {
}
``` 

Recommended examples of formatting empty code blocks (but note, that generally they are prohibited):

```kotlin
fun doNothing（）{} 

fun doNothingElse（）{ 
}
```

Not recommended:
```kotlin
try {
  doSomething()
} catch (e: Some) {}
```

Use this code instead of the example above
```kotlin
try {
   doSomething()
} catch (e: Some) {
}
```

### <a name="c3.4"></a>Line width
### <a name="s3.5"></a> Recommendation 3.4: line length should be less than 120 symbols

This international code style prohibits non-latin (non ASCII) symbols in the code. But in case you would like to use them anyway - please use the following convention:

- One wide character occupies the width of two narrow characters.
  The "wide" and "narrow" of a character are defined by its [*east asian width* Unicode attribute] (https://unicode.org/reports/tr11/).
  Generally, narrow characters are also called "half-width" characters. All characters in the ASCII character set include letters (such as `a`, `A`), numbers (such as `0`, `3`), and punctuation (such as `, `, `{`), spaces. All of them are narrow characters.
  Wide characters are also called "full-width" characters, Chinese characters (such as `中`, `文`), Chinese punctuation (`, `, `, `), full-width letters and numbers (such as `Ａ`、`３`) are all. These characters counted as 2 narrow characters.
  
- Any line that exceeds this limit (120 narrow symbols) should be wrapped, as described in the [*Newline* section](#c3.5). 

 **Exceptions：**

  1. Long URL or long JSON method reference in KDoc.
  2. The `package` and `import` statements.
  3. The command line in the comment so that it can be cut and pasted into the shell for use.

<!-- =============================================================================== -->
### <a name="c3.5"></a> Line breaks (newlines)
### <a name="s3.4"></a> Recommendation 3.5 No more than one statement per line
There should not be more than one code statement in one line (this recommendation prohibits usage of code with ";")
Such code is prohibited as it makes code visibility worse:
```kotlin
val a = ""; val b = ""
```

### <a name="r3.6"></a>Rule 3.6 line break style rules if the line is split

1. Compared to Java Kotlin allows not to put semicolon (';') after each statement separated by newline.
 There should be no redundant semicolon at the end of lines.
 
 In case when newline is needed to split the line, it should be placed after operators like &&/||/+/e.t.c and all *infix functions* (for example - [xor](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/xor.html)).
 But newline should be placed before operators like ('.', '?.', '?:', '::', e.t.c).
 
  Please note that all operators for comparing like '==', '>', '<', e.t.c should not be split.
  Incorrect: 
  ```kotlin
         if (node !=
                 null && test != null) {}
 ```
 
  Correct: 
  ```kotlin
             if (node != null && 
                     test != null) {
             }
 ```
  
* Note, that you need to follow functional style (i.e. each function call in a chain with `.` should start at a new line if chain of functions contains more than one call):
```kotlin
  val value = otherValue!!
          .map { x -> x }
          .filter {
              val a = true
              true
          }
          .size    
```
* Note, that Kotlin parser prohibits operator !! to be separated from the value it is checking.

**Exception**: if functional chain is used inside of the branches of ternary operator - no need to split them with newlines. Valid example:
```kotlin
if (condition) list.map { foo(it) }.filter { bar(it) } else list.drop(1)
```  
  
2. Newline should be placed after assignment operator ('=')
3. In function or class declarations name of a function or constructor should not be split by a newline from the opening brace '('.
   Brace follows immediately after the name without any spaces both in declarations and at call sites.
4. Newline should be placed right after the comma (',')
5. In lambda statements, if it's body contains more than one line, the newline should be placed after an arrow if lambda has explicit parameters.
   If lambda uses implicit parameter (`it`), newline should be placed after opening brace ('{').
   See examples below:

Bad example:
```kotlin
    value.map { name -> foo()
        bar()
    }
```

Recommended examples：
```kotlin
value.map { name ->
    foo()
    bar()
}

val someValue = { node:String -> node }
```

6. When the function contains only a single expression, it can be expressed as [expression function] (https://kotlinlang.org/docs/reference/functions.html#single-expression-functions).
Instead of: 
```kotlin
override fun toString(): String { return "hi" }
```
use:
```kotlin
override fun toString() = "hi"
```

7. If argument list in function declaration (including constructors) contains more than 2 arguments, these arguments should be split by newlines:
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

If and only if the first parameter is on the same line as an opening parenthesis, all parameters can horizontally aligned by the first parameter.
Otherwise, there should be a line break after an opening parenthesis.

Kotlin 1.4 introduced a trailing comma as an optional feature, so it is generally recommended to place all parameters on a separate line
and append trailing comma. It makes resolving of merge conflicts easier.

8. If supertype list has more than 2 elements, they should be separated by newlines
```kotlin
class MyFavouriteVeryLongClassHolder :
    MyLongHolder<MyFavouriteVeryLongClass>(),
    SomeOtherInterface,
    AndAnotherOne { }
```

### <a name="c3.6"></a>Blank lines
### <a name="s3.6"></a>Recommendation 3.6: Reduce unnecessary blank lines and keep the code compact

By reducing unnecessary blank lines, you can display more code on one screen and that makes the code more readable.

- Blank lines should separate content based on it's relevance: blank lines should be placed between groups of fields, constructors, methods, nested classes, init blocks, objects (see [rule 3.2](#r3.2))
- Do not use more than one single line inside methods, type definitions, initialization expressions
- Generally do not use more than two consecutive blank lines in a row
- Do not put newlines in the beginning or at the end of code blocks with curly braces:

```kotlin
fun baz() {
        
    doSomething();  // No need to add blank lines at the beginning and at the end of the code block
    // ...

}
```

###<a name="c3.7"></a> Horizontal space
### <a name="s3.7"></a> Recommendation 3.7: Usage of whitespace for code separation

  Note: this rule corresponds to the case when symbols are located on the same line. In some cases there could be a line break instead of a space, but this logic is described in other rules.

  1. Any keywords (like 'if', 'when', 'for', e.t.c) should be separated with a single whitespace from the opening parenthesis.
     Only exceptions is 'constructor' keyword. It should not be separated from a parenthesis.

  2. Separate any keywords (such as `else` or `try`, etc.) from the opening brace ('{') with a single whitespace.
     If `else` is used in ternary-style statement without braces, then there should be a single space between 'else' and the statement after:
     `if (condition) foo() else bar()`
  
  3. Use single whitespace before any opening brace (`{`).
     Only exception is passing of a lambda as a parameter inside parenthesis:
     ```kotlin
         private fun foo(a: (Int) -> Int, b: Int) {}
         foo({x: Int -> x}, 5) // no space before '{'
     ```

  4. Single whitespace should be placed on both sides of binary operators. This also applies to operator-like symbols, for example: 

     - Colon in generic structures with 'where' keyword: `where T : Type`
     - Arrow in lambdas: `(str: String) -> str.length()`

  **Exceptions：**

  - Two colons (`::`) are written without spaces: `Object::toString`
  - Dot separator (`.`) that stays on the same line with an object name `object.toString()`
  - Safe access modifiers: `?.` and `!!`, that stay on the same line with an object name: `object?.toString()`
  - Operator `..` for creating ranges, e.g. `1..100`

  5. Spaces should be used *after* ',' and ':' (also ';', but please note that this code style prohibits usage of ';' in the middle of the line, see [rule 3.4](#s3.4)) (except cases when those symbols are in the end of line). There should be no whitespaces in the end of line.
  The only exception when there should be no spaces *after* colon is when colon is used in annotation to specify use-site target (e.g. `@param:JsonProperty`)
  There should be no spaces *before* `,`, `:` and `;`. The only exceptions for colon are the following:
  - when `:` is used to separate a type and a supertype, including anonimous object (after `object` keyword)
  - when delegating to a superclass constructor or a different constructor of the same class
  
  Good example:
  ```kotlin
      abstract class Foo<out T : Any> : IFoo { }
      
      class FooImpl : Foo() {
          constructor(x: String) : this(x) { /*...*/ }
          
          val x = object : IFoo { /*...*/ } 
      }
  ```

  6. There should be *only one space* between identifier and it's type: `list: List<String>`
  If type is nullable there should be no space before `?`.
  
  7. When using '[]' operator (get/set) there should be *no* spaces between identifier and '[': `someList[0]`
  
  8. There should be no space between a method or constructor name (both at declaration and at call site) and a parenthesis: `foo() {}`
  Note that this subrule is related only to spaces, whitespace rules are described in [rule 3.6](#r3.6). This rule does not prohibit, for example, the following code:
  ```kotlin
    fun foo
    (
        a: String
    )
  ```

  9. Never put a space after `(`, `[`, `<` (when used as bracket in templates) or before `)`, `]`, `>` (when used as bracket in templates)

  10. There should be no spaces between prefix/postfix operator (like `!!` or `++`) and it's operand

### <a name="s3.8"></a>Recommendation 3.8: No spaces should be inserted for horizontal alignment

*Horizontal alignment* - is a practice to add additional spaces in the code, to align code blocks on the same level with previous code blocks.

- It always takes time to format and support such code and fix alignment for new developers in case they need to change something in aligned code.
- Long identifier names will break the alignment and will make the code less presentable.
- The disadvantages of alignment are greater than the benefits; in order to reduce maintenance costs and not cause trouble, misalignment is the best choice.

Recommendation: the only exception where it can look good is `enum class`, where you can use alignment (in table format) to make code more readable:
```kotlin
enum class Warnings(private val id: Int, private val canBeAutoCorrected: Boolean, private val warn: String) : Rule {
    PACKAGE_NAME_MISSING         (1, true,  "no package name declared in a file"),
    PACKAGE_NAME_INCORRECT_CASE  (2, true,  "package name should be completely in a lower case"),
    PACKAGE_NAME_INCORRECT_PREFIX(3, false, "package name should start from company's domain")
    ;
}
```

 Recommended examples：
 ```kotlin
 private val nr: Int // no alignment, but looks fine
 private var color: Color // no alignment
 ```

 Not recommended：
 ```kotlin
 private val    nr: Int    // aligned comment with extra spaces
 private val color: Color  // alignment for a comment and alignment for identifier name
 ```

###<a name="c3.8"></a> Enumerations
### <a name="s3.9"></a>Recommendation 3.9: enum values are separated by comma and a line break. ';' is put on the new line
1) Enum values are separated by comma and a line break. ';' is put on the new line:
     ```Kotlin
        enum class Warnings {
            A,
            B,
            C
            ;
        }
     ```
    This will help to resolve conflicts and reduce it's number during merging pull requests.

2) In case enum is simple (it has no properties, no methods and no comments inside) - it can be declared in a single line:
     ```kotlin
    enum class Suit { CLUBS, HEARTS, SPADES, DIAMONDS }
     ```

3) Prefer enum classes if it is possible, for example instead of 2 Boolean properties like:
    `val isCelsius = true | val isFahrenheit = false` use enum class:
    ```kotlin
    enum class TemperatureScale { CELSIUS, FAHRENHEIT }
    ```

    - The variable value only changes within a fixed range and is defined with the enum type. For example, [Recommend 8.6.3 Keyboard](#s8.6.3) example
    - Avoid comparison with magic constant numbers of -1, 0, and 1, instead of this use enums:
  ```kotlin
    enum class ComparisonResult {
        ORDERED_ASCENDING,
        ORDERED_SAME,
        ORDERED_DESCENDING
        ;
    }
  ```

### <a name="c3.9"></a> Variable declaration
### <a name="r3.7"></a>Rule 3.7: declare one variable on one line

Each property or variable declaration should be declared on separate line. Bad example: `val n1: Int; val n2: Int`.

### <a name="s3.10"></a>Recommendation 3.10: Variables are declared close to the line where they are first used
Local variables are declared close to the point where they are first used. This will minimize their scope.
Local variable are usually initialized during declaration or initialized immediately after declaration.
The member fields of the class should be declared collectively (see [recommendation 3.2](#s3.2) for details on class structure).

### <a name="c3.10"></a>When expression

### <a name="r3.10"></a>Rule 3.10: 'when' statement must have else branch, unless when condition variable is enumerated or sealed type
Each when statement contains an `else` statement group, even if it does not contain any code.

*Exception:* If a when statement of type `enum or sealed` contains all values of a enum - there is no need to have "else" branch.
The compiler can issue a warning when it is missing.

### <a name="c3.11"></a> Annotations
### <a name="s3.11"></a> Recommendation 3.11: Each annotation applied to a class, method or constructor is on its own line

1. Annotations applied to the class, method, or constructor are placed on separate lines (one annotation per line). Example:
```kotlin
@MustBeDocumented
@CustomAnnotation
fun getNameIfPresent() { ... }
```
2. A single annotation should stay on the same line where the code that it is annotating:
```kotlin
@CustomAnnotation class Foo {}
```
3. Multiple annotations applied to a field/property are allowed to appear on the same line as the field:
```kotlin
@MustBeDocumented @CustomAnnotation loader: DataLoader
```
### <a name="c3.12"></a> Comments layout

Block comments are at the same indentation level as the surrounding code.
Recommended examples:

 ```kotlin
class SomeClass {
     /*
      * This is
      * okay
      */
      fun foo() {}
}
 ```

 **Hint：** To have automatic formatting by IDEs use `/*...*/` block comments.

### <a name="c3.13"></a> Modifiers
### <a name="s3.12"></a> Recommendation 3.12 If a declaration has multiple modifiers, always follow the sequence below
Recommended sequence:

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

### <a name="s3.13"></a>Recommendation 3.13: long numerical values should be separated by underscore

Note: Usage of underscores makes the numeric constants easier to read and easier to find errors in it.
```kotlin
val oneMillion = 1_000_000
val creditCardNumber = 1234_5678_9012_3456L
val socialSecurityNumber = 999_99_9999L
val hexBytes = 0xFF_EC_DE_5E
val bytes = 0b11010010_01101001_10010100_10010010
```

### <a name="c3.14"></a> Strings
### <a name="r3.14.1"></a>Rule 3.14.1: Concatenation Strings is prohibited when string fits one line, use raw strings and string templates instead.
Kotlin significantly enhanced work with Strings:
[String templates](https://kotlinlang.org/docs/reference/basic-types.html#string-templates), [Raw strings](https://kotlinlang.org/docs/reference/basic-types.html#string-literals)
That's why code looks much better when instead of using explicit concatenation to use proper Kotlin strings in case your line is not too long and you do not need to split it with newlines.

Bad example:
```kotlin
val myStr = "Super string"
val value = myStr + " concatenated"
```

Good example:
```kotlin
val myStr = "Super string"
val value = "$myStr concatenated"
```

### <a name="r3.8"></a>Rule 3.14.2: String template format
Redundant curly braces in string templates

In String templates there should not be redundant curly braces. In case of using a not complex statement (one argument)
 there should not be curly braces.

Bad example:
```kotlin
val someString = "${myArgument} ${myArgument.foo()}"
```

Valid example:
```kotlin
val someString = "$myArgument ${myArgument.foo()}"
```

Redundant string template
In case string template contains only one variable - there is no need to use string template. Use this variable directly.

Bad example:
```kotlin
val someString = "$myArgument"
```

Valid example:
```kotlin
val someString = myArgument
```


### <a name="c4"></a>4 Variables and types
### <a name="c4.1"></a>Variables
### <a name="r4.1.1"></a> Rule 4.1.1: Do not use Float and Double when accurate calculations are needed
Floating point numbers provide a good approximation over a wide range of values, but they cannot produce accurate results in some cases.
Binary floating-point numbers are unsuitable for precise calculations, because it is impossible to represent 0.1 or any other negative power of 10 in a `binary representation` with a finite length.

For example - this simple code that looks to be obvious:
```
    val myValue = 2.0 - 1.1
    println(myValue)
``` 

will print something like this: `0.8999999999999999`

That's why in case you need precise calculations (currency, finance, exact science etc.), it is recommended to use Int, Long, BigDecimal, etc. BigDecimal should be a good choice.

Bad example:

If a float value contains more than 6-7 decimal numbers, then it will be rounded
 ```kotlin
 val eFloat = 2.7182818284f // Float, will be rounded to 2.7182817
 ```

Recommended example: when accurate calculations are needed:

 ```kotlin
    val income = BigDecimal("2.0")
    val expense = BigDecimal("1.1")
    println(income.subtract(expense)) // here you will get 0.9
 ```

### <a name="r4.1.2"></a>Rule 4.1.2: Numbers of a float type should not be directly compared with equality operator (==) or other methods like compareTo and equals. 

Since floating-point numbers have precision problems in computer representation - as it was recommended in
Rule 4.1.1 - better to use BigDecimal instead when you need to make accurate computations and comparison.
The following code describes these problems:
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

Recommended example:

```kotlin
val foo = 1.03f
val bar = 0.42f
if (abs(foo - bar) > 1e-6f) {
    println("Ok")
} else {
    println("Not")
}
```

### <a name="r4.1.3"></a> Rule 4.1.3 Try to use 'val' instead of 'var' for variable declaration [SAY_NO_TO_VAR]

Variables with `val` modifier - are immutable (read-only). Usage of such variables instead of `var` variables increases robustness and readability of code,
because `var` variables can be reassigned several times in the business logic. Of course, in some scenarios with loops or accumulators only `var`s can be used and are allowed.
 
### <a name="c4.2"></a>Types
### <a name="s4.2.1"></a>Recommendation 4.2.1: Use Contracts and smart cast as much as possible

Kotlin compiler introduced [Smart casts](https://kotlinlang.org/docs/reference/typecasts.html#smart-casts) that help to reduce the size of code.

Bad example:
```kotlin
    if (x is String) {
        print((x as String).length) // x was already automatically cast to String - no need to use 'as' keyword here
    }
```

Good example:
```kotlin
    if (x is String) {
        print(x.length) // x was already automatically cast to String - no need to use 'as' keyword here
    }
```

Also Kotlin 1.3 introduced [Contracts](https://kotlinlang.org/docs/reference/whatsnew13.html#contracts) that provide enhanced logic for smart-cast.
Contracts are used and are very stable in `stdlib`, for example:

```kotlin
fun bar(x: String?) {
    if (!x.isNullOrEmpty()) {
        println("length of '$x' is ${x.length}") // smartcasted to not-null
    }
} 
```

Smart cast and contracts reduce boilerplate code and forced type conversion, that's why it is much better to use them.

Bad example:
```kotlin
fun String?.isNotNull(): Boolean = this != null

fun foo(s: String?) {
    if (s.isNotNull()) s!!.length // No smartcast here and !! operator is used
}
```

Good example:
```kotlin
fun foo(s: String?) {
    if (s.isNotNull()) s.length // We have used a method with contract from stdlib that helped compiler to execute smart cast
}
```

### <a name="s4.2.2"></a>Recommendation 4.2.2: Try to use type alias to represent types and make code more readable

Type aliases provide alternative names for existing types. If the type name is too long, you can introduce a shorter name and replace the original type name with the new name.
It helps to shorten long generic types. For example, code looks much more readable if you introduce a typealias instead of a long chain of nested generic types.
We recommend to use typealias if the type contains **more than two** nested generic types and this type itself is longer than **25 chars**.

Bad example:
```kotlin
val b: MutableMap<String, MutableList<String>>
```

Good example:
```kotlin
typealias FileTable = MutableMap<String, MutableList<String>>
val b: FileTable
```

You can also provide additional aliases for function (lambda-like) types:
```kotlin
typealias MyHandler = (Int, String, Any) -> Unit

typealias Predicate<T> = (T) -> Boolean
```

### <a name="c4.3"></a> Null safety and variable declarations
Kotlin itself is declared as a null safe programming language. But to be compatible with Java it still supports nullable types.

### <a name="s4.3.1"></a> Recommendation 4.3.1: avoid declaration of variables of nullable types, especially of types from Kotlin stdlib.
To avoid NullPointerException and help compiler checks to prevent NPE try to avoid usage of nullable types (with "?" symbol).
Not recommended:
```kotlin 
val a: Int? = 0
```

Recommended:
```kotlin 
val a: Int = 0
```

Nevertheless, if you use Java libraries extensively you'll have to use nullable types and enrich your code with '!!' and '?' symbols.
But at least for Kotlin stdlib (declared in [official documentation](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/)) avoid using nullable types. 
Try to use initializers for empty collections. For example: if you want to initialise list instead of using `null` use `emptyList()`.

Not recommended:
```kotlin 
val a: List<Int>? = null 
```

Recommended:
```kotlin 
val a: List<Int> = emptyList() 
```

### <a name="s4.3.2"></a> Recommendation 4.3.2: Variables of generic types should have explicit type declaration
As in Java, classes in Kotlin may have type parameters. In general, to create an instance of such a class, we need to provide type arguments:
```kotlin
val myVariable: Map<Int, String> = emptyMap<Int, String>() 
```
But compiler can inherit type parameter from the right value and will not force user to explicitly declare the type.
Such declarations are not recommended as programmer will need to look at the method to find its return value and to understand the type of a variable. 

Not recommended:
```kotlin
val myVariable = emptyMap<Int, String>() 
```

Recommended:
```kotlin
val myVariable: Map<Int, String> = emptyMap() 
```

### <a name="c5"></a>5 Functions
### <a name="c5.1"></a>Function design

Knowledge of how to build design patterns and avoid code smells helps to write clean code. This and functional style should be unified together when you write the code on Kotlin.
Ideas of functional style are the following: function - is the smallest unit of combinable and reusable code. Functions should have clean logic,
**high cohesion** and **low coupling** to effectively organize the code. The code in functions should be simple and straightforward, should not hide
the author's intentions and should have clean abstarction and control statements should be used in a straight forward manner. 
The side effects (code that does not affect function's return value, but affects global/object instance variables) should not be used for state changes.
The only exceptions are state machines. 

[Kotlin is designed to support and encourage functional programming](https://www.slideshare.net/abreslav/whos-more-functional-kotlin-groovy-scala-or-java)。
Kotlin language has built-in mechanisms that support functional programming. Standard collections ans sequences have methods for functional programming. For example - apply/with/let/run/e.t.c.
Kotlin Higher-Order functions, function types, lambdas, default function arguments. As it was discussed [before](#r4.1.3) - Kotlin supports and encourages to use immutable types. 
All of this motivates to write pure functions that avoid side effects and for specific input have corresponding output.

The pure function pipeline data flow - is a part of functional paradigm. When you have chains of function calls and each step is:
1. simple
2. verifiable
3. testable
4. replaceable
5. pluggable
6. extensible  
7. result of each step is immutable 

then it is easy to do concurrent programming. There can be only one side effect in this data stream and it can be placed only at the end.

### <a name="r5.1.1"></a> Rule 5.1.1 Avoid too long functions, no more than 30 lines (non-empty, non-comment)

The function should be able to be displayed on one screen and only implement one certain logic.
Too long function often means that the function is too complicated and can be split or be more primitive.

**Exception:** Some functions that implement complex algorithms may exceed 30 lines due to the aggregation and comprehensiveness.
Linter warnings for such functions can be suppressed. 

Even if a long function works very well right now, once someone else modifies it, new problems or bugs may appear due to the complex logic.
It is recommended to split such functions into several separated functions that are shorter and easier to manage, so that others can read and modify the code properly.

### <a name="r5.1.2"></a>Rule 5.1.2 Avoid deep nesting of function code blocks. It should be limited to 4 levels 

The nesting depth of the code block of a function is the depth of mutual inclusion between the code control blocks in the function (for example: if, for, while, when, etc.).
Each level of nesting will increase the mental effort when reading code, because you need to maintain a current "stack" in your mind (for example, entering conditional statements, entering loops, etc.).
**Exception:** The nesting levels of lambda expressions, local classes, and anonymous classes in functions are calculated based on the innermost function, and the nesting levels of enclosing methods are not accumulated.
To avoid a confusion of code readers functional decomposition should be done. This will help reader to switch between context.

### <a name="r5.1.3"></a>Rule 5.1.3 Avoid usage of nested functions
Nested functions are making function context more complex and can cause confusion of a reader. 
Also visibility context of nested functions is not obvious for the reader of the code.
Bad example:
```kotlin
fun foo() { 
    fun nested():String { 
        return "String from nested function" 
    } 
    println("Nested Output: ${nested()}") 
} 
```  

### <a name="c5.2"></a> Function arguments
### <a name="r5.2.1"></a>Rule 5.2.1 The lambda parameter of the function should be placed in the last place in the argument list

With a such notation it is easier to use curly brackets, code becoming more readable.
Good example: 
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

### <a name="r5.2.2"></a>Rule 5.2.2 Number of parameters of function should be limited to 5

Long argument list - is a code smell that makes code less reliable. If there are more than 5 parameters, then maintenance is becoming more difficult and merging of conflicts is starting to be much more complex.
It is recommended to reduce the number of parameters. 

If groups of parameters appear in different functions multiple times, it means that these parameters are closely related and can be encapsulated into a single data class.
It is recommended to use data classes and Maps to unify these function arguments.

### <a name="r5.2.3"></a>Rule 5.2.3 Use default values for function arguments instead of overloading them
In Java default values for function arguments were prohibited. That's why each time when it is needed to create a function with less arguments it should be overloaded.
In Kotlin you can use default arguments instead.

Bad example:
```kotlin
private fun foo(arg: Int) {
    // ...
}

private fun foo() {
    // ...
}
``` 

Good example:
```kotlin
 private fun foo(arg: Int = 0) {
     // ...
 }
``` 


# <a name="c6"></a>6 Classes, interfaces and functions
### <a name="c6.1"></a>6.1 Classes
### <a name="r6.1.1"></a> Rule 6.1.1: Primary constructor should be defined implicitly in the declaration of the class
In case class contains only one explicit constructor - it should be converted to implicit primary constructor.
Bad example:
```kotlin
class Test {
    var a: Int
    constructor(a: Int) {
        this.a = a
    }
}
```

Good example:
```kotlin
class Test(var a: Int) { 
    // ...
}

// in case of any annotations or modifer used on constructor:
class Test private constructor(var a: Int) { 
    // ...
}
```

### <a name="r6.1.2"></a> Rule 6.1.2: Prefer data classes instead of classes without any functional logic
Some people say that data class - is a code smell. But in case you really need to use it and your x1code is becoming more simple because of that -
you can use Kotlin `data classes`. Main purpose of these classes is to hold data.
But also `data classes` will automatically generate several useful methods:
- equals()/hashCode() pair;
- toString()
- componentN() functions corresponding to the properties in their order of declaration;
- copy() function

So instead of using `normal` classes:
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

Prefer:
```kotlin
data class Test1(var a: Int = 0, var b: Int = 0)
```

Exception #1: Note, that data classes cannot be abstract, open, sealed or inner, that's why these types of classes cannot be changed to data class.
Exception #2: No need to convert a class to data class in case this class extends some other class or implements an interface.

### <a name="r6.1.3"></a> Rule 6.1.3: Do not use the primary constructor if it is empty and has no sense
The primary constructor is part of the class header: it goes after the class name (and optional type parameters).
But in is useless - it can be omitted. 

Bad example:
```kotlin
// simple case that does not need a primary constructor
class Test() {
    var a: Int = 0
    var b: Int = 0
}

// empty primary constructor is not needed here also
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

Good example:
```
// the good example here is a data class, but this example shows that you should get rid of braces for primary constructor
class Test {
    var a: Int = 0
    var b: Int = 0
}
```

### <a name="r6.1.4"></a> Rule 6.1.4: several init blocks are redundant and generally should not be used in your class
The primary constructor cannot contain any code. That's why Kotlin has introduced `init` blocks.
These blocks are used to store the code that should be run during the initialization of the class.
Kotlin allows to write multiple initialization blocks that are executed in the same order as they appear in the class body.
Even when you have the (rule 3.2)[#s3.2] this makes code less readable as the programmer needs to keep in mind all init blocks and trace the execution of the code.
So in your code you should try to use single `init` block to reduce the complexity. In case you need to do some logging or make some calculations before the assignment 
of some class property - you can use powerful functional programming. This will reduce the possibility of the error, when occasionally someone will change the order of your `init` blocks. 
And it will make the logic of the code more coupled. It is always enough to use one `init` block to implement your idea in Kotlin.

Bad example:
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

Good example:
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

Also - init block was not added to Kotlin to help you simply initialize your properties it is needed for more complex tasks. 
So if `init` block contains only assignments of variables - move it directly to properties, so they will be correctly initialized near the declaration.
In some case this rule can be in clash with [6.1.1](#r6.1.1), but that should not stop you.
Bad example:
```kotlin
class A(baseUrl: String) {
    private val customUrl: String
    init {
        customUrl = "$baseUrl/myUrl"
    }
}
```

Good example:
```kotlin
class A(baseUrl: String) {
    private val customUrl = "$baseUrl/myUrl"
}
```

### <a name="r6.1.5"></a> Rule 6.1.5: Explicit supertype qualification should not be used if there is not clash between called methods
This rule is applicable for both interfaces and classes.

Bad example:
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

### <a name="r6.1.6"></a> Rule 6.1.6: Abstract class should have at least one abstract method
Abstract classes are used to force a developer to implement some of its parts in its inheritors.
In case when abstract class has no abstract methods - then it was set `abstract` incorrectly and can be converted to a normal class.
Bad example:
```kotlin
abstract class NotAbstract {
    fun foo() {}
    
    fun test() {}
}
```

Good example:
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


### <a name="r6.1.7"></a> Rule 6.1.7: in case of using "implicit backing property" scheme, the name of real and back property should be the same
Kotlin has a mechanism of [backing properties](https://kotlinlang.org/docs/reference/properties.html#backing-properties).
In some cases implicit backing is not enough and it should be done explicitly:
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

In this case the name of backing property (`_table`) should be the same to the name of real property (`table`), but should have underscore (`_`) prefix.
It is one of the exceptions from the [identifier names rule](#r1.2)

### <a name="r6.1.8"></a> Recommendation 6.1.8: avoid using custom getters and setters
Kotlin has a perfect mechanism of [properties](https://kotlinlang.org/docs/reference/properties.html#properties-and-fields).
Kotlin compiler automatically generates `get` and `set` methods for properties and also lets the possibility to override it:
```kotlin 
// Bad example ======
class A {
    var size: Int = 0
        set(value) {
            println("Side effect")
            field = value
        }
        get() = this.hashCode() * 2
}
```

From the callee code these methods look like an access to this property: `A().isEmpty = true` for setter and `A().isEmpty` for getter.
But in all cases it is very confusing when `get` and `set` are overriden for a developer who uses this particular class. 
Developer expects to get the value of the property, but receives some unknown value and some extra side effect hidden by the custom getter/setter. 
Use extra functions for it instead.

Good example:
```kotlin 
// Bad example ======
class A {
    var size: Int = 0
    fun initSize(value: Int) {
        // some custom logic
    }
    
    fun goodNameThatDescribesThisGetter() = this.hashCode() * 2
}
```

### <a name="r6.1.9"></a> Rule 6.1.9: never use the name of a variable in the custom getter or setter (possible_bug)
Even if you have ignored [recommendation 6.1.8](#r6.1.8) you should be careful with using the name of the property in your custom getter/setter
as it can accidentally cause a recursive call and a `StackOverflow Error`. Use `field` keyword instead.

Very bad example:
```kotlin
    var isEmpty: Boolean
        set(value) {
            println("Side effect")
            isEmpty = value
        }
        get() = isEmpty
```

### <a name="s6.1.10"></a> Recommendation 6.1.10 no trivial getters and setters are allowed in the code
In Java - trivial getters - are getters that are simply returning the value of a field.
Trivial setters - are simply setting the field with a value without any transformation.
But in Kotlin trivial getters/setters are generated by the default. There is no need to use it explicitly for all types of data-structures in Kotlin.

Bad example:
```kotlin
class A {
    var a: Int = 0 
    get() = field
    set(value: Int) { field = value }

    //
}
```

Good example:
```kotlin
class A {
    var a: Int = 0 
    get() = field
    set(value: Int) { field = value }

    //
}
```

### <a name="s6.1.11"></a> Rule 6.1.11: use apply for grouping object initialization
In the good old Java before functional programming became popular - lot of classes from commonly used libraries used configuration paradigm.
To use these classes you had to create an object with the constructor that had 0-2 arguments and set the fields that were needed to run an object.
In Kotlin to reduce the number of dummy code line and to group objects [`apply` extension](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/apply.html) was added:  
 
Bad example:
```kotlin
class HttpClient(var name: String) {
    var url: String = ""
    var port: String = ""
    var timeout = 0
    
    fun doRequest() {}
}

fun main() {
    val httpClient = HttpClient("myConnection")
    httpClient.url = "http://pushkin.com"
    httpClient.port = "8080"
    httpClient.timeout = 100
    
    httpCLient.doRequest()
}   

```

Good example:
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
                url = "http://pushkin.com"
                port = "8080"
                timeout = 100
            }
            .doRequest()
}
```

### <a name="c6.2"></a>6.2 Extension functions
[Extension functions](https://kotlinlang.org/docs/reference/extensions.html) - is a killer-feature in Kotlin. 
It gives you a chance to extend classes that were already implemented in external libraries and help you to make classes less heavy.
Extension functions are resolved statically.

### <a name="s6.2.1"></a> Recommendation 6.2.1: use extension functions for making logic of classes less coupled
It is recommended that for classes non-tightly coupled functions with rare usages in the class should be implemented as extension functions where possible.
They should be implemented in the same class/file where they are used. This is non-deterministic rule, so it cannot be checked or fixed automatically by static analyzer.

### <a name="s6.2.2"></a> Rule 6.2.2: there should be no extension functions with the same name and signature if they extend base and inheritor classes (possible_bug)
As extension functions are resolved statically. In this case there can be a situation when a developer implements two extension functions - one is for the base class and another one for the inheritor.
And that can lead to an issue when incorrect method is used. 
Bad example:
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

### <a name="c6.2"></a>6.3 Interfaces
`Interface`s in Kotlin can contain declarations of abstract methods, as well as method implementations. What makes them different from abstract classes is that interfaces cannot store state.
They can have properties, but these need to be abstract or to provide accessor implementations.

Kotlin's interfaces can define attributes and functions.
In Kotlin and Java, the interface is the main presentation means of application programming interface (API) design, and should take precedence over the use of (abstract) classes.

### <a name="c6.2"></a>6.4 Objects
### <a name="s6.4.1"></a> Rule 6.4.1: Avoid using utility classes/objects, use extensions instead
As described in [6.2 Extension functions](#c6.2) - extension functions - is a very powerful mechanism.
So instead of using utility classes/objects, use it instead.
This allows you to remove the unnecessary complexity and wrapping class/object and to use top-level functions instead.

Bad example:
```kotlin 
    object StringUtil {
        fun stringInfo(myString: String): Int {
            return myString.count{ "something".contains(it) }
        }
    }
    StringUtil.stringInfo("myStr")
```

Good example:
```kotlin
    fun String.stringInfo(): Int {
        return this.count{ "something".contains(it) }
    }

    "myStr".stringInfo()
```

### <a name="s6.4.2"></a> Recommendation 6.4.2: Objects should be used for Stateless Interfaces
Kotlin’s object are extremely useful when we need to implement a some interface from an external library that doesn’t have any state.
No need to use class for such structures.

Good example:
```
interface I {
    fun foo()
}

object O: I {
    override fun foo() {}
}
```
