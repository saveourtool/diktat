package test.paragraph3.newlines

val elem1 = firstArgumentDot()?.secondArgumentDot
?.thirdArgumentDot
?.fourthArgumentDot
?.fifthArgumentDot
?.sixthArgumentDot


val elem2 = firstArgumentDot?.secondArgumentDot()
?.thirdArgumentDot
    ?.fourthArgumentDot
?.fifthArgumentDot
?.sixthArgumentDot


val elem3 = firstArgumentDot?.secondArgumentDot?.thirdArgumentDot()
?.fourthArgumentDot
    ?.fifthArgumentDot
?.sixthArgumentDot


val elem4 = firstArgumentDot?.secondArgumentDot?.thirdArgumentDot + firstArgumentDot?.secondArgumentDot?.thirdArgumentDot?.fourthArgumentDot


val elem5 = firstArgumentDot()!!.secondArgumentDot()!!
.thirdArgumentDot!!
.fourthArgumentDot!!
.fifthArgumentDot!!
.sixthArgumentDot()


val elem6 = firstArgumentDot!!.secondArgumentDot!!.thirdArgumentDot()!!
    .fourthArgumentDot!!
.fifthArgumentDot()!!
.sixthArgumentDot


val elem7 = firstArgumentDot!!.secondArgumentDot()!!
.thirdArgumentDot!!
.fourthArgumentDot()!!
    .fifthArgumentDot!!
.sixthArgumentDot


val elem8 = firstArgumentDot()!!.secondArgumentDot!!.thirdArgumentDot + firstArgumentDot!!.secondArgumentDot!!.thirdArgumentDot!!.fourthArgumentDot


val elem9 = firstArgumentDot().secondArgumentDot
.thirdArgumentDot()
.fourthArgumentDot
.fifthArgumentDot
.sixthArgumentDot


val elem10 = firstArgumentDot.secondArgumentDot()
.thirdArgumentDot
    .fourthArgumentDot
.fifthArgumentDot()
.sixthArgumentDot


val elem11 = firstArgumentDot.secondArgumentDot.thirdArgumentDot()
.fourthArgumentDot
    .fifthArgumentDot
.sixthArgumentDot


val elem12 = firstArgumentDot.secondArgumentDot.thirdArgumentDot + firstArgumentDot.secondArgumentDot().thirdArgumentDot.fourthArgumentDot


val elem13 = firstArgumentDot!!.secondArgumentDot?.thirdArgumentDot()
.fourthArgumentDot!!
.fifthArgumentDot()
?.sixthArgumentDot


val elem14 = firstArgumentDot.secondArgumentDot?.thirdArgumentDot()!!
.fourthArgumentDot
?.fifthArgumentDot
.sixthArgumentDot


val elem15 = firstArgumentDot?.secondArgumentDot!!.thirdArgumentDot.fourthArgumentDot()
.fifthArgumentDot!!
.sixthArgumentDot


val elem16 = firstArgumentDot.secondArgumentDot.thirdArgumentDot.fourthArgumentDot.fifthArgumentDot.sixthArgumentDot


val elem17 = firstArgumentDot!!.secondArgumentDot.thirdArgumentDot!!.fourthArgumentDot.fifthArgumentDot!!.sixthArgumentDot


val elem18 = firstArgumentDot.secondArgumentDot?.thirdArgumentDot.fourthArgumentDot?.fifthArgumentDot.sixthArgumentDot


private val holder: java.util.concurrent.atomic.AtomicReference<T> = java.util.concurrent.atomic.AtomicReference(valueToStore)


private val holder: kotlin.native.concurrent.AtomicReference<T> = kotlin.native.concurrent.AtomicReference(valueToStore)
