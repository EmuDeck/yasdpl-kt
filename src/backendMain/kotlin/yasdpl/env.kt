package yasdpl

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import kotlinx.serialization.Serializable
import platform.posix.getenv

@OptIn(ExperimentalForeignApi::class)
@Serializable
object DeckyEnv : Env {
    /// Decky version
    override val version: String
        get() {
            return (getenv("DECKY_VERSION")?.toKString()?.let(Result.Companion::success) ?: Result.failure(
                RuntimeException("DECKY_VERSION is not set")
            )).getOrThrow()
        }

    /// User running Decky
    override val user: String
        get() {
            return (getenv("DECKY_USER")?.toKString()?.let(Result.Companion::success) ?: Result.failure(
                RuntimeException(
                    "DECKY_USER is not set"
                )
            )).getOrThrow()
        }

    /// Home of user running Decky
   override val home: String
        get() {
            return (getenv("DECKY_HOME")?.toKString()?.let(Result.Companion::success) ?: Result.failure(
                RuntimeException(
                    "DECKY_HOME is not set"
                )
            )).getOrThrow()
        }

    /// Settings directory recommended to be used by Decky
    override val settingsDir: String
        get() {
            return (getenv("DECKY_PLUGIN_SETTINGS_DIR")?.toKString()?.let(Result.Companion::success) ?: Result.failure(
                RuntimeException("DECKY_PLUGIN_SETTINGS_DIR is not set")
            )).getOrThrow()
        }

    /// Runtime directory recommended to be used by Decky
    override val runtimeDir: String
        get() {
            return (getenv("DECKY_PLUGIN_RUNTIME_DIR")?.toKString()?.let(Result.Companion::success) ?: Result.failure(
                RuntimeException("DECKY_PLUGIN_RUNTIME_DIR is not set")
            )).getOrThrow()
        }

    /// Log directory recommended to be used by Decky
    override val logDir: String
        get() {
            return (getenv("DECKY_PLUGIN_LOG_DIR")?.toKString()?.let(Result.Companion::success) ?: Result.failure(
                RuntimeException("DECKY_PLUGIN_LOG_DIR is not set")
            )).getOrThrow()
        }

    /// Root directory of plugin
    override val pluginDir: String
        get() {
            return (getenv("DECKY_PLUGIN_DIR")?.toKString()?.let(Result.Companion::success) ?: Result.failure(
                RuntimeException("DECKY_PLUGIN_DIR is not set")
            )).getOrThrow()
        }

    /// Plugin name
    override val pluginName: String
        get() {
            return (getenv("DECKY_PLUGIN_NAME")?.toKString()?.let(Result.Companion::success) ?: Result.failure(
                RuntimeException("DECKY_PLUGIN_NAME is not set")
            )).getOrThrow()
        }

    /// Plugin version
    override val pluginVersion: String
        get() {
            return (getenv("DECKY_PLUGIN_VERSION")?.toKString()?.let(Result.Companion::success) ?: Result.failure(
                RuntimeException("DECKY_PLUGIN_VERSION is not set")
            )).getOrThrow()
        }

    /// Plugin author
    override val pluginAuthor: String
        get() {
            return (getenv("DECKY_PLUGIN_AUTHOR")?.toKString()?.let(Result.Companion::success) ?: Result.failure(
                RuntimeException("DECKY_PLUGIN_AUTHOR is not set")
            )).getOrThrow()
        }
}