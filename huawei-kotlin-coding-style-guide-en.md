<!-- <center><br><br><br><br><br><br><br> -->
<big><big><big><big><big><big><big><strong>Kotlin programming language code style</strong>
V1.0</big></big></big></big></big></big></big>
<!-- <br><br><br><br><br><br><br><br><br><br><br><br><br><br> -->

![img](/img/huawei-logo-small.jpg)

<big><big><big><big><big>Copyright Huawei Technologies Co., Ltd.</big></big></big></big><big>
<!-- <br><br><br></center> -->



#目录

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

### <a name="r1.3"></a> Rule 1.3: package name is in lower case and separated by dots, code developed internally in Huawei should start with com.huawei, and the package name is allowed to have numbers

Package names are all lowercase, consecutive words are simply concatenated together (no underscores) plus it should contain product name and module name.
Also it should contain department or team name to prevent conflicts with other teams.
Package names are allowed to have numbers, like org.apache.commons.lang3, xxx.yyy.v2.

**Exceptions：** 

- In some special cases, such as open source projects or commercial cooperation, package naming should not start with com.huawei
- In some cases, if the package name starts with a number or other characters, but these characters cannot be used at the beginning of the Java/Kotlin package name, or the package name contains reserved Java keywords, underscores are allowed.
   For example: `org.example.hyphenated_name`,` int_.example`, `com.example._123name`

Valid example: 
 ```kotlin
 com.huawei.mobilecontrol.views
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

The content and format of the copyright license must be as follows, the Chinese version:
`版权所有 (c) 华为技术有限公司 2012-2020` 
English version:
`Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.`

Regarding the release notes, see examples below:

-2012-2020 can be modified according to actual needs. 2012 is the year the file was first created, and 2020 is the year the file was last modified. The two can be the same, such as "2020-2020".
  When there are major changes to the file such as feature extensions, major refactorings, etc, then the subsequent years must be updated.
- -The copyright statement can use Huawei subsidiaries.
  On Chinese：版权所有 (c) 海思半导体 2012-2020
  On English：Copyright (c) Hisilicon Technologies Co., Ltd. 2012-2020. All rights reserved.

Copyright should not use KDoc style or single line style comments, it must start from the beginning of the file.
Example of a minimal Copyright comment without other functional comments:

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

 ## <a name="c3"></a>3  排版

### <a name="c3.1"></a>  文件

### <a name="r3.1"></a>规则3.1 避免文件过长，不超过2000行（非空非注释行）

过长的文件往往意味着文件（模块）功能不单一，过于复杂。  
建议根据职责进行横向拆分，或根据层次进行纵向分层。
不需人工维护的文件，则可以例外。如工具自动生成的临时代码。

### <a name="r3.2"></a>规则3.2 一个源文件按顺序包含版权、package、import、顶层类，且用空行分隔

1. 许可证或版权信息
2. @file注解
3. package语句,且不换行
4. import语句，且不换行，不能用通配符*
5. 顶层类头、顶层函数头注释
6. 一个顶层类 (只有一个)，在一个与它同名的.kt源文件中；或顶层函数；
   以上每个部分之间用一个空行隔开。

### <a name="s3.1"></a>建议3.1 import包应当按照先安卓，华为公司，其它商业组织，其它开源第三方、net/org开源组织、最后java的分类顺序出现，并用一个空行分组

说明：静态导入置于所有其他导入之上（与常规导入一样的排序方式）。Java最基础的包，是指java.base模块中的包，参照[java.base中的包清单](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/module-summary.html) 。
Java的其它包，是指java.base模块之外的[其它SE模块的包](https://docs.oracle.com/en/java/javase/11/docs/api/index.html)。
从上往下，大致分类是，import static、安卓、华为公司com.huawei.\*、其它商业组织com.\*、其它开源第三方xxx.yyy.\*、net/org开源组织、javacard、Java最基础的包、Java的其它包、Java的扩展包。
每一类内部按照字母顺序排序。几大分类也大致是按字母排序的（android,com,net,org），只是java/javax的在最后。
三方开源，包含了商业公司的开源，例如com.alibaba.fastjson，com.intellij.openapi等，与非盈利组织的开源，例如net/org组织的。这儿，“其它”，就是指除了前缀为com,net,org之外的其它三方开源，例如下面的lombok,maven。

这个风格兼容于[安卓的import顺序](https://source.android.com/setup/contribute/code-style#order-import-statements)，如果没有最上面的安卓包，也适用于非安卓。这样广泛应用工具扫描，有利于其准确性。
**工具格式化提示：** com一组，其它开源第三方一组，net/org开源组织一组，java一组，javax一组（不区分基础还是扩展包），组内按字母排序，是允许的。

推荐例子：

```java
import static all.other.imports; // 静态导入

import android.*; // 安卓

import androidx.*; // 安卓

import com.android.*; // 安卓

import huawei.*; // 华为公司

import com.huawei.*; // 华为公司

import com.google.common.io.Files; // 其它商业组织

import lombok.extern.slf4j.Sl4j;  // 其它开源第三方

import maven.*;  // 其它开源第三方

import net.sf.json.*; // 开源组织

import org.linux.apache.server.SoapServer; // 开源组织

import javacard.*;

import java.io.IOException; // Java最基础的包
import java.net.URL;

import java.rmi.RmiServer;  // Java的其它包
import java.rmi.server.Server;

