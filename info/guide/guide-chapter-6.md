# <a name="c6"></a> 6. Classes, interfaces, and extension functions
<!-- =============================================================================== -->
### <a name="c6.1"></a> 6.1 Classes
This section describes the rules of denoting classes in your code.
#### <a name="r6.1.1"></a> 6.1.1  Denoting a class with a single constructor
When a class has a single constructor, it should be defined as a primary constructor in the declaration of the class. If the class contains only one explicit constructor, it should be converted to a primary constructor.

**Invalid example**:
```kotlin
class Test {
    var a: Int
    constructor(a: Int) {
        this.a = a
    }
}
```

**Valid example**:
```kotlin
class Test(var a: Int) { 
    // ...
}

// in case of any annotations or modifiers used on a constructor:
class Test private constructor(var a: Int) { 
    // ...
}
```

#### <a name="r6.1.2"></a> 6.1.2 Prefer data classes instead of classes without any functional logic
Some people say that the data class is a code smell. However, if you need to use it (which makes your code more simple), you can utilize the Kotlin `data class`. The main purpose of this class is to hold data,
but also `data class` will automatically generate several useful methods:
- equals()/hashCode() pair;
- toString()
- componentN() functions corresponding to the properties in their order of declaration;
- copy() function

Therefore, instead of using `normal` classes:

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

**prefer data classes:**
```kotlin
data class Test1(var a: Int = 0, var b: Int = 0)
```

**Exception 1**: Note that data classes cannot be abstract, open, sealed, or inner; that is why these types of classes cannot be changed to a data class.

**Exception 2**: No need to convert a class to a data class if this class extends some other class or implements an interface.

#### <a name="r6.1.3"></a> 6.1.3 Do not use the primary constructor if it is empty or useless
The primary constructor is a part of the class header; it is placed after the class name and type parameters (optional) but can be omitted if it is not used.

**Invalid example**:
```kotlin
// simple case that does not need a primary constructor
class Test() {
    var a: Int = 0
    var b: Int = 0
}

// empty primary constructor is not needed here
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

**Valid example**:
```kotlin
// the good example here is a data class; this example also shows that you should get rid of braces for the primary constructor
class Test {
    var a: Int = 0
    var b: Int = 0
}
```

#### <a name="r6.1.4"></a> 6.1.4 Do not use redundant init blocks in your class
Several init blocks are redundant and generally should not be used in your class. The primary constructor cannot contain any code. That is why Kotlin has introduced `init` blocks.
These blocks store the code to be run during the class initialization.
Kotlin allows writing multiple initialization blocks executed in the same order as they appear in the class body.
Even when you follow (rule 3.2)[#r3.2], this makes your code less readable as the programmer needs to keep in mind all init blocks and trace the execution of the code.
Therefore, you should try to use a single `init` block to reduce the code's complexity. If you need to do some logging or make some calculations before the class property assignment, you can use powerful functional programming. This will reduce the possibility of the error if your `init` blocks' order is accidentally changed and
make the code logic more coupled. It is always enough to use one `init` block to implement your idea in Kotlin.

**Invalid example**:
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

**Valid example**:
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

The `init` block was not added to Kotlin to help you initialize your properties; it is needed for more complex tasks. 
Therefore if the `init` block contains only assignments of variables - move it directly to properties to be correctly initialized near the declaration.
In some cases, this rule can be in clash with [6.1.1](#r6.1.1), but that should not stop you.

**Invalid example**:
```kotlin
class A(baseUrl: String) {
    private val customUrl: String
    init {
        customUrl = "$baseUrl/myUrl"
    }
}
```

**Valid example**:
```kotlin
class A(baseUrl: String) {
    private val customUrl = "$baseUrl/myUrl"
}
```

#### <a name="r6.1.5"></a> 6.1.5 Explicit supertype qualification
The explicit supertype qualification should not be used if there is no clash between called methods. This rule is applicable to both interfaces and classes.

**Invalid example**:
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

#### <a name="r6.1.6"></a> 6.1.6 Abstract class should have at least one abstract method
Abstract classes are used to force a developer to implement some of its parts in their inheritors.
When the abstract class has no abstract methods, it was set `abstract` incorrectly and can be converted to open class.

**Invalid example**:
```kotlin
abstract class NotAbstract {
    fun foo() {}
    
