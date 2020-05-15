import com.cygns.logik.Logik
import org.junit.Test
import java.lang.AssertionError
import java.lang.Math.pow

fun main() {
}

class LogikTest {

    @Test
    fun test_formatting() {
        assert(Logik.parse("2&&3").truthTable() == Logik.parse("2 and 3").truthTable())
    }

    @Test
    fun test_equality() {
        val statement1 = Logik.parse("q or (v or s)")
        val statement2 = Logik.parse("(q or v) or s")
        assert(statement1.truthTable() == statement2.truthTable())
        val statement3 = Logik.parse("q and (v or s)")
        assert(statement1.truthTable() != statement3.truthTable())
    }

    @Test
    fun test_orderOfOperations() {
        val statement1 = Logik.parse("p or q implies v")
        val statement2 = Logik.parse("p or (q implies v)")
        assert(statement1.truthTable() != statement2.truthTable())
    }
}