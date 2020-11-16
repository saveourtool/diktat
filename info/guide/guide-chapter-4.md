# <a name="c4"></a> 4. Variables and types
<!-- =============================================================================== -->
### <a name="c4.1"></a> 4.1 Variables
### <a name="r4.1.1"></a> Rule 4.1.1: Do not use Float and Double types when accurate calculations are needed.
Floating-point numbers provide a good approximation over a wide range of values, but they cannot produce accurate results in some cases.
Binary floating-point numbers are unsuitable for precise calculations, because it is impossible to represent 0.1 or any other negative power of 10 in a `binary representation` with a finite length.

The following example seems to be simple code that is obvious: 
```kotlin
    val myValue = 2.0 - 1.1
    println(myValue)
``` 

However, it will print a value such as: `0.8999999999999999`

As such, if you need to make precise calculations (for example, when dealing with currency, finance, or an exact science), `Int`, `Long`, `BigDecimal`, etc. are recommended.
Among them, `BigDecimal` should serve as a good choice.

**Invalid example:** \
If a float value contains more than six to seven decimal numbers, it will be rounded off.
 ```kotlin
 val eFloat = 2.7182818284f // Float, will be rounded to 2.7182817
 ```

**Valid example** (when accurate calculations are needed): 
 ```kotlin
    val income = BigDecimal("2.0")
    val expense = BigDecimal("1.1")
    println(income.subtract(expense)) // you will obtain 0.9 here
 ```

### <a name="r4.1.2"></a> Rule 4.1.2: The numbers of a float type should not be directly compared with the equality operator (==) or other methods like compareTo and equals.

Since floating-point numbers involve precision problems in computer representation, it is better to use `BigDecimal` as recommended in [Rule 4.1.1](#r4.1.1) to make accurate computations and comparisons. The following code describes these problems.

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

### <a name="r4.1.3"></a> Rule 4.1.3 Try to use 'val' instead of 'var' for variable declaration [SAY_NO_TO_VAR].

Variables with the `val` modifier are immutable (read-only).
Code robustness and readability increase through the use of such variables, as opposed to `var` variables.
This is because var variables can be reassigned several times in the business logic.
Of course, in some scenarios with loops or accumulators, only `var`s are permitted.

<!-- =============================================================================== -->
### <a name="c4.2"></a> 4.2 Types
### <a name="s4.2.1"></a> Recommendation 4.2.1: Use Contracts and smart cast as much as possible.

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

Also Kotlin 1.3 introduced [Contracts](https://kotlinlang.org/docs/reference/whatsnew13.html#contracts) that provide enhanced logic for smart-cast.
Contracts are used and are very stable in `stdlib`.
 
For example:

```kotlin
fun bar(x: String?) {
    if (!x.isNullOrEmpty()) {
        println("length of '$x' is ${x.length}") // smartcasted to not-null
    }
} 
```

Smart cast and contracts are better because they reduce boilerplate code and forced type conversion.

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

### <a name="s4.2.2"></a>Recommendation 4.2.2: Try to use type alias to represent types and make code more readable.

Type aliases provide alternative names for existing types.
If the type name is too long, you can replace it with a shorter name. It helps to shorten long generic types.
For example, code looks much more readable if you introduce a `typealias` instead of a long chain of nested generic types.
We recommend the use of a `typealias` if the type contains **more than two** nested generic types and is longer than **25 chars**.

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

### <a name="s4.3.1"></a> Recommendation 4.3.1: Avoid declaring variables with nullable types, especially from Kotlin stdlib.
To avoid `NullPointerException` and help compiler checks prevent NPE, try to avoid using nullable types (with `?` symbol).

**Invalid example**:
```kotlin 
val a: Int? = 0
```

**Valid example**:
```kotlin 
val a: Int = 0
```

Nevertheless, if you use Java libraries extensively, you will have to use nullable types and enrich your code with `!!` and `?` symbols.
Avoid using nullable types for Kotlin stdlib (declared in [official documentation](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/)) avoid using nullable types. 
Try to use initializers for empty collections. ), and try using initializers for empty collections. For example: If you want to initialize a list instead of using `null` use `emptyList()`.

**Invalid example**:
```kotlin 
val a: List<Int>? = null 
```

**Valid example**:
```kotlin 
val a: List<Int> = emptyList()
```

### <a name="s4.3.2"></a> Recommendation 4.3.2: Variables of generic types should have an explicit type declaration.
As in Java, classes in Kotlin may have type parameters. To create an instance of such a class, we typically need to provide type arguments:

```kotlin
val myVariable: Map<Int, String> = emptyMap<Int, String>() 
```

However, the compiler can inherit type parameters from the right value, and as such, will not force users to explicitly declare the type.
These declarations are not recommended because programmers would need to find its return value and understand the variable type by looking at the method.

**Invalid example**:
```kotlin
val myVariable = emptyMap<Int, String>() 
```

**Valid example**:
```kotlin
val myVariable: Map<Int, String> = emptyMap() 
```
