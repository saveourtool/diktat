import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.cqfn.diktat.plugin.gradle.DiktatGradlePlugin
import org.cqfn.diktat.plugin.gradle.DiktatExtension
import java.io.File

fun Project.configureDiktat() {
    apply<DiktatGradlePlugin>()
    configure<DiktatExtension> {
        debug = true
        inputs = files("src/main/kotlin/**/*.kt", "src/test/kotlin/**/*.kt")
        excludes = files("src/main/kotlin/generated/**/*.kt")
        diktatConfigFile = File("diktat-analysis.yml")
    }
}