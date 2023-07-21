package usdpl

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator


/// Accepted Packet types and the data they contain
@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("discriminant")
sealed class Packet
{
	/// A remote call
	data class Call(val call: RemoteCall) : Packet()

	/// A response to a remote call
	data class CallResponse(val response: RemoteCallResponse) : Packet()

	/// Unused
	data object KeepAlive : Packet()

	/// Invalid
	data object Invalid : Packet()
	/// General message
	data class Message(val message: String) : Packet()

	/// Response to an unsupported packet
	data object Unsupported : Packet()

	/// Broken packet type, useful for testing
	data object Bad : Packet()

	/// Many packets merged into one
	data class Many(val children: List<Packet>) : Packet()

	/// Translation data dump
	data class Translations(val data: Map<String, List<String>>) : Packet()

	/// Request translations for language
	data class Language(val language: String) : Packet()
}