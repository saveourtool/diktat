import config.rules.RulesConfig
import config.rules.RulesConfigReader
import org.junit.Test

class JsonReaderTest {
    @Test
    fun `testing json reading`() {
        val a: List<RulesConfig>? = RulesConfigReader().readResource("rules-config.json")
        a?.filter(it.)
    }
}