import javax.swing.JPanel;  // Java的扩展包
import javax.swing.event.ActionEvent;
```

### <a name="s3.2"></a>建议3.2  一个类或接口的声明部分应当按照类变量、静态初始化块、实例变量、实例初始化块、构造器、方法的顺序出现，且用空行分隔

说明：1. 对于自注释字段之间可以不加空行； 2. 非自注释字段应该加注释且字段间空行隔开。
一个类或接口的声明部分应当按照以下顺序出现：

 - 属性声明
 - 初始化块
 - 次构造器
 - 方法`或`嵌套类，嵌套类可以与成员方法根据业务逻辑交替出现，把概念上相近的放在一起,  将嵌套类放在紧挨使⽤这些类的代码之后。如果打算在外部使⽤嵌套类，⽽且类中并没有引⽤这些类，那么把它们放到末尾，在伴⽣对象之后。
 - 伴生对象
 - 属性、构造器，均按访问修饰符从大到小排列：public、internal、protected、private

**例外：**
类中的首个声明是LOG控制开关和TAG的，修饰符定义`private val`，允许放在类中的最前面。
包括TAG,mTAG,STAG,DBG,DEBUG,logger,xxxLogger，名字不区分大小写都允许。

 ### <a name="c3.1"></a>  大括号

 #### <a name="r3.3"></a>规则3.3  在条件语句和循环块中必须使用大括号

 在 `if`， `else`， `when`， `for`，`do`和 `while`等语句中，即使程序体是空的或只包含一个语句，也要使用大括号。

**例外:** *仅*当整个表达式适合一行时，可以省略大括号。

不好的例子：

```kotlin
val value = if (string.isEmpty())  // WRONG!
                0
            else
                1
