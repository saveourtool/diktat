package test.paragraph3.trailing_comma

class Customer(
    val name: String,
    lastName: String
)

fun shift(x: Int,
          y: Int
) {
    shift(
        25,
        20
    )

    val colors = listOf(
        "red",
        "green",
        "blue"
    )
}

fun getZValue(mySurface: Surface,
              xValue: Int,
              yValue: Int
) =
    mySurface[
            xValue,
            yValue
    ]

fun isReferenceApplicable(myReference: KClass<*>
) = when (myReference) {
    Comparable::class,
    Iterable::class,
    String::class
    -> true
    else -> false
}

fun isReferenceApplicable(myReference: KClass<*> // comment
) = when (myReference) {
    Comparable::class,
    Iterable::class,
    String::class
    -> true
    else -> false
}

@ApplicableFor([
    "serializer",
    "balancer",
    "database",
    "inMemoryCache"
]
)
fun foo() {}

fun <T1,
        T21
        > foo() {}

fun mains() {
    foo<
            Comparable<Number
                    >,
            Iterable<Number
                    >
            >()
}

fun printMeanValue() {
    var meanValue: Int = 0
    for ((
        _,
        _,
        year
    ) in cars) {
        meanValue += year
    }
    println(meanValue/cars.size)
}

enum class SomeEnum(
    val a: Int, val b: Int  // comment
)

enum class SomeEnum(
    val a: Int, val b: Int// comment
)

enum class SomeEnum(
    val a: Int, val b: Int  /* comment */
)

enum class SomeEnum(
    val a: Int, val b: Int  /**
    some comment */
)

fun foo() {
    val sum: (Int, Int, Int,) -> Int = fun(
        x,
        y,
        z // trailing comma
    ): Int {
        return x + y + x
    }
    println(sum(8, 8, 8))
}

fun shift(x: Int, y: Int) {
    shift(
        25,
        20 // trailing comma
    )

    val colors = listOf(
        "red",
        "green",
        "blue" // trailing comma
    )
}