package test.paragraph3.multiple_modifiers

import org.jetbrains.kotlin.javax.inject.Inject

public @Annotation final fun foo() {
}

public @Annotation fun foo() {
}

public @Suppress()@Inject() fun qwe() {
}

public @Suppress() @Inject() fun qwe() {
}
