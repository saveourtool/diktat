
![img](diktat.jpg)

#Content

| Chapter                | Content                                                         |
| ------------------- | ------------------------------------------------------------ |
| [0 Intro](#c0.1)     | [Purpose](#c0.1) [General principles](#c0.2) [Terminology](#c0.3) [Scope](#c0.4) [Exception](#c0.5) |
| [1 Naming](#c1)       | [Identifiers](#c1.1) [Package naming](#c1.2) [Classes, enumeration and interfaces](#c1.3) [Functions](#c1.4) [Constants](#c1.5) [Variables](#c1.6) |
| [2 注释](#c2)       | [Kdoc](#c2.1) [文件头](#c2.2)、[函数头](#c2.3)、[代码](#c2.4)、[注释的语言](#c2.5) |
| [3 排版格式](#c3)   | [大括号](#c3.1) [缩进](#c3.2) [行宽](#c3.3) [换行](#c3.4) [空白](#c3.5) [枚举](#c3.6) [变量](#c3.7) [数组](#c3.8) [when表达式](#c3.9) [注解](#c3.10) [注释排版](#c3.11) [修饰符](#c3.12) |
| [4 变量和类型](#c4) | [变量](#c4.1) [类型](#c4.2)|
| [5 函数](#c5)      | [函数设计](#c5.1) [函数参数](#c5.2)|
| [6 类和接口](#c6)   | [类](#c6.1) [接口](#c6.2) |
| [7 Kotlin-Java互操作](#c7)   | [Java(用于Kotlin调用)](#c7.1) [Kotlin(用于Java调用)](#c7.2) |
| [附录](#appendix)   | [参考](#reference) [贡献者](#contributor)|


 # <a name="c0"></a> Foreword

 ## <a name="c0.1"></a>Purpose of this document   

When you want to call your code "good" you always think that this code is 
  1) simple
  2) maintainable
  3) reliable
  4) testable
  5) efficient
  6) portable
  7) reusable
  
 Programming is a creative job. 
 This specification is used as a guide for software developers to grow up good programming habits and to help developers to 
 write consistent, easy-to-read, high-quality code, so it will improve product competitiveness software development efficiency.

## <a name="c0.2"></a> General principles

Even being very modern and advanced programming language, but absolutely as the same as with other languages，Kotlin follow general principles:

1. Clarity first. Clarity is a necessary feature of programs that are easy to maintain and refactor.

2. Simplicity is great and beautiful. Such simple code  is easy to understand and easy to implement.

3. Consistency. Unification is great when people that work in the same team and do the same project have same style habbits.
In this case it is much easier to change, review and understand code of your teammate.  

In addition while programming on Kotlin need to pay attention on the following aspects:

1.Try to write clean and simple Kotlin code

Kotlin combines two main programming paradigms: functional and object oriented.
Both them are trusted and well known software engineering practices. Kotlin is young language and it stands on the shoulders 
of such mature languages as Java, C++, C#, Scala. That's why Kotlin introduces many features that help to write cleaner, more readable code and reduces annoying code structures. 
For example: type and null safety, extension functions, infix syntax, immutability, val/var differentiation, expression-oriented features, when statements, much easier work with collections,
type autocoversion and other syntactic sugar.

  
2.Follow Kotlin idioms

Kotlin author Andrey Breslav mentioned that Kotlin language is pragmatic and practical, not academic.
Being prgmatic means that you can easily transform the ideas in people's minds into real working software. This programming language is more and more closer to natural languages than it's ancestors.
Kotlin's design principles are [readability, reusability, interoperability, security, tool-friendliness]
 (https://blog.jetbrains.com/kotlin/2018/10/kotlinconf-2018-announcements/).

3.Use Kotlin efficiently

Some features of Kotlin help to write higher performance code: including rich coroutine library, sequences, inline functions/classes, arrays of basic types, tailRec, CallsInPlace of contract, etc.
 ## <a name="c0.3"></a> Terminology   

**Rules**: conventions that should be followed when programming.

**Recommendations**: Conventions that should be considered when programming.

**Explanation**: Provide necessary explanations for this rule / recommendation.

**Example**: Give examples of this rule / suggestion from good and bad aspects.

 The version of Kotlin to which this specification applies, unless otherwise specified, applies to versions 1.3 and above.

## <a name="c0.4"></a>Scope

This specification applies to all software coded in Kotlin language within the company.

## <a name="c0.5"></a>Exception

Whether it is a 'rule' or a 'recommendation', you must understand the reason why it is needed and why it's better for you to use it.
However, there may be exceptions to some rules and recommendations.

Depending on your situation or personal habits in your project you can violate part of these rules.
But remember that one exception leads to many and can completely destroy the consistency of the code. 
There should be very few exceptions to the 'rules'. It is strongly recommended to follow both 'rules' and 'recommendations'. 

When modifying open source code or third-party code, you may not use the existing specifications and follow the style used by open source code or third-party code to save a unified style.

Software based directly on the interface of the Android native operating system interface, such as the Android Framework, remains consistent with the Android style.

## <a name="c1"></a>1 Naming
Naming your variables/functions/classes e.t.c meaningfully and appropriately is a difficult thing in programming.
You can call your naming good when your code can express the idea and it's functionality clearly, avoid misleading, 
avoid unnecessary coding and decoding, do not use magic numbers, do not use inappropriate abbreviations.
### <a name="r1.1"></a>Rule 1.1: 
Rule 1.1 The source file encoding format (including comments) must be UTF-8 only.
The ASCII horizontal space character (0x20, that is, space) is the only white space character allowed. Tabs are not used for indentation..

### <a name="c1.1"></a>Identifiers

### <a name="r1.2"></a> Rule 1.2:
1. All identifiers should use only ASCII letters or digits, and the names should match regular expressions \w{2,64}

Explanation: Each valid name of identifier should match regular expression \ w {2,64}. 
{2,64} means that the length of the name is from 2 to 64 characters, also the length of the variable name should be proportional to its life range,
 functionality and responsibility. It depends on the project, but generally it is suggested to have length of names less than 31 symbols. Otherwise
 for example class declaration with generics or inheritance from a super class can cause line breaking.
No special prefix or suffix should be used in these names. For example, the following are inappropriate: name_, mName, s_name, and kName.

2. For files choose names that describe the content of this file, use camel case (PascalCase) and .kt extension. 

3. Typical examples of camel case naming：

| Meaning | Correct |Incorrect|
| ---- | ---- | ---- |
| "XML Http Request" | XmlHttpRequest | XMLHTTPRequest |
| "new customer ID" | newCustomerId | newCustomerID |
| "inner stopwatch" | innerStopwatch | innerStopWatch |
| "supports IPv6 on iOS" | supportsIpv6OnIos | supportsIPv6OnIOS |
| "YouTube importer" | YouTubeImporter | YoutubeImporter |

4. Usage of `` and free naming for functions and identifiers are prohibited. For example - not recommended to use: 
    ```kotlin
       val "my dummy name-with-minus" = "value" 
   ```
   The only exception can be - is function names in Unit tests.


 **Exceptions**
 - i,j,k variables that are used in loops is a standard for industry. It is allowed to use 1 symbol for such variables.
 - e variable can be used for catching exceptions in catch block: `catch (e: Exception) {}`
 - Java community generally suggests not to use prefixes, but in case of developing code for Android you can use s prefix for static fields
  and m for non-public non-static fields. PLease note that prefixing can also do harm for the style together with auto generation of getters and setters.

| Type | Naming style |
| ---- | ---- |
| Interfaces, classes, annotations, enumerated types, object type names | Camel case starting with capital letter, test classes have Test suffix, the filename is 'TopClassName'.kt  |
| Class fields, local variables, methods, method parameters| Camel case starting with small letter, test methods may be underlines with '_'|
| Static constants, enumerated values | Only uppercase underlined with '_' |
| Generic type variable | Single capital letter, can be followed by a number, for example: E, T, U, X, T2 |
| Exceptions | Same as class names, but with suffix Exception, like: AccessException, NullPointerException, e.t.c|

### <a name="c1.2"></a>Packages naming

### <a name="r1.3"></a> Rule 1.3: package name is in lower case and separated by dots, code developed internally in your company should start with your.company.domain, and the package name is allowed to have numbers

Package names are all lowercase, consecutive words are simply concatenated together (no underscores) plus it should contain product name and module name.
Also it should contain department or team name to prevent conflicts with other teams.
Package names are allowed to have numbers, like org.apache.commons.lang3, xxx.yyy.v2.

**Exceptions：** 

- In some special cases, such as open source projects or commercial cooperation, package naming should not start with your.company.domain
- In some cases, if the package name starts with a number or other characters, but these characters cannot be used at the beginning of the Java/Kotlin package name, or the package name contains reserved Java keywords, underscores are allowed.
   For example: `org.example.hyphenated_name`,` int_.example`, `com.example._123name`

Valid example: 
 ```kotlin
 your.company.domain.mobilecontrol.views
 ```

### <a name="c1.3"></a> Classes, enumerations and interfaces

 ### <a name="r1.4"></a> Rule 1.4: Classes, enumerations, and interface names use camel case nomenclature
1. The class name is usually a noun or a phrase with a noun, using the camel case nomenclature, like: UpperCamelCase.  For example: Character or ImmutableList.
The name of an interface can also be a noun or a phrase with a noun (such as List), but sometimes it can be an adjective or phrase with adjectives (such as Readable). 
Note, that the naming of classes should not use verbs, but should use nouns, such as Customer, WikiPage, and Account; but try to avoid vague words like Manager, Process, e.t.c
2. Test classes start with the name of the class they are testing and end with Test. For example, HashTest or HashIntegrationTest.

Incorrect examples：
```kotlin
class marcoPolo {} 
class XMLService {} 
interface TAPromotion {}
class info {}
```

Correct version：
```kotlin
class MarcoPolo {}
class XmlService {}
interface TaPromotion {}
class Order {}
```

### <a name="c1.4"></a>Functions

### <a name="r1.5"></a> Rule 1.5: function names should be in camel case

 1) The function name is usually a verb or verb phrase, and uses the camel case nomenclature lowerCamelCase. For example, sendMessage, stopProcess or calculateValue

The format is as follows:

 a) In case of getting, modifying or calculating some value: ```get + non-boolean field()``` 
 But note, that getters are automatically generated by Kotlin compiler for some classes and it is preferred to use special get syntax for fields:
     ```kotlin
     private val field: String
         get() {
         }
     ```
 Also it is preferred to call property access syntax instead of calling getter directly (in this case Kotlin compiler will automatically call corresponding getter)
 
 b) is + boolean variable name()

 c) set + field/attribute name(). But note, that Kotlin has absolutely same syntax and code generation as described for getters in point a

 d) has + Noun / adjective ()

 e) verb()
    The verb is mainly used on the object of the action itself, such as document.print ();

 f) verb + noun() 

