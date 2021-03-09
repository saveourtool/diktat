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

