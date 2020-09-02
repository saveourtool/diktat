package test.paragraph3.annotations

class SomeClass
@Inject
@SomeAnnotation
constructor() {

    @ThirdAnnotation
    @FourthAnnotation
    fun someFunc(@Annotation var1: Int, @AnotherAnnotation var2: String) {

    }
}

class AnotherClass
@Inject
@SomeAnnotation
constructor() {

}