```

推荐例子:

```kotlin
val value = if (string.isEmpty()) 0 else 1  // Okay
```



 #### <a name="r3.2"></a>规则3.4 对于*非空*块和块状结构，左大括号放在行尾

 对于*非空*块和块状结构，大括号遵循K&R风格（俗称*埃及括号*）：

 - 左大括号不换行
 - 右大括号自己单独一行
 - 右大括号后，可以跟逗号、分号等，也可以跟随 `else`, `catch`,`finally`等关键字语句。

 推荐例子：

 ```kotlin
        return () -> {
            while (condition()) {
                method()
            }
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

 对于枚举类的例外，请看 [枚举](#c3.6)。

### <a name="c3.2"></a> 缩进

### <a name="r3.5"></a>规则3.5 使用空格进行缩进，每次缩进4个空格

只允许使用空格(space)进行缩进，每次缩进为 **4** 个空格。不允许插入制表符tab。
当前几乎所有的集成开发环境（IDE）和代码编辑器都支持配置将Tab键自动扩展为**4**空格输入，请配置你的代码编辑器支持使用空格进行缩进。
**例外：** 方法参数换行、字符串+语句换行、方法连续操作符.调用，这些特殊场景，业界流行工具Eclipse,IntelliJ IDEA ，默认缩进8个空格，也是可以的。

#### <a name="s3.3"></a>建议3.3  应该避免空块；多块的右大括号应该新起一行

 空块或块状结构遵循1.3.1.2要求，也可以在打开后立即关闭，在（`{}`）之间没有字符或换行符，**除非**它是 *多块语句的一部分*（ `if/else`或者 `try/catch/finally`等）。

 推荐例子：

 ```kotlin
   // 这是可以接受的
   fun doNothing（）{} 
 
   // 这同样可以接受
   fun doNothingElse（）{ 
   }
 ```

 多块语句，不好的例子：
 ```kotlin
  try {
      doSomething()
  } catch (Exception e) {}
 ```

 多块语句，好：
 ```kotlin
   try {
       doSomething()
   } catch (Exception e) {
   }
 ```

### <a name="c3.4"></a>行内容

### <a name="s3.4"></a>建议3.4 每行不超过一个语句

 每个语句后面都有一个换行符。

### <a name="c3.5"></a>行宽

### <a name="s3.5"></a>建议3.5 每行限长120个窄字符

一个宽字符占用两个窄字符的宽度。除非另有说明，否则任何超出此限制的行都应该换行，如 *换行* 一节中所述。 
每个Unicode代码点都计为一个字符，即使其显示宽度大于或小于。例如，如果使用 [全角字符](https://en.wikipedia.org/wiki/Halfwidth_and_fullwidth_forms)，您可以选择比此规则严格要求的位置更早地换行。
字符的“宽”与“窄”由它的[*east asian width* Unicode属性](https://unicode.org/reports/tr11/)定义。通常，窄字符也称“半角”字符，ASCII字符集中的所有字符，包括字母（如：`a`、`A`）、数字（如：`0`、`3`）、标点（如`,`、`{`）、空格，都是窄字符；
宽字符也称“全角”字符，汉字（如：`中`、`文`）、中文标点（`，`、`、`）、全角字母和数字（如`Ａ`、`３`）等都是宽字符，算2个窄字符。
 	 

>  注：对于代码风格检查工具的开发人员来说，可以简单地判断字符是否在ASCII的范围内，以及是否为中文（可判断其所在的Unicode块），来判断宽窄，这样可以覆盖99%以上的代码，其余字符可以保守地认为是半角，不判定其超出限制。如果需要更精确的判断，可以使用开源的[ICU](http://site.icu-project.org/home)库等。

 **例外：**

 1. KDoc中的长URL或长JSON方法引用。
 2. `package`和 `import`语句。
 3. 注释中的命令行，使它可以剪切并粘贴到shell中使用。

### <a name="c3.4"></a>换行

#### <a name="r3.6"></a>规则3.6 换行起点在点号、双冒号、类型&、catch块中管道之前，在函数左括号、逗号、lambda箭头和其左大括号之后

  1. 与Java不同，Kotlin一般不在结尾写分号，因此换行应出现在 运算符*之后*，例如&&之后，避免语法错误。

    - 但在以下“类似运算符”的符号之前换行：
      - 点分隔符（`.`）
      - 成员引用的两个冒号（`::`）
  2. 赋值符号后
  3. 函数或构造函数名称与紧跟其后面的左括号（`(`）保持在同一行。
  4. 逗号（`,`）断点出现在逗号之后。
  5. 在lambda表达式中，如果箭头后是单个表达式，可以在箭头后面出现断点，如果箭头后是程序块，可以在大括号后出现断点。

    推荐例子：

 ```kotlin
 val lambda =
     (label: String, value: Long) -> {
         // ...
     }
 
 val predicate = str ->
     longExpressionInvolving(str)
 ```

6. 当函数只包含单个表达式时，它可以表示为 [表达式函数](https://kotlinlang.org/docs/reference/functions.html#single-expression-functions)。

   推荐例子:

   ```kotlin
   override fun toString （）：String = "hi"
   override fun toString （）：String { return "Hey" }
   ```


### <a name="c3.5"></a>空白

### <a name="s3.6"></a>建议3.6 减少不必要的空行，保持代码紧凑 

减少不必要的空行，可以显示更多的代码，方便代码阅读。下面有一些建议遵守的规则：

- 根据上下内容的相关程度，合理安排空行：空行出现在字段，构造方法，方法，嵌套类，静态初始化块之间
- 方法内部、类型定义内部、初始化表达式内部，不使用**连续**空行
- 不使用**连续 3 个**空行，或更多
- 大括号内的代码块**行首之前和行尾之后不要加空行**，包括类型和方法定义、语句代码块。

```kotlin
fun foo() {
    // ...
}



fun bar() {  // Bad：最多使用连续2个空行
    // ...
}
```

```kotlin
fun baz() {
        
    doSomething();  // Bad：大括号内部首尾，不需要空行
    // ...

}
```

### 水平空格

### <a name="s3.7"></a>建议3.7 单个空格应该分隔关键字与其后的左括号、与其前面的右大括号，出现在任何二元/三元运算符/类似运算符的两侧，`,:;`或类型转换结束括号`)`之后使用空格。行尾和空行不能有空格space

  1. 将任何关键字（例如 `if`， `for`, `while`, `when`, `try` 或 `catch`）与该行后面的左括号用空格分开,
     **例外：** 方法调用类的`super`, `this`。

  2. 将任何关键字（例如 `else`或 `catch`等）与该行前后大括号与空格分开

  3. 在任何大括号（`{`）之前都使用空格，数组有两个例外：

     - `@SomeAnnotation({a, b})` （注解的数组不需要使用空格）
     - `String[][] xs = {{"foo"}};`（`{{`多维数组之间不需要空格）

  4. 在任何二元或三元运算符的两边。这也适用于以下“类似运算符”的符号：

     - 交类型中的＆符号： `where T : <Foo & Bar>`
     - 处理多个异常的catch块的管道： `catch (FooException | BarException e)`
     - `for`（“foreach”）语句中的冒号（` : `）
     - lambda表达式中的箭头： `(str: String) -> str.length()`

  **例外：**

```kotlin
- `::`方法引用的两个冒号()，写法像`Object::toString`
- 点分隔符（`.`），写法像 `object.toString()`
```

  5. `,:;`或类型转换cast结束括号（`) `）之后使用空格

  6. 在类型注解与`[]`之间

  7. 在声明的类型和变量之间： `list: List<String>`


### <a name="s3.8"></a>建议3.8 不应插入空格水平对齐

 *水平对齐*是在代码中添加可变数量的附加空格的做法，目的是使某些标记直接出现在前一行的某些其他标记下方。
 包括在KDoc注释性的描述内容前不应插入空格对齐。

 - 如果参数/变量名长短差异较大，无规律插入的空白数呈凹凸状，并不美观
 - 如果某个参数/变量名较长，例如gardenPlantingDetailViewModel，对应的描述内容也较长的话，就可能不得不换行，又可能会有换行对齐的顾忌
 - 后续的维护者可能会困扰是否在整个module/package都刻意追求对齐
   因此，对齐的弊大于利；为了减少维护成本，不造成困扰，不对齐是最好的选择。

 推荐例子：

 ```kotlin
 private val nr: Int; // 这可以
 private var color: Color; // 这也可以
 ```

 不好的例子：

 ```kotlin
 private val nr: Int;       // 维护者可能不得不修改这些对齐空格数
 private val color: Color;  // 不必与上行对齐注释 
 ```

###<a name="c3.8"></a> 枚举

### <a name="s3.9"></a>建议3.9 枚举常量间用逗号隔开， 换行可选

 在枚举常量后面的每个逗号之后，换行符是可选的。还允许额外的空白行（通常只有一行）。例如：

 ```Java
    private enum Answer {
        YES {
            @Override
            public fun toString() {
                return "yes";
            }
        },

        NO,
        MAYBE
    }
 ```

 没有方法且没有关于其常量的文档的枚举类，可以选择格式化就好像它是一个数组初始化一样：

 ```java
 private enum Suit { CLUBS, HEARTS, SPADES, DIAMONDS }
 ```

 由于枚举类*是类*，因此适用于格式化类的所有其他规则。
 Java的枚举比较灵活强大，而且与switch/case结合较好，只要可能，优先使用。
 枚举的使用场景：

  - 布尔型的2元素值，例如isCelsius = true | false来表示摄氏|华氏，可用

  ```java
  public enum TemperatureScale { CELSIUS, FAHRENHEIT }
  ```

  - 变量值仅在一个固定范围内变化用 enum 类型来定义。例如 [建议8.6.3的Keyboard](#s8.6.3)例子 
  - 整数或字符串的枚举模式，蕴含有某种名称空间的，例如上面的`Suit`例子，或者其它语言的ComparisonResult，避免-1、0、1的数字比较：

  ```kotlin
    public enum ComparisonResult {
        ORDERED_ASCENDING,
        ORDERED_SAME,
        ORDERED_DESCENDING
    }
  ```

 #### <a name="c3.7"></a> 变量声明

### <a name="r3.7"></a>规则3.7 每行声明一个变量

 每行的变量声明（字段或本地）都只声明一个变量，不好的例子 `val n1: Int; val n2: Int`。

 **例外：**`for`循环标题中可以接受多个变量声明 。

### <a name="s3.10"></a>建议3.10 变量被声明在接近它们首次使用的行

局部变量被声明为接近它们首次使用的点，以最小化它们的范围。局部变量声明通常具有初始化，或在声明后立即初始化。类的成员字段要集中声明。

### <a name="c3.11"></a>when

### <a name="r3.10"></a>规则3.10 when语句要有else分支，除非when的条件变量是枚举或sealed类型

 每个when语句都包含一个`else`语句组，即使它不包含任何代码。
 **例外：**如果`enum或sealed`类型的when语句包含涵盖该类型的*所有*可能值的显式案例，*则*该类型的when语句*可以*省略该`else`语句组。编译器能够在缺少时，发出警告。

### <a name="c3.12"></a>注解

### <a name="s3.11"></a> 建议3.11 应用于类，方法或构造方法的每个注解独占一行

 应用于类，方法或构造方法的注解都在其自己的行上（即，每行一个注解）。例：

 ```java
 @Override
 @Nullable
 public String getNameIfPresent() { ... }
 ```

单个的注解可以和签名的第一行出现在同一行   

```java
@Override public int hashCode() { . . . }
```

应用于字段的多个注解允许与字段出现在同一行   

```java
@Partial @Mock DataLoader loader;
```

### <a name="c3.13"></a>注释排版

  块注释与周围代码的缩进级别相同。它们可以是 `/* ... */`风格或 `// ...`风格。对于多行 `/* ... */`注释，后续行必须以`*`与`*`前一行对齐的方式开始 。
推荐例子：

 ```java
 /*
  * This is          // And so
  * okay.            // is this.
  */
 ```

 **提示：**编写多行注释时，如果希望自动代码格式化程序在必要时重新换行（段落样式），请使用该`/* ... */`样式。

### <a name="c3.14"></a> 修饰符

### <a name="s3.12"></a> 建议3.12   如果⼀个声明有多个修饰符，始终按照以下顺序

推荐的顺序（如果存在）：

 ```kotlin
public / internal / protected / private
expect / actual
final / open / abstract / sealed / const
external
override
lateinit
tailrec
vararg
suspend
inner
enum / annotation
companion
inline
infix
operator
data
 ```

### <a name="s3.13"></a>建议3.13 较长的数字字面值使用下划线分隔

说明： 根据业务含义，较长的数字字面值，使用下划线使数字常量更易读，也更容易发现错误。

```kotlin
val oneMillion = 1_000_000
val creditCardNumber = 1234_5678_9012_3456L
val socialSecurityNumber = 999_99_9999L
val hexBytes = 0xFF_EC_DE_5E
val bytes = 0b11010010_01101001_10010100_10010010
```

 下表是一些易混淆的字符，当一起作为标识符时，需留意，但工具不作扫描。
 表示元素索引下标、数字变量的，还可以考虑用常见的fst,snd,start/end,from/to,mid,idx,pos,size,cap,count,total等。

| 期望的字符    | 易引起混淆的单个字符命名 | 更可读的备选短名 |
| ------------- | ------------------------ | ---------------- |
| 0 (zero)      | O (大写的o), D (大写的d) | obj, dgt         |
| 1 (one)       | I (大写的i), l (小写L)   | it, ln, line     |
| 2 (two)       | Z (大写的z)              | n1, n2           |
| 5 (five)      | S (大写的s)              | xs, str          |
| 6 (six)       | e (小写的E)              | ex, elm          |
| 8 (eight)     | B (大写的b)              | bt, nxt          |
| n (小写的N)   | h (小写的H)              | nr, head, height |
| rn (小写的RN) | m (小写的M)              | mbr, item        |

 # <a name="c4"></a>4 变量和类型

### <a name="c4.1"></a>变量

### <a name="r4.1"></a>规则4.1 需要精确计算时不要使用Float和Double

 说明：浮点数在一个范围很广的值域上提供了很好的近似，但是它不能产生精确的结果。二进制浮点数对于精确计算是非常不适合的，因为它不可能将0.1，或者10的其它任何负次幂表示为一个长度有限的二进制小数。涉及精确的数值计算（货币、金融等），建议使用Int, Long, BigDecimal等。

 不好的例子：

Float值包含多于 6～7 位十进制数，那么会将其舍入

 ```kotlin
 val eFloat = 2.7182818284f // Float，实际值为 2.7182817
 ```

 推荐例子：需要精确计算时

 ```kotlin
val income = BigDecimal("1.03")
val expense = BigDecimal("0.42")
println(income.subtract(expense))
 ```

### <a name="r4.3"></a>规则4.2 浮点型数据判断相等不能直接使用==， **equals或者`flt.compareTo(another) == 0`作相等的比较**

 说明： **compareTo(another)的大小比较可以** 。考虑由`BigDecimal`代替做运算操作。

与0.0f或0.0d作算术比较是允许的。
由于浮点数在计算机表示中存在精度的问题，因此，判断2个浮点数相等不能直接使用等号可以采用如下示例的方式。
其中1e-6f为一个float极小值，实际使用时请根据情况判断精度，并且提取常量。如果是double,请使用1e-6d。

 ```kotlin
val f1 = 1.0f - 0.9f
val f2 = 0.9f - 0.8f
if (f1 == f2) {
    println(" 预期进入此代码快，执行其它业务逻辑")
} else {
    println(" 但事实上 fl == f2 的结果为 false")
}

val flt1 = f1;
val flt2 = f2;
if (flt1.equals(flt2)) {
    println(" 预期进入此代码快，执行其它业务逻辑")
} else {
    println(" 但事实上 equals 的结果为 false")
} 
 ```

推荐例子：

```kotlin
val foo = 1.03f
val bar = 0.42f
if (abs(foo - bar) > 1e-6f) {
    println("OKAY")
} else {
    println("NOK")
}
```



### <a name="r4.3"></a>建议4.1 尽量使用val单次赋值变量

  说明：鼓励只读的、不可变val/immutable，而不是多次可写的var变量，可以增强代码的推理健壮性。但是，累加（accumulator）性的场景，var变量也是允许的。

### <a name="c4.2"></a>类型

### <a name="s4.2"></a>建议4.2 尽量使用契约Contracts，利用smart cast

  说明：Kotlin编译器基于Contracts，可有更好的smart cast，减少样板代码和强制类型转换。

### <a name="s4.3"></a>建议4.3 尽量使用类型别名表示函数类型和集合类型

  说明：类型别名为现有类型提供替代名称。 如果类型名称太长，你可以另外引入较短的名称，并使用新的名称替代原类型名。

它有助于缩短较长的泛型类型。 例如，通常缩减集合类型是很有吸引力的：

```kotlin
typealias NodeSet = Set<Network.Node>

typealias FileTable<K> = MutableMap<K, MutableList<File>>
```

你可以为函数类型提供另外的别名：

```kotlin
typealias MyHandler = (Int, String, Any) -> Unit

typealias Predicate<T> = (T) -> Boolean
```



 # <a name="c5"></a>5 函数

## <a name="c5.1"></a>函数设计

函数设计的精髓：函数是可组合、可重用的代码最小单位，编写高内聚低耦合的整洁函数，同时把代码有效组织起来。代码简单直接、不隐藏设计者的意图、用干净利落的抽象和直截了当的控制语句将函数有机组织起来。用到break,continue,return等中间跳转时，要写上返回结果状态。不应该通过全局/实例变量的副作用来表示状态变化，状态机是例外。 

[Kotlin的设计是支持并鼓励函数式编程的](https://www.slideshare.net/abreslav/whos-more-functional-kotlin-groovy-scala-or-java)。Kotlin也内建支持管道数据流的顶层的标准函数，包括apply，also，run/with，let，在标准库中也广泛使用。Kotlin不允许对函数参数赋值，函数参数可以有默认值，这些都鼓励编写纯的函数（pure functions），即针对有意义的特定输入，避免副作用，总会有对应的输出。

纯函数管道数据流是一种编程范式，每一步都是很简单、可验证、可测试、可替换、可插入、可扩展， 而且容易实现并发处理。数据流中最多只有一个副作用,且只能放在末端。这是一种以数据为中心的风格，数据可以是Kotlin的数据类，包括`Pair<T1,T2>`,`Triple<T1,T2,T3>` 及`data class<T1,T2,...Tn>`，甚至是Maps。

### <a name="r5.1"></a>规则5.1 避免函数过长，不超过30行（非空非注释）

函数应该可以一屏显示完，只做一件事情，而且把它做好。 

过长的函数往往意味着函数功能不单一，过于复杂，或过分呈现细节，未进行进一步抽象。

**例外：** 某些实现算法的函数，由于算法的聚合性与功能的全面性，可能会超过30行。

即使一个长函数现在工作的非常好, 一旦有人对其修改, 有可能出现新的问题, 甚至导致难以发现的bug。  
建议将其拆分为更加简短并易于管理的若干函数，以便于他人阅读和修改代码。

### <a name="r5.2"></a>规则5.2 避免函数的代码块嵌套过深，不要超过4层

函数的代码块嵌套深度指的是函数中的代码控制块（例如：if、for、while、when等）之间互相包含的深度。  
每级嵌套都会增加阅读代码时的脑力消耗，因为需要在脑子里维护一个“栈”（比如，进入条件语句、进入循环等等）。  **例外：** 函数内的lambda表达式、局部类和匿名类嵌套层次以最内层函数来计算，不累积enclosing method的嵌套层次。
应该做进一步的功能分解，从而避免使代码的阅读者一次记住太多的上下文。

### <a name="c5.2"></a>函数参数

### <a name="s5.1"></a>规则5.1 函数的lambda参数放置于最后的位置

 说明：这样利于用大括号表示，更具可读性。

 

 ### <a name="s5.1"></a>建议5.1 函数的参数个数不应超过5个

 说明：如果参数超过5个，则维护的难度很大，建议减少参数个数。

 如果多个参数同时多次出现在多个函数中，说明这些参数紧密相关，可以将它们封装到一个数据类中。

鼓励使用命名和默认参数代替函数重载。

### <a name="s5.2"></a>建议5.2 函数的参数多使用数据类和Maps

 说明：建议**函数尽量设计成参数为hash-map类型的单参数函数**，象`R`语言大多数函数那样，可以设计很多带默认值的命名参数，有很强的可扩展性。另外，`Kotlin`操作hash-map的核心函数很多，操作方便，key to value可读性好。还有`Kotlin`解构方便，在函数体内形式参数使用上与一般多参数函数是一样方便的。

 # <a name="c6"></a>6 类和接口

### <a name="c6.1"></a>6.1 类
### <a name="r6.1"></a>规则6.1 支持属性名称应该与属性的名称完全匹配，前缀为下划线

当需要 支持属性时 ，其名称应该与属性的名称完全匹配，但前缀为下划线。

```kotlin
private var _ table ：Map   ？= null val table ：Map  
 
    get （）{ if （_ table == null ）{ _ table = HashMap （）} return _ table ?: throw AssertionError （）} 
    
```

### <a name="s6.1"></a>建议6.1 类的成员函数要精炼，非紧耦合的设计为扩展函数

 说明：扩展函数是Kotlin的一大亮点，充分利用扩展函数用助于写出纯函数管道数据流的风格。

### <a name="s6.2"></a>建议6.2 充分利用委托减少冗余代码

 说明：类和属性委托是Kotlin语言的内建特性。把多个属性储存在一个映射（map）中，有利于构造纯函数管道数据流。

### <a name="c6.2"></a>6.2 接口

Kotlin的接口interface可以定义属性和函数。在Kotlin和Java中，接口是应用编程接口（API）设计的主要表现手段，并且应该优先于（抽象）类的使用。

Java 9 引入了 JPMS，后者提供了一个模块化平台，使用来自 Java 语言规范的[访问控制](https://docs.oracle.com/javase/specs/jls/se9/html/jls-6.html#jls-6.6)概念来强制实施类型*可访问性*封装。每个模块都定义了哪些包将被导出，从而可供其他模块访问。默认情况下，JMPS 层中的模块都位于同一个类加载器中。

一个包可以包含一个 API。这些 API 包的客户端有两种*角色*：*API 使用者*和 *API 提供者*。API 使用者使用 API 提供者实现的 API。

### <a name="s6.3"></a>建议6.3 API设计，可同时在模块化和非模块化 JVM 环境中使用，应遵循：

1. 包必须是一个有凝聚力、稳定的单元。

2. 最小化包耦合。

3. 避免静态。型不应包含静态成员。静态工厂也应该避免。实例的创建应与 API 分离。

4. 不应通过静态 `getInstance` 方法或静态字段等静态对象来访问单例对象。当需要使用单例对象时，该对象应由 API 定义为单例，并通过依赖注入或对象注册表提供给 API 使用者。

5. 避免类加载器假设。API 设计必须避免在 API 使用者与 API 提供者之间传递类名，而且必须避免与类加载器分层结构和类型可视性/可访问性有关的假设。为了提供可扩展性模型，API 设计应让 API 使用者将类对象，或者最好将实例对象，传递给 API 提供者。

6. 明确规定 API 使用者和 API 提供者的类型角色。例如，在 `javax.servlet` 包中，`ServletContext` 类型由 API 提供者（比如 servlet 容器）实现。向 `ServletContext` 添加新方法需要更新所有 API 提供者来实现新方法，但 API 使用者无需执行更改，除非他们希望调用该新方法。但是，`Servlet` 类型由 API 使用者实现，向 `Servlet` 添加新方法需要修改所有 API 使用者来实现新方法，还需要修改所有 API 提供者来使用该新方法。因此，`ServletContext` 类型有一个 API 提供者角色，`Servlet` 类型有一个 API 使用者角色。

### <a name="s6.4"></a>建议6.4 API设计，应充分表现业务领域的含义

说明：充分表现业务领域的含义，有利于API更加可读可维护，清晰地表达意图。

Kotlin的内联类能够创建新的类型而不增加运行时开销。Kotlin编译器为每个内联类保留一个包装器，内联类的实例可以在运行时表示为包装器或者底层类型。

这儿有个利用内联类表达贴切业务含义的API例子。名字必须有特定的含义，例如只允许英文字符和数字，长度8位。而密码还可以有特殊字符且长度只能是6位。

一般的例子： 调用者存在参数位置放错的可能性

```kotlin
interface Authenticate {
    fun verifyCipher(val name: String, val password: String) {
        println(name.length)
        transformNameStr(name)
        transormPasswordStr(password)
    }
}
fun transformNameStr(val name: String) {
    // ......
}
fun transformPasswordStr(val password: String) {
    // ......
}
```

推荐的例子：如果调用者参数位置放错，编译器不能通过

```kotlin
interface Authenticate {
    fun verifyCipher(val name: Name, val password: Password) {
        println(name.sized)
        name.transform()
        password.transorm()
    }
}

inline class Name(val name: String) {
    val sized: Int
        get() = name.length.coerceAtMost(8) // 名字长度最多8位
 
    fun transform() {
        println("Hello, $name")
    }
} 

inline class Password(val text: String) {
    val sized: Int
        get() = name.length.coerceAtMost(6) // 名字长度最多6位
 
    fun transform() {
        println("Hello, $text")
    }
}
```



# <a name="c7"></a>7 Kotlin-Java互操作

## <a name="c7.1"></a>7.1 Java（用于Kotlin调用）

### <a name="r7.1"></a>规则7.1 不要使用任何Kotlin的[硬关键字](https://kotlinlang.org/docs/reference/keyword-reference.html#hard-keywords) 作为方法或字段的名称,允许使用[软关键字](https://kotlinlang.org/docs/reference/keyword-reference.html#soft-keywords)， [修饰符关键字](https://kotlinlang.org/docs/reference/keyword-reference.html#modifier-keywords)和[特殊标识符](https://kotlinlang.org/docs/reference/keyword-reference.html#special-identifiers)

例如，Mockito的when函数在Kotlin需要反引号：

```kotlin
val callable = Mockito.mock(Callable::class.java)
Mockito.`when`(callable.call()).thenReturn(/* … */)
```

### <a name="r7.2"></a>规则7.2 禁止使用`Any`扩展函数或属性

### <a name="r7.3"></a>规则7.3  公共API中的每个非基本参数，返回值和字段类型都应具有可空性注释

说明：非注释类型被解释为 [“平台”类型](https://kotlinlang.org/docs/reference/java-interop.html#null-safety-and-platform-types)，其具有模糊的可空性。

JSR 305包注释可用于设置合理的默认值，但目前不鼓励使用。它们需要一个选择加载标志才能被编译器尊重并与Java 9的模块系统发生冲突。

### <a name="r7.4"></a>规则7.4  同规则5.1，Java方法的lambda参数放置于最后的位置

说明：符合[SAM转换](https://kotlinlang.org/docs/reference/java-interop.html#sam-conversions)条件的参数类型 应该是最后一个。

例如，RxJava 2’的 Flowable.create()

```java
public static  Flowable create(
    FlowableOnSubscribe source,
    BackpressureStrategy mode) { /* … */ }
```

FlowableOnSubscribe 是符合SAM转换的，在Kotlin中调用的话，大括号就嵌入了：

```kotlin
Flowable.create({ /* … */ }, BackpressureStrategy.LATEST)
```

如果调整参数位置，则可利用尾部lambda语法：

```kotlin
Flowable.create(BackpressureStrategy.LATEST) { /* … */ }
```

### <a name="r7.5"></a>规则7.5 对于要在Kotlin中表示为属性的方法，必须使用严格的“bean”式前缀

说明：访问器方法需要'get'前缀，或者对于boolean -returning方法，可以使用'is'前缀。关联的mutator方法需要'set'前缀。

## <a name="c7.2"></a>7.2 Kotlin（用于Java调用）

### <a name="r7.5"></a>规则7.6  当文件包含顶层函数或属性时，使用`@file:JvmName("Foo")`来标注它以提供一个好的名称

说明：默认情况下，文件MyClass.kt中的顶层成员将最终出现在一个`MyClassKt`的类中 。可添加`@file:JvmMultifileClass` 以将多个文件中的顶层成员组合到一个类中。

### <a name="s7.1"></a>建议7.1  要从Java中使用的[函数类型](https://kotlinlang.org/docs/reference/lambdas.html#function-types)应该避免返回类型 `Unit`

### <a name="s7.2"></a>建议7.2  避免使用`Nothing`泛型

说明：泛型参数的`Nothing`类型作为Java的原始类型公开。原始类型很少在Java中使用，应该避免使用。

### <a name="s7.3"></a>建议7.3  抛出已检查异常的函数应该在KDoc记录 `@Throws`

### <a name="s7.4"></a>建议7.4  从公共API返回共享或非只读集合时，将它们包装在不可修改的容器中或执行防御性拷贝

### <a name="r7.6"></a>规则7.7  伴随对象中的公共函数必须注释 `@JvmStatic`为作为静态方法公开

说明：如果没有注释，这些函数仅可用作静态`Companion`字段上的实例方法。

*不正确：没有注释*

```kotlin
class KotlinClass { 
    companion object { 
        fun doWork （）{ / * ... * / } 
    }
}  
               
public final class JavaClass { 
    public static void main （String ... args ）{ 
        KotlinClass.companion.doWork （）;
    }
}      
```

*正确：* `@JvmStatic` *注释*

```kotlin
class KotlinClass {
    companion object {
        @JvmStatic fun doWork （）{ / * ... * / }
    }
}       
         
        
public final class JavaClass { 
    public static void main （String ... args ）{ 
        KotlinClass.doWork （）; 
    }
}     
```

### <a name="r7.7"></a>规则7.8  `const`作为有效常量的公共非属性 `companion object`必须注释`@JvmField`为暴露为静态字段

说明：如果没有注释，这些属性仅可用作静态`Companion`字段上奇怪命名的实例“getters” 。使用`@JvmStatic`而不是`@JvmField`将奇怪命名的“getters”移动到类上的静态方法，这仍然是不正确的。

*不正确：没有注解*

```kotlin
class KotlinClass {
    companion object {
        const val INTEGER _ ONE = 1
        val BIG _ INTEGER _ ONE = BigInteger.ONE
    }
}     
      
    
public final class JavaClass {
    public static void main （String ... args ）{
        System.out.println （KotlinClass.INTEGER_ONE ）;
        System.out.println （KotlinClass.companion.getBIG_INTEGER_ONE （））;
    }
}        
```

*不正确：没有* `@JvmStatic` *注解*

```kotlin
class KotlinClass {
    companion object {
        const val INTEGER _ ONE = 1
        @JvmStatic val BIG _ INTEGER _ ONE = BigInteger.ONE
    }
}    
  
public final class JavaClass {
    public static void main （String... args ）{
        System.out.println （KotlinClass.INTEGER_ONE ）;
        System.out.println （KotlinClass.getBIG_INTEGER_ONE （））;
    }
}             
    
```

*正确：* `@JvmField` *注解*

```kotlin
class KotlinClass {
    companion object {
        const val INTEGER _ ONE = 1 
        @JvmField val BIG _ INTEGER _ ONE = BigInteger.ONE
    }
}  
        
    
public final class JavaClass {
    public static void main （String ... args ）{
        System.out.println （KotlinClass.INTEGER_ONE ）;
        System.out.println （KotlinClass.BIG_INTEGER_ONE ）;
    }
}     
        
        
```

### <a name="s7.3"></a>建议7.3  习惯命名上，Kotlin与Java有所不同，应当使用@JvmName

```kotlin
sealed class Optional
data class Some(val value: T): Optional()
object None : Optional()

@JvmName("ofNullable")
fun  T?.asOptional() = if (this == null) None else Some(this)

// FROM KOTLIN:
fun main(vararg args: String) {
    val nullableString: String? = "foo"
    val optionalString = nullableString.asOptional()
}

// FROM JAVA:
public static void main(String... args) {
    String nullableString = "Foo";
    Optional optionalString =
          Optionals.ofNullable(nullableString);
}
```

### <a name="r7.8"></a>规则7.9  带默认值的函数重载必须使用@JvmOverloads

说明：@JvmOverloads用后会生成方法，确保他们都合理，如果仍不满足，可以做如下重构：

1. 调整参数的顺序，优先把有默认值的放在结尾
2. 把带默认值的移到手写的函数重载中

*不正确: 没有* `@JvmOverloads`

```kotlin
class Greeting {
    fun sayHello(prefix: String = "Mr.", name: String) {
        println("Hello, $prefix $name")
    }
}
public class JavaClass {
    public static void main(String... args) {
        Greeting greeting = new Greeting();
        greeting.sayHello("Mr.", "Bob");
    }
}
```

*正确:* `@JvmOverloads` 

```kotlin
class Greeting {
    @JvmOverloads
    fun sayHello(prefix: String = "Mr.", name: String) {
        println("Hello, $prefix $name")
    }
}
public class JavaClass {
    public static void main(String... args) {
        Greeting greeting = new Greeting();
        greeting.sayHello("Bob");
    }
}
```

### <a name="s7.4"></a>建议7.4  启用Android Studio 3.2+的Lint检查

说明：在Android Studio中，**File > Preferences > Editor > Inspections** Kotlin Interoperability下，打开所有的互操作性检查。如果是Command-line builds，增加下面的行在build.gradle中

```groovy
android {
    // ...
    lintOptions {
        check 'Interoperability'
    }
}
```



#<a name="appendix"></a>9 附录

## <a name="reference"></a>9.1	参考
 ###9.1.1    Google Kotlin Style Guides: <https://developer.android.com/kotlin/style-guide>
 ###9.1.2	JetBrains Kotlin语言编程规范：<https://kotlinlang.org/docs/reference/coding-conventions.html>

 ####9.1.3	Ktlin: <https://github.com/shyiko/ktlint>

## <a name="contributor"></a>9.2	贡献者

感谢所有参与规则制订、检视、评审的专家、同事！
感谢所有提 issue / MR 参与贡献的同事！

| 版本             | 起草                                                         | 评审                                                         | 批准人         | 修订情况                                                 |
| ---------------- | ------------------------------------------------------------ | ------------------------------------------------------------ | -------------- | -------------------------------------------------------- |
| DKBA1040-2019.03 | **CBG软件部**: 孙奇辉00440503 <br>**CBG软件部**: 薛竹飙00377846 任占民00389500<br> **公共开发部**: 焦石00343677 <br>**北研质量部**: 姜皓00267218 <br> | **CBG**: 王燕东00275844, 何良春00474316, 郑成亮00336987, 伊伟00212138, 王娅琦00464446, 张学煜 84105082,  赵俊民00387162，孙渊磊0217938, 冯文瀚00379734, 张志军00254575, 汪新建00210918<br>**网络**: 刘金亮00314904 <br>**研发能力中心**: 周代兵00340713, 李科00316555,陈旭00218141<br> | 董庆阳00372143 | V1.0 <br>本次优化，重点参考了Google、JetBrains等编程规范 |

