@file:Suppress("unused", "HasPlatformType")

package io.reactivex.rxjava3.kotlin

import io.reactivex.rxjava3.annotations.CheckReturnValue
import io.reactivex.rxjava3.annotations.SchedulerSupport
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableSource
import io.reactivex.rxjava3.functions.*

/**
 * SAM adapters to aid Kotlin lambda support
 */
object Observables {

    @Deprecated("New type inference algorithm in Kotlin 1.4 makes this method obsolete. Method will be removed in future RxKotlin release.",
        replaceWith = ReplaceWith("Observable.combineLatest(source1, source2, combineFunction)", "io.reactivex.Observable"),
        level = DeprecationLevel.WARNING)
    @CheckReturnValue
    @SchedulerSupport(SchedulerSupport.NONE)
    inline fun <T1 : Any, T2 : Any, R : Any> combineLatest(
        source1: Observable<T1>,
        source2: Observable<T2>,
        crossinline combineFunction: (T1, T2) -> R
    ): Observable<R> = Observable.combineLatest(source1, source2,
        BiFunction<T1, T2, R> { t1, t2 -> combineFunction(t1, t2) })


    @Deprecated("New type inference algorithm in Kotlin 1.4 makes this method obsolete. Method will be removed in future RxKotlin release.",
        replaceWith = ReplaceWith("Observable.combineLatest(source1, source2, source3, source4, combineFunction)", "io.reactivex.Observable"),
        level = DeprecationLevel.WARNING)
    @CheckReturnValue
    @SchedulerSupport(SchedulerSupport.NONE)
    inline fun <T1 : Any, T2 : Any, T3 : Any, T4 : Any, R : Any> combineLatest(
        source1: Observable<T1>, source2: Observable<T2>, source3: Observable<T3>,
        source4: Observable<T4>, crossinline combineFunction: (T1, T2, T3, T4) -> R
    ): Observable<R> = Observable.combineLatest(source1, source2, source3, source4,
        Function4 { t1: T1, t2: T2, t3: T3, t4: T4 -> combineFunction(t1, t2, t3, t4) })

}
