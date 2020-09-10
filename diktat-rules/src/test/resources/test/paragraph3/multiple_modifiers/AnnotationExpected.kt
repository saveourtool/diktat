package test.paragraph3.multiple_modifiers

import org.jetbrains.kotlin.javax.inject.Inject

@Annotation public final fun foo() {
}

@Annotation public fun foo() {
}

@Inject() @Suppress() public fun qwe() {
}

@Inject() @Suppress() public fun qwe() {
}
