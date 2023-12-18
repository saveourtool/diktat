package test.paragraph2.kdoc

/**
 * @param name
 */
class A constructor(
    name: String
) {}

/**
 * @param name single-line comment
 */
class A constructor(
    name: String
) {}

/**
 * @param name
 *   block
 *   comment
 */
class A constructor(
    name: String
) {}

/**
 * @param name
 *   kdoc property
 *   comment
 */
class A constructor(
    name: String
) {}

class A constructor(
    /**
     * @property name property
     * comment
     */
    name: String
) {}

/**
 * @property name
 */
class A constructor(
    val name: String
) {}

/**
 * @property name single-line comment
 */
class A constructor(
    val name: String
) {}

/**
 * @property name
 *   block
 *   comment
 */
class A constructor(
    val name: String
) {}

/**
 * @property name
 *   kdoc property
 *   comment
 */
class A constructor(
    val name: String
) {}

class A constructor(
    /**
     * @property name property
     * comment
     */
    val name: String
) {}

/**
 * @param name
 */
class A constructor(
    private val name: String
) {}

/**
 * @param name single-line comment
 */
class A constructor(
    private val name: String
) {}

/**
 * @param name
 *   block
 *   comment
 */
class A constructor(
    private val name: String
) {}

/**
 * @param name
 *   kdoc property
 *   comment
 */
class A constructor(
    private val name: String
) {}

class A constructor(
    /**
     * @property name property
     * comment
     */
    private val name: String
) {}

/**
 * @param K
 * @property openName single-line comment
 * @property openLastName
 *   block
 *   comment
 * @property openBirthDate
 *   kdoc property
 *   comment
 */
open class B<K : Any> constructor(
    open val openName: String,
    open val openLastName: String,
    open val openBirthDate: String,
    /**
     * @property openAddr property
     * comment
     */
    open val openAddr: String
) {}

/**
 * @param K
 * @param P
 * @param G
 * @param privateName single-line comment
 * @property protectedName single-line comment
 * @property internalName single-line comment
 * @property openName single-line comment
 * @property name single-line comment
 * @param paramName single-line comment
 * @param privateLastName
 *   block
 *   comment
 * @property protectedLastName
 *   block
 *   comment
 * @property internalLastName
 *   block
 *   comment
 * @property openLastName
 *   block
 *   comment
 * @property lastName
 *   block
 *   comment
 * @param paramLastName
 *   block
 *   comment
 * @param privateBirthDate
 *   kdoc property
 *   comment
 * @property protectedBirthDate
 *   kdoc property
 *   comment
 * @property internalBirthDate
 *   kdoc property
 *   comment
 * @property openBirthDate
 *   kdoc property
 *   comment
 * @property birthDate
 *   kdoc property
 *   comment
 * @param paramBirthDate
 *   kdoc property
 *   comment
 */
class A<K : Any, P: Any, G: Any> constructor(
    private val privateName: String,
    protected val protectedName: String,
    internal val internalName: String,
    override val openName: String,
    val name: String,
    paramName: String,
    private val privateLastName: String,
    protected val protectedLastName: String,
    internal val internalLastName: String,
    override val openLastName: String,
    val lastName: String,
    paramLastName: String,
    private val privateBirthDate: String,
    protected val protectedBirthDate: String,
    internal val internalBirthDate: String,
    override val openBirthDate: String,
    val birthDate: String,
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

/**
 * @property as
 * @property keyAs
 */
actual annotation class JsonSerialize(
    actual val `as`: KClass<*>,
    actual val keyAs: KClass<*>,
)