    fun test() {}
}
```

**Valid example**:
```kotlin
abstract class NotAbstract {
    abstract fun foo()
    
    fun test() {}
}

// OR
open class NotAbstract {
    fun foo() {}
    
    fun test() {}
}

// OR
class NotAbstract {
    fun foo() {}

    fun test() {}
}
```


#### <a name="r6.1.7"></a> 6.1.7 When using the "implicit backing property" scheme, the name of real and back property should be the same
Kotlin has a mechanism of [backing properties](https://kotlinlang.org/docs/reference/properties.html#backing-properties).
In some cases, implicit backing is not enough and it should be done explicitly:
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

In this case, the name of the backing property (`_table`) should be the same as the name of the real property (`table`) but should have an underscore (`_`) prefix.
It is one of the exceptions from the [identifier names rule](#r1.2)

#### <a name="r6.1.8"></a> 6.1.8 Avoid using custom getters and setters
Kotlin has a perfect mechanism of [properties](https://kotlinlang.org/docs/reference/properties.html#properties-and-fields).
Kotlin compiler automatically generates `get` and `set` methods for properties and can override them.

**Invalid example:**
```kotlin 
class A {
    var size: Int = 0
        set(value) {
            println("Side effect")
            field = value
        }
        // user of this class does not expect calling A.size receive size * 2 
        get() = field * 2
}
```

From the callee code, these methods look like access to this property: `A().isEmpty = true` for setter and `A().isEmpty` for getter.

However, when `get` and `set` are overridden, it  isn't very clear for a developer who uses this particular class. 
The developer expects to get the property value but receives some unknown value and some extra side-effect hidden by the custom getter/setter. 
Use extra functions instead to avoid confusion.



**Valid example**:
```kotlin 
class A {
    var size: Int = 0
    fun initSize(value: Int) {
        // some custom logic
    }
    
    // this will not confuse developer and he will get exactly what he expects    
    fun goodNameThatDescribesThisGetter() = this.size * 2
}
```

**Exception:** `Private setters` are only exceptions that are not prohibited by this rule.

#### <a name="r6.1.9"></a> 6.1.9 Never use the name of a variable in the custom getter or setter (possible_bug)
If you ignored [recommendation 6.1.8](#r6.1.8), be careful with using the name of the property in your custom getter/setter
as it can accidentally cause a recursive call and a `StackOverflow Error`. Use the `field` keyword instead.

**Invalid example (very bad)**:
```kotlin
var isEmpty: Boolean
    set(value) {
        println("Side effect")
        isEmpty = value
    }
    get() = isEmpty
```

#### <a name="r6.1.10"></a> 6.1.10 No trivial getters and setters are allowed in the code
In Java, trivial getters - are the getters that are just returning the field value.
Trivial setters - are merely setting the field with a value without any transformation.
However, in Kotlin, trivial getters/setters are generated by default. There is no need to use it explicitly for all types of data structures in Kotlin.

**Invalid example**:
```kotlin
class A {
    var a: Int = 0 
    get() = field
    set(value: Int) { field = value }

    //
}
```

**Valid example**:
```kotlin
class A {
    var a: Int = 0 
    get() = field
    set(value: Int) { field = value }

    //
}
```

#### <a name="r6.1.11"></a> 6.1.11 Use 'apply' for grouping object initialization
In Java, before functional programming became popular, many classes from common libraries used the configuration paradigm.
To use these classes, you had to create an object with the constructor with 0-2 arguments and set the fields needed to run the object.
In Kotlin, to reduce the number of dummy code line and to group objects [`apply` extension](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/apply.html) was added:  
 
**Invalid example**:
```kotlin
class HttpClient(var name: String) {
    var url: String = ""
    var port: String = ""
    var timeout = 0
    
    fun doRequest() {}
}

fun main() {
    val httpClient = HttpClient("myConnection")
    httpClient.url = "http://example.com"
    httpClient.port = "8080"
    httpClient.timeout = 100
    
    httpCLient.doRequest()
}   

```

**Valid example**:
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
                url = "http://example.com"
                port = "8080"
                timeout = 100
            }
    httpClient.doRequest()
}
```

### <a name="r6.1.12"></a> 6.1.12 Prefer Inline classes when a class has a single property
If a class has only one immutable property, then it can be converted to the inline class.

