package usdpl

import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals

class SerdeTest {
	@Test
	fun encodeDecode() {
		@Serializable
		data class TestStruct(val a: Int, val b: String)

		val testStruct = TestStruct(1, "2")

        val json = toJson(testStruct)
		//language=JSON
		assertEquals("""{"a":1,"b":"2"}""", json)
		val decoded = fromJson<TestStruct>(json)
		assertEquals(testStruct, decoded)
	}

	@Test
    fun encodeDecodeList() {
		@Serializable
        data class TestStruct(val a: List<Int>)

        val testStruct = TestStruct(listOf(1, 2))

        val json = toJson(testStruct)
		//language=JSON
		assertEquals("""{"a":[1,2]}""", json)
		val decoded = fromJson<TestStruct>(json)
		assertEquals(testStruct, decoded)
	}

	@Test
    fun encodeDecodeMap() {
		@Serializable
        data class TestStruct(val a: Map<String, Int>)

        val testStruct = TestStruct(mapOf("a" to 1, "b" to 2))

        val json = toJson(testStruct)
		//language=JSON
		assertEquals("""{"a":{"a":1,"b":2}}""", json)
		val decoded = fromJson<TestStruct>(json)
		assertEquals(testStruct, decoded)
	}

	@Test
	fun encodeDecodeNested() {
		@Serializable
        data class TestStruct(val a: Int, val b: String, val c: TestStruct?)

        val testStruct = TestStruct(1, "2", TestStruct(3, "4", null))

        val json = toJson(testStruct)
		//language=JSON
		assertEquals("""{"a":1,"b":"2","c":{"a":3,"b":"4","c":null}}""", json)
		val decoded = fromJson<TestStruct>(json)
		assertEquals(testStruct, decoded)
	}
}
