package yasdpl

import kotlinx.serialization.Serializable
import kotlin.test.Test

class Main {
	@Test
	fun test() {
        val instance = YasdplInstance(4200)

		@Serializable
		data class Test(
			val a: Int,
            val b: Int
		)

		instance.yasdplCallBackendPromise<Test, Test>("test", Test(1, 2)).then { value ->
			println(value)
		}

    }
}