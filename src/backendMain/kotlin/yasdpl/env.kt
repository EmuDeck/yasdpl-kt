package yasdpl

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import platform.posix.getenv

@OptIn(ExperimentalForeignApi::class)
object DeckyEnv
{
    /// Decky version
    val version: Result<String> get()
    {
        return getenv("DECKY_VERSION")?.toKString()?.let(Result.Companion::success) ?: Result.failure(RuntimeException("DECKY_VERSION is not set"))
    }

/// User running Decky
    val user: Result<String> get()
    {
        return getenv("DECKY_USER")?.toKString()?.let(Result.Companion::success)?: Result.failure(RuntimeException("DECKY_USER is not set"))
    }

/// Home of user running Decky
    val home: Result<String> get()
    {
        return getenv("DECKY_HOME")?.toKString()?.let(Result.Companion::success)?: Result.failure(RuntimeException("DECKY_HOME is not set"))
    }

/// Settings directory recommended to be used by Decky
    val settingsDir: Result<String> get()
    {
        return getenv("DECKY_PLUGIN_SETTINGS_DIR")?.toKString()?.let(Result.Companion::success)?: Result.failure(RuntimeException("DECKY_PLUGIN_SETTINGS_DIR is not set"))
    }

/// Runtime directory recommended to be used by Decky
    val runtimeDir: Result<String> get()
    {
        return getenv("DECKY_PLUGIN_RUNTIME_DIR")?.toKString()?.let(Result.Companion::success)?: Result.failure(RuntimeException("DECKY_PLUGIN_RUNTIME_DIR is not set"))
    }

/// Log directory recommended to be used by Decky
    val logDir: Result<String> get()
    {
        return getenv("DECKY_PLUGIN_LOG_DIR")?.toKString()?.let(Result.Companion::success)?: Result.failure(RuntimeException("DECKY_PLUGIN_LOG_DIR is not set"))
    }

/// Root directory of plugin
    val pluginDir: Result<String> get()
    {
        return getenv("DECKY_PLUGIN_DIR")?.toKString()?.let(Result.Companion::success)?: Result.failure(RuntimeException("DECKY_PLUGIN_DIR is not set"))
    }

/// Plugin name
    val pluginName: Result<String> get()
    {
        return getenv("DECKY_PLUGIN_NAME")?.toKString()?.let(Result.Companion::success)?: Result.failure(RuntimeException("DECKY_PLUGIN_NAME is not set"))
    }

/// Plugin version
    val pluginVersion: Result<String> get()
    {
        return getenv("DECKY_PLUGIN_VERSION")?.toKString()?.let(Result.Companion::success)?: Result.failure(RuntimeException("DECKY_PLUGIN_VERSION is not set"))
    }

/// Plugin author
    val pluginAuthor: Result<String> get()
    {
        return getenv("DECKY_PLUGIN_AUTHOR")?.toKString()?.let(Result.Companion::success)?: Result.failure(RuntimeException("DECKY_PLUGIN_AUTHOR is not set"))
    }
}