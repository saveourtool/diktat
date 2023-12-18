package test.paragraph2.kdoc

class A constructor(
    name: String
) {}

class A constructor(
    //single-line comment
    name: String
) {}

class A constructor(
    /*
     * block
     * comment
     */
    name: String
) {}

class A constructor(
    /**
     * kdoc property
     * comment
     */
    name: String
) {}

class A constructor(
    /**
     * @property name property
     * comment
     */
    name: String
) {}

class A constructor(
    val name: String
) {}

class A constructor(
    //single-line comment
    val name: String
) {}

class A constructor(
    /*
     * block
     * comment
     */
    val name: String
) {}

class A constructor(
    /**
     * kdoc property
     * comment
     */
    val name: String
) {}

class A constructor(
    /**
     * @property name property
     * comment
     */
    val name: String
) {}

class A constructor(
    private val name: String
) {}

class A constructor(
    //single-line comment
    private val name: String
) {}

class A constructor(
    /*
     * block
     * comment
     */
    private val name: String
) {}

class A constructor(
    /**
     * kdoc property
     * comment
     */
    private val name: String
) {}

class A constructor(
    /**
     * @property name property
     * comment
     */
    private val name: String
) {}

open class B<K : Any> constructor(
    //single-line comment
    open val openName: String,
    /*
     * block
     * comment
     */
    open val openLastName: String,
    /**
     * kdoc property
     * comment
     */
    open val openBirthDate: String,
    /**
     * @property openAddr property
     * comment
     */
    open val openAddr: String
) {}

class A<K : Any, P: Any, G: Any> constructor(
    //single-line comment
    private val privateName: String,
    //single-line comment
    protected val protectedName: String,
    //single-line comment
    internal val internalName: String,
    //single-line comment
    override val openName: String,
    //single-line comment
    val name: String,
    //single-line comment
    paramName: String,
    /*
     * block
     * comment
     */
    private val privateLastName: String,
    /*
     * block
     * comment
     */
    protected val protectedLastName: String,
    /*
     * block
     * comment
     */
    internal val internalLastName: String,
    /*
     * block
     * comment
     */
    override val openLastName: String,
    /*
     * block
     * comment
     */
    val lastName: String,
    /*
     * block
     * comment
     */
    paramLastName: String,
    /**
     * kdoc property
     * comment
     */
    private val privateBirthDate: String,
    /**
     * kdoc property
     * comment
     */
    protected val protectedBirthDate: String,
    /**
     * kdoc property
     * comment
     */
    internal val internalBirthDate: String,
    /**
     * kdoc property
     * comment
     */
    override val openBirthDate: String,
    /**
     * kdoc property
     * comment
     */
    val birthDate: String,
    /**
     * kdoc property
     * comment
     */
    paramBirthDate: String,
    /**
     * @property privateAddr property
     * comment
     */
    private val privateAddr: String,
    /**
     * @property protectedAddr property
     * comment
     */
    protected val protectedAddr: String,
    /**
     * @property internalAddr property
     * comment
     */
    internal val internalAddr: String,
    /**
     * @property openAddr property
     * comment
     */
    override val openAddr: String,
    /**
     * @property addr property
     * comment
     */
    val addr: String,
    /**
     * @property paramAddr property
     * comment
     */
    paramAddr: String,
) : B<K>(), C<P>, D<G> {}

actual annotation class JsonSerialize(
    actual val `as`: KClass<*>,
    actual val keyAs: KClass<*>,
)