Sometimes it is necessary for business logic to create a wrapper around some type. However, it introduces runtime overhead due to additional heap allocations. Moreover, if the wrapped type is primitive, the performance hit is terrible, because primitive types are usually heavily optimized by the runtime, while their wrappers don't get any special treatment.

**Invalid example**:
```kotlin
class Password {
    val value: String
}
```

**Valid example**:
```kotlin
inline class Password(val value: String)
```

<!-- =============================================================================== -->
### <a name="c6.2"></a>6.2 Extension functions
This section describes the rules of using extension functions in your code.

[Extension functions](https://kotlinlang.org/docs/reference/extensions.html) is a killer-feature in Kotlin. 
It gives you a chance to extend classes that were already implemented in external libraries and helps you to make classes less heavy.
Extension functions are resolved statically.

#### <a name="r6.2.1"></a> 6.2.1 Use extension functions for making logic of classes less coupled
It is recommended that for classes, the non-tightly coupled functions, which are rarely used in the class, should be implemented as extension functions where possible.
They should be implemented in the same class/file where they are used. This is a non-deterministic rule, so the code cannot be checked or fixed automatically by a static analyzer.

#### <a name="r6.2.2"></a> 6.2.2 No extension functions with the same name and signature if they extend base and inheritor classes (possible_bug)
You should avoid declaring extension functions with the same name and signature if their receivers are base and inheritor classes (possible_bug),
as extension functions are resolved statically. There could be a situation when a developer implements two extension functions: one is for the base class and
another for the inheritor. This can lead to an issue when an incorrect method is used.

**Invalid example**:
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

#### <a name="r6.2.3"></a> 6.2.3 Don't use extension functions for the class in the same file
You should not use extension functions for the class in the same file, where it is defined.

**Invalid example**:
```kotlin
class SomeClass {
    
}

fun SomeClass.deleteAllSpaces() {
    
}
```

#### <a name="r6.2.4"></a> 6.2.4 You should not use property length with operation - 1, you can change this to lastIndex
You should not use property length with operation - 1, you can change this to lastIndex

**Invalid example**:
```kotlin
val A = "name"
val B = A.length - 1
val C = A[A.length - 1]
```

**Valid example**:
```kotlin
val A = "name"
val B = A.lastIndex
val C = A[A.lastIndex]
```


<!-- =============================================================================== -->
### <a name="c6.3"></a> 6.3 Interfaces
An `Interface` in Kotlin can contain declarations of abstract methods, as well as method implementations. What makes them different from abstract classes is that interfaces cannot store state.
They can have properties, but these need to be abstract or to provide accessor implementations.

Kotlin's interfaces can define attributes and functions.
In Kotlin and Java, the interface is the main presentation means of application programming interface (API) design and should take precedence over the use of (abstract) classes.

<!-- =============================================================================== -->
### <a name="c6.4"></a> 6.4 Objects
This section describes the rules of using objects in code.
#### <a name="r6.4.1"></a> 6.4.1 Instead of using utility classes/objects, use extensions
Avoid using utility classes/objects; use extensions instead. As described in [6.2 Extension functions](#c6.2), using extension functions is a powerful method.
This enables you to avoid unnecessary complexity and class/object wrapping and use top-level functions instead.

**Invalid example**:
```kotlin 
object StringUtil {
    fun stringInfo(myString: String): Int {
        return myString.count{ "something".contains(it) }
    }
}
StringUtil.stringInfo("myStr")
```

**Valid example**:
```kotlin
fun String.stringInfo(): Int {
    return this.count{ "something".contains(it) }
}

"myStr".stringInfo()
```

#### <a name="r6.4.2"></a> 6.4.2 Objects should be used for Stateless Interfaces
Kotlin’s objects are extremely useful when you need to implement some interface from an external library that does not have any state.
There is no need to use classes for such structures.

**Valid example**:
```
interface I {
    fun foo()
}

object O: I {
    override fun foo() {}
}
```
### <a name="c6.5"></a> 6.5 Kts Files
This section describes general rules for `.kts` files
#### <a name="r6.5.1"></a> 6.5.1 kts files should wrap logic into top-level scope
It is still recommended wrapping logic inside functions and avoid using top-level statements for function calls or wrapping blocks of code
in top-level scope functions like `run`.

**Valid example**:
```
run {
    // some code
}

fun foo() {

}
```
