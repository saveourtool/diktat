## <a name="c0"></a> 0. Preface
 <!-- =============================================================================== -->
### <a name="c0.1"></a> 0.1 Purpose of this document   

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

<!-- =============================================================================== -->
### <a name="c0.2"></a> 0.2 General principles

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

3. Use Kotlin efficiently

    Some Kotlin features help you write higher-performance code: including rich coroutine library, sequences, inline functions/classes, arrays of basic types, tailRec, and CallsInPlace of contract.

<!-- =============================================================================== -->
### <a name="c0.3"></a> 0.3 Terminology   

**Rules**: conventions that should be followed when programming.

**Recommendations**: conventions that should be considered when programming.

**Explanation**: necessary explanations of rules and recommendations.

**Valid Example**: examples (recommended) of rules and recommendations.

**Invalid Example**: examples (not recommended) of rules and recommendations.

Unless otherwise stated, this specification applies to versions 1.3 and later of Kotlin.

<!-- =============================================================================== -->
### <a name="c0.4"></a> 0.4 Exceptions

Even though exceptions may exist, it is important to understand why rules and recommendations are needed.
Depending on your project situation or personal habits, you can break some of the rules. However, remember that one exception leads to many and can completely destroy the consistency of code. As such, there should be very few exceptions.
When modifying open-source code or third-party code, you can choose to implement the style used by the code (as opposed to using the existing specifications) to maintain consistency.
Software that is directly based on the interface of the Android native operating system, such as the Android Framework, remains consistent with the Android style.