g) Callback function allows preposition + verb form naming, such as: onCreate(), onDestroy(), toString()

Bad examples:

 ```kotlin
   fun type(): String
   fun Finished(): Boolean
   fun visible(boolean)
   fun DRAW()
   fun KeyListener(Listener)
 ```

Correct examples：

 ```kotlin
   fun getType(): String
   fun isFinished(): Boolean
   fun setVisible(boolean)
   fun draw()
   fun addKeyListener(Listener)
 ```

2)An underscore may be included in the JUnit test function name, underscore should be a logical component that is used to separate names,
 and each logical part is written in lowerCamelCase.
 For example: a typical pattern <methodUnderTest> _ <state>, such as pop_emptyStack.
### <a name="c1.5"></a> Constants 

### <a name="r1.6"></a> Rule 1.6 Constant names should be in UPPER case, words separated by underscore

1. Constants are attributes created with const keyword, or top-level/"val" local variables of an object that hold immutable data.
 In most cases constants can be identified as const val property from object/companion object/file top level
 These variables contain fixed constant value that generaly should never be changed by a programmer.
 This includes basic types, strings, immutable types and immutable collections of immutable types. If any state of an object can be changed, then this is not a constant.

2. Constant names should contain only UPPERCASE letters, separated by underscores. They should have val or const val modifier to explicitly make them final.

