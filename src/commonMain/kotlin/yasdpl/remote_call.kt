package yasdpl

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class RemoteCall(
    val function: String,
    val parameters: JsonElement?
)

@Serializable
data class RemoteCallResponse(
    val response: JsonElement?
)


