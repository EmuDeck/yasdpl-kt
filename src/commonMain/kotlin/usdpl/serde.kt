package usdpl

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

inline fun <reified T> toJson(value: T): String {
	return Json.encodeToString(value)
}

inline fun <reified T> fromJson(json: String): T {
	return Json.decodeFromString(json)
}