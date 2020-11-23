# <a name="c8"></a> 8. Things that will be moved to a main guide later
<!-- =============================================================================== -->
### <a name="c8.1"></a> Null-safety
### <a name="r8.1.1"></a> 4.3.3 Explicit null checks

Try to avoid explicit null checks (explicit comparison with `null`) 
Kotlin is declared as [Null-safe](https://kotlinlang.org/docs/reference/null-safety.html) language.
But Kotlin architects wanted Kotlin to be fully compatible with Java, that's why `null` keyword was also introduced in Kotlin. 

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
    println("null")
} ?: run { println("not null") }
```

**Exceptions:** in case of complex expressions like multiple `else-if` structures, or a long conditional statement there is a common sense to use explicit comparison with `null`.

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
