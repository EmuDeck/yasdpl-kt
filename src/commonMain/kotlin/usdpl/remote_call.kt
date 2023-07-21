package usdpl

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.json.Json

@Serializable
data class RemoteCall(
	val id: Int,
	val function: String,
	val parameters: List<String>
)

@Serializable
data class RemoteCallResponse(
    val id: Int,
    val result: List<String>
)