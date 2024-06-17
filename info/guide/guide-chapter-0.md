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

1. Writing a clean and simple Kotlin code

    Kotlin combines two of the main programming paradigms: functional and object-oriented.
    Both of these paradigms are trusted and well-known software engineering practices.
    As a young programming language, Kotlin is built on top of well-established languages such as Java, C++, C#, and Scala.
    This enables Kotlin to introduce many features that help a developer write cleaner, more readable code while also reducing the number of complex code structures. For example, type and null safety, extension functions, infix syntax, immutability, val/var differentiation, expression-oriented features, "when" statements, much easier work with collections, type auto conversion, and other syntactic sugar.

2. Following Kotlin idioms

    The author of Kotlin, Andrey Breslav, mentioned that Kotlin is both pragmatic and practical, but not academic.
    Its pragmatic features enable ideas to be transformed into real working software easily. Kotlin is closer to natural languages than its predecessors, and it implements the following design principles: readability, reusability, interoperability, security, and tool-friendliness (https://blog.jetbrains.com/kotlin/2018/10/kotlinconf-2018-announcements/).

3. Using Kotlin efficiently

    Some Kotlin features can help you write high-performance code. Such features include: rich coroutine library, sequences, inline functions/classes, arrays of basic types, tailRec, and CallsInPlace of contract.

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
The software that is directly based on the Android native operating system interface, such as the Android Framework, remains consistent with the Android style.
