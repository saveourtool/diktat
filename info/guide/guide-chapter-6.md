# <a name="c6"></a> 6. Classes, interfaces and functions
<!-- =============================================================================== -->
### <a name="c6.1"></a> 6.1 Classes
### <a name="r6.1.1"></a> Rule 6.1.1:  When a class has a single constructor, it should be defined as a primary constructor in the declaration of the class.
In case class contains only one explicit constructor - it should be converted to a primary constructor.

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

// in case of any annotations or modifiers used on constructor:
class Test private constructor(var a: Int) { 
    // ...
}
```

### <a name="r6.1.2"></a> Rule 6.1.2: Prefer data classes instead of classes without any functional logic.
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

**Prefer:**
```kotlin
data class Test1(var a: Int = 0, var b: Int = 0)
```

**Exception #1**: Note, that data classes cannot be abstract, open, sealed or inner, that's why these types of classes cannot be changed to data class.
**Exception #2**: No need to convert a class to data class in case this class extends some other class or implements an interface.

### <a name="r6.1.3"></a> Rule 6.1.3: Do not use the primary constructor if it is empty and has no sense.
The primary constructor is part of the class header: it goes after the class name (and optional type parameters).
But in is useless - it can be omitted. 

**Invalid example**:
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

**Valid example**:
```kotlin
// the good example here is a data class, but this example shows that you should get rid of braces for primary constructor
class Test {
    var a: Int = 0
    var b: Int = 0
}
```

### <a name="r6.1.4"></a> Rule 6.1.4: several init blocks are redundant and generally should not be used in your class.
The primary constructor cannot contain any code. That's why Kotlin has introduced `init` blocks.
These blocks are used to store the code that should be run during the initialization of the class.
Kotlin allows to write multiple initialization blocks that are executed in the same order as they appear in the class body.
Even when you have the (rule 3.2)[#s3.2] this makes code less readable as the programmer needs to keep in mind all init blocks and trace the execution of the code.
So in your code you should try to use single `init` block to reduce the complexity. In case you need to do some logging or make some calculations before the assignment 
of some class property - you can use powerful functional programming. This will reduce the possibility of the error, when occasionally someone will change the order of your `init` blocks. 
And it will make the logic of the code more coupled. It is always enough to use one `init` block to implement your idea in Kotlin.

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

Also - init block was not added to Kotlin to help you simply initialize your properties it is needed for more complex tasks. 
So if `init` block contains only assignments of variables - move it directly to properties, so they will be correctly initialized near the declaration.
In some case this rule can be in clash with [6.1.1](#r6.1.1), but that should not stop you.

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

### <a name="r6.1.5"></a> Rule 6.1.5: Explicit supertype qualification should not be used if there is not clash between called methods.
This rule is applicable for both interfaces and classes.

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

### <a name="r6.1.6"></a> Rule 6.1.6: Abstract class should have at least one abstract method.
Abstract classes are used to force a developer to implement some of its parts in its inheritors.
In case when abstract class has no abstract methods - then it was set `abstract` incorrectly and can be converted to a normal class.

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
class NotAbstract {
    fun foo() {}
    
    fun test() {}
}
```


### <a name="r6.1.7"></a> Rule 6.1.7: in case of using "implicit backing property" scheme, the name of real and back property should be the same.
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

### <a name="r6.1.8"></a> Recommendation 6.1.8: avoid using custom getters and setters.
Kotlin has a perfect mechanism of [properties](https://kotlinlang.org/docs/reference/properties.html#properties-and-fields).
Kotlin compiler automatically generates `get` and `set` methods for properties and also lets the possibility to override it:
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

From the callee code these methods look like an access to this property: `A().isEmpty = true` for setter and `A().isEmpty` for getter.
But in all cases it is very confusing when `get` and `set` are overridden for a developer who uses this particular class. 
Developer expects to get the value of the property, but receives some unknown value and some extra side effect hidden by the custom getter/setter. 
Use extra functions for it instead.

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

### <a name="r6.1.9"></a> Rule 6.1.9: never use the name of a variable in the custom getter or setter (possible_bug).
Even if you have ignored [recommendation 6.1.8](#r6.1.8) you should be careful with using the name of the property in your custom getter/setter
as it can accidentally cause a recursive call and a `StackOverflow Error`. Use `field` keyword instead.

**Invalid example (very bad)**:
```kotlin
var isEmpty: Boolean
    set(value) {
        println("Side effect")
        isEmpty = value
    }
    get() = isEmpty
```

### <a name="s6.1.10"></a> Recommendation 6.1.10 no trivial getters and setters are allowed in the code.
In Java - trivial getters - are getters that are simply returning the value of a field.
Trivial setters - are simply setting the field with a value without any transformation.
But in Kotlin trivial getters/setters are generated by the default. There is no need to use it explicitly for all types of data-structures in Kotlin.

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

### <a name="s6.1.11"></a> Rule 6.1.11: use apply for grouping object initialization.
In the good old Java before functional programming became popular - lot of classes from commonly used libraries used configuration paradigm.
To use these classes you had to create an object with the constructor that had 0-2 arguments and set the fields that were needed to run an object.
In Kotlin to reduce the number of dummy code lines and to group objects [`apply` extension](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/apply.html) was added:
 
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

<!-- =============================================================================== -->
### <a name="c6.2"></a>6.2 Extension functions
[Extension functions](https://kotlinlang.org/docs/reference/extensions.html) - is a killer-feature in Kotlin. 
It gives you a chance to extend classes that were already implemented in external libraries and help you to make classes less heavy.
Extension functions are resolved statically.

### <a name="s6.2.1"></a> Recommendation 6.2.1: use extension functions for making logic of classes less coupled.
It is recommended that for classes non-tightly coupled functions with rare usages in the class should be implemented as extension functions where possible.
They should be implemented in the same class/file where they are used. This is non-deterministic rule, so it cannot be checked or fixed automatically by static analyzer.

### <a name="s6.2.2"></a> Rule 6.2.2: there should be no extension functions with the same name and signature if they extend base and inheritor classes (possible_bug).
As extension functions are resolved statically. In this case there can be a situation when a developer implements two extension functions - one is for the base class and another one for the inheritor.
And that can lead to an issue when incorrect method is used. 

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

<!-- =============================================================================== -->
### <a name="c6.2"></a> 6.3 Interfaces
`Interface`s in Kotlin can contain declarations of abstract methods, as well as method implementations. What makes them different from abstract classes is that interfaces cannot store state.
They can have properties, but these need to be abstract or to provide accessor implementations.

Kotlin's interfaces can define attributes and functions.
In Kotlin and Java, the interface is the main presentation means of application programming interface (API) design, and should take precedence over the use of (abstract) classes.

<!-- =============================================================================== -->
### <a name="c6.4"></a> 6.4 Objects
### <a name="s6.4.1"></a> Rule 6.4.1: Avoid using utility classes/objects, use extensions instead.
As described in [6.2 Extension functions](#c6.2) - extension functions - is a very powerful mechanism.
So instead of using utility classes/objects, use it instead.
This allows you to remove the unnecessary complexity and wrapping class/object and to use top-level functions instead.

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

### <a name="s6.4.2"></a> Recommendation 6.4.2: Objects should be used for Stateless Interfaces.
Kotlin’s object are extremely useful when we need to implement a some interface from an external library that doesn’t have any state.
No need to use class for such structures.

**Valid example**:
```
interface I {
    fun foo()
}

object O: I {
    override fun foo() {}
}
```