3. Such objects that have immutable content like Logger, Lock, e.t.c. can be in uppercase as constant or can have camel case as regular variables.

4. Don't use magic numbers, use meaningful constants instead. SQL or logging strings should not be treated as "magic numbers" and should not be defined as string constants;
 "Magic constants" like NUM_FIVE = 5 or NUM_5 = 5 should not be treated as constants. Because in case someone will change it to NUM_5 = 50 or 55 it will be easy to make a mistake. 
 In general, such constants can represent business logic values like measures, capacity, scope, location, tax rate, promotional discounts, power base multiples in the algorithm, etc.
 How to avoid magic numbers:
 - use library functions and APIs. For example, instead of checking that size == 0 use isEmpty() function. To work with time use built-ins from java.time API.
 - enumerations can be used for naming patterns, see [Recommended usage scenario for enumeration in 3.9] (# s3.9)
 
Bad example:

 ```kotlin
 var int MAXUSERNUM = 200;
 val String sL = "Launcher";
 ```

Correct example：

 ```kotlin
 const val int MAX_USER_NUM = 200;
 const val String APPLICATION_NAME = "Launcher";
 ```

### <a name="c1.6"></a> Non-constant fields

 ### <a name="r1.7"></a> Rule 1.7: The name of the non-constant field should use camel case and starts with lower letter. 

Variable, attribute, parameter and other non-constant field names should usually be nouns or phrases with noun.
These fields should use lowerCamelCase formatting.

Even if a local variable is final and immutable but it cannot be treated as a constant then it should not be using same rules as described for constants.
Name of variables with a type from collections (sets, lists, e.t.c) should contain nouns in plural form. For example: ```var namesList: List<String>```

Name of non-constant variable should use lower camel case. The name of the final immutable field that is used to store the singleton object can use the same notation with camel case.

Bad examples：

 ```kotlin
 customername: String
 user: List<String> = listof()
 ```

Correct examples：

 ```kotlin
var customerName: String
val users: List<String> = listOf();
val mutableCollection: MutableSet<String> = HashSet()
 ```

### <a name="s1.1"></a> Recommendation 1.1: Avoid using boolean variable names with negative meaning 

Note: When using logical operator and the name that has negative meaning, there will be problems of understanding, so called "double negative". It is not straight forward to understand what !isNotError means .
The JavaBeans specification automatically generates isXxx () getters for attributes of boolean classes. But it is not mandatory for all such methods that return boolean to have such notation.
For Boolean local variables or methods, it is highly recommended to add non-meaningful prefixes, including `is` which is commonly used by JavaBeans, or` has`, `can`,` should`, `must`.
Modern IDEs (like Intellij) already do that for you when you try to generate getters in Java. For Kotlin it is even easier as everything is on byte-code level under the hood. 

Bad examples：

```kotlin
val isNoError: Boolean
val isNotFound: Boolean
fun empty()
fun next();
```

Correct examples：

```kotlin
val isError: Boolean
val isFound: Boolean
val hasLicense: Boolean
val canEvaluate: Boolean
val shouldAbort: Boolean
fun isEmpty()
fun hasNext()
```

 ## <a name="c2"></a>Chapter 2  comments

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
## <a name="c2.2"></a>Comments to the file header

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

- It is forbidden to have empty comment blockst.
     As in the above example, if there is no content after the option `@apiNote`, the entire tag block should be deleted.

- Industry is not using any history information in comments. History can be found in VCS (git/svn/e.t.c). It is not recommended to include historical data in the comments of the Kotlin source code.


## <a name="c2.3"></a>Function header comments

### <a name="r2.5"></a>Rule 2.5 Prohibit empty or useless function comments

Function header comments are placed above function declarations or definitions. There should be no newline between a function declaration and it's Kdoc. 
Use the above [KDoc](#c2.1) style rules.  

In Chapter 1 of current code style we stated that function name should self commend it's functionality as much as possible. So in the Kdoc try to mention things that are not stored in function name.
Avoid dummy useless comments. 

The function header comment content is optional, but not limited to: function description, return value, performance constraints, usage, memory conventions, algorithm implementation, reentrant requirements, etc.
The module's external interface declaration and its comments should clearly convey important and useful information.

## <a name="c2.4"></a>Code comments

### <a name="r2.6"></a>Rule 2.6 Add a blank line between the body of the comment and Kdoc tag-blocks; there must be 1 space between the comment character and the content of the comment; there must be a newline between a Kdoc and the previous code above
1. Add a blank line between the body of the comment and Kdoc tag-blocks; there must be 1 space between the comment character and the content of the comment; there must be a newline between a Kdoc and the previous code above.
 No need to add a blank line before a first comment in this particular name space (code block), for example between function declaration and first comment in a function body.  

Examples: 
```kotlin
/** 
 * This is the short overview comment for the example interface.
 *                   /* Add a blank line between the general comment text and each KDoc tag */
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
      *                /* 注释正文与其下的各个KDoc tag之间加1个空行 */
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

 ## <a name="c3"></a>3 Typesetting

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

### <a name="s3.1"></a>Recommendation 3.1: import statements should appear in the following order: Android, internal company imports, external commercial organizations, other open source third parties, net/org open source organizations, and finally java core dependencies. Each group should be separated by a blank line.

Note: Static imports are placed above all other imports (using the same sorting method as for regular imports). 
From top to bottom, the order is the following:
1. static imports
2. Android
3. Imports of packages used internally in your organization
4. Other commercial organizations
5. Open source imports
6. net/org open source dependencies
7. javacard
8. java core packages

Each category sorted in alphabetical order. This style is compatible with [Android import order] (https://source.android.com/setup/contribute/code-style#order-import-statements). 

Recommended example：

```java
import static all.other.imports; // static imports

import android.*; // android

import androidx.*; // android

import com.android.*; // android

import your.company.*; // your company's libs

import com.your.company.*; // your company's libs

import com.google.common.io.Files; // other business organizations

import lombok.extern.slf4j.Sl4j;  // Other open source third parties

import maven.*;  // Other open source third parties

import net.sf.json.*; //  open source organization starting with .net

import org.linux.apache.server.SoapServer; // open source organization starting with .org

import javacard.*;

import java.io.IOException; // java core packages
import java.net.URL;

import java.rmi.RmiServer;  // java core packages
import java.rmi.server.Server;

import javax.swing.JPanel;  // java extensions 
import javax.swing.event.ActionEvent;
```

### <a name="s3.2"></a>>Recommendation 3.2: The declaration part of a class-like code structures (class/interface/e.t.c) should be in the following order: compile-time constants (for objects), class properties, late-init class properties, init-blocks, constructors, public methods, internal methods, protected methods, private methods, companion object. Their declaration should be separated by blank lines.

Notes:
1. There should be no blank lines between properties without comments;
2. Properties with comments/Kdoc should be separated by a newline before the comment/Kdoc

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

 #### <a name="r3.3"></a>Rule 3.3 Braces must be used in conditional statements and loop blocks

1) In `if`, `else`, `for`, `do`, and `while` statements, even if the program body is empty or contains only one statement, braces should be used.
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

2) No need to use braces for the body of lambdas and when-conditions (after an arrow)
  
Bad example:

```kotlin
val a = { b: String, c: String -> { // these braces are increasing complexity
        null
    }
}
```

Valid examples: 

```kotlin
val a = { b: String, c: String ->
        null
}

someValue.map { x -> x}
```

#### <a name="r3.4"></a> Rule 3.4 For *non-empty* blocks and block structures, the opening brace is placed at the end of the line

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

#### <a name="s3.3"></a>Recommendation 3.3: try to avoid empty blocks; multiple braces should start a new line

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

### <a name="c3.4"></a> Code lines

### <a name="s3.4"></a> Recommendation 3.4 No more than one statement per line
There should not be more than one code statement in one line (this recommendation prohibits usage of code with ";")
Such code is prohibited as it makes code visibility worse:
```kotlin
val a = ""; val b = ""
```

### <a name="c3.5"></a>Line width

### <a name="s3.5"></a> Recommendation 3.5: line length should be less than 120 symbols

This international code style prohibits non-latin (non ASCII) symbols in the code. But in case you would like to use them anyway - please use the following convention:

- One wide character occupies the width of two narrow characters.
  The "wide" and "narrow" of a character are defined by its [*east asian width* Unicode attribute] (https://unicode.org/reports/tr11/).
  Generally, narrow characters are also called "half-width" characters. All characters in the ASCII character set include letters (such as `a`, `A`), numbers (such as `0`, `3`), and punctuation (such as `, `, `{`), spaces. All of them are narrow characters.
  Wide characters are also called "full-width" characters, Chinese characters (such as `中`, `文`), Chinese punctuation (`, `, `, `), full-width letters and numbers (such as `Ａ`、`３`) are all. These characters counted as 2 narrow characters.
  
- Any line that exceeds this limit (120 narrow symbols) should be wrapped, as described in the *Newline* section. 

 **Exceptions：**

  1. Long URL or long JSON method reference in KDoc.
  2. The `package` and `import` statements.
  3. The command line in the comment so that it can be cut and pasted into the shell for use.

### <a name="c3.4"></a> Line breaks (newlines)

#### <a name="r3.6"></a>Rule 3.6 line break style rules if the line is split

1. Compared to Java Kotlin allows not to put semicolon (';') after each statement separated by newline.
 There should be no redundant semicolon at the end of lines.
 
 In case when newline is needed to split the line, it should be placed after operators like &&/||/+/e.t.c
 But newline should be placed before operators like ('.', '?.', '?:', '::', e.t.c).
 
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
3. The name of a function or constructor should not be split by a newline from the opening brace '('. Brace follows immediately after the name without any spaces.   
4. Newline should be placed right after the comma (',')
5. In lambda statements, if it's body contains more than one line, the newline should be placed after an arrow. See examples below:

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

### <a name="c3.5"></a>Blank lines

### <a name="s3.6"></a>Recommendation 3.6: Reduce unnecessary blank lines and keep the code compact

By reducing unnecessary blank lines, you can display more code one one screen and that makes the code more readable.

- Blank lines should separate content based on it's relevance: blank lines should be placed between groups of fields, constructors, methods, nested classes, init blocks, objects
- Do not use more than one single line inside methods, type definitions, initialization expressions
- Generally do not use more than two consecutive blank lines in a row
- Do not put newlines in the beginning or at the end of code blocks with curly braces:

```kotlin
fun baz() {
        
    doSomething();  // No need to add blank lines at the beginning and at the end of the code block
    // ...

}
```

### Horizontal space

### <a name="s3.7"></a> Recommendation 3.7: Usage of whitespace for code separation

  1. Any keywords (like 'if', 'when', 'for', e.t.c) should be separated from with a single whitespace from the openning parenthesis.
     Only exceptions are: 'super' and 'constructor' keywords. They should not be separated from a parenthesis.

  2. Separate any keywords (such as `else` or `catch`, etc.) from the openning brace ('{') with a single whitespace.
  
  3. Use single whitespace before any opening whitespace (`{`).
     Only exception is passing of a lambda:
     ```kotlin
         private fun foo(a: (Int) -> Int, b: Int) {}
         foo({x: Int -> x}, 5) // no space before '{'
     ```

  4. Single whitespace should be placed on both sides of binary operators. Also this applies to operator-like symbols, for example: 

     - In generic structures with 'where' keyword： `where T : Type`
     - With arrow in lambdas： `(str: String) -> str.length()`

  **Exceptions：**

```kotlin
- Two colons (`::`) are written without spaces: `Object::toString`
- Dot separator (`.`) that stays on the same line with an object name `object.toString()`
```

  5. Spaces should used after ','/':',';' (except cases when those symbols are in the end of line). There should be no whitespaces in the end of line.

  6. There should be *only one space* between identifier and it's type： `list: List<String>`
  
  7. When using '[]' operator (get/set) there should be *no* spaces between identifier and '[': `someList[0]`
  
  8. There should be no space between a method name and a parenthesis: `foo() {}`

### <a name="s3.8"></a>Recommendation 3.8: No spaces should be inserted for horizontal alignment

*Horizontal alignment* - is a practice to add additional spaces in the code, to align code blocks on the same level with previous code blocks.

- It always takes time to format and support such code and fix alignment for new developers in case they need to change something in aligned code.
- Long identifier names will break the alignment and will make the code less presentable.
- The disadvantages of alignment are greater than the benefits; in order to reduce maintenance costs and not cause trouble, misalignment is the best choice.

Recommendation: the only exception where can look good is `enum class`, where you can use alignment (in table format) to make code more readable:
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

 #### <a name="c3.7"></a> Variable declaration

### <a name="r3.7"></a>Rule 3.7: declare one variable on one line

Each property or variable declaration should be declared on separate line. Bad example: `val n1: Int; val n2: Int`.

### <a name="s3.10"></a>Recommendation 3.10: Variables are declared close to the line where they are first used
Local variables are declared close to the point where they are first used. THis will minimize their scope.
Local variable declarations are usually initialized or initialized immediately after the declaration. The member fields of the class should be declared collectively. 

### <a name="c3.11"></a>When

### <a name="r3.10"></a>Rule 3.10: 'when' statement must have else branch, unless when condition variable is enumerated or sealed type
Each when statement contains an `else` statement group, even if it does not contain any code.

*Exception:* If a when statement of type `enum or sealed` contains all values of a enum - there is no need to have "else" branch.
The compiler can issue a warning when it is missing.

### <a name="c3.12"></a> Annotations

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
### <a name="c3.13"></a> Comments layout

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

### <a name="c3.14"></a> Modifiers

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
The following table contains some confusing characters. You should be careful when using them as identifiers. 
And better use other names instead of these identifiers.

| Expected      | Confusing name           | Suggested name |
| ------------- | ------------------------ | ---------------- |
| 0 (zero)      | O, D                     | obj, dgt         |
| 1 (one)       | I, l                     | it, ln, line     |
| 2 (two)       | Z                        | n1, n2           |
| 5 (five)      | S                        | xs, str          |
| 6 (six)       | e                        | ex, elm          |
| 8 (eight)     | B                        | bt, nxt          |
| n,h           | h,n                      | nr, head, height |
| rn, m         | m,rn                     | mbr, item        |

### <a name="r3.8"></a>Rule 3.8: Concatenation of Strings is prohibited, use raw strings and string templates instead.
Kotlin significantly enhanced work with Strings:
[String templates](https://kotlinlang.org/docs/reference/basic-types.html#string-templates), [Raw strings](https://kotlinlang.org/docs/reference/basic-types.html#string-literals)
That's why code looks much better when instead of using explicit concatenation to use proper Kotlin strings.

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