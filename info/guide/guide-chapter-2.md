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
- 
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
