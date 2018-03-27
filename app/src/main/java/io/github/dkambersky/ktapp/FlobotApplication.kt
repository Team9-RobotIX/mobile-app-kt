package io.github.dkambersky.ktapp

import android.app.Application
import android.preference.PreferenceManager
import io.github.dkambersky.ktapp.data.AuthState

/**
 * Stores global information to avoid the overhead
 * of constantly serializing and deserializing the same data.
 *
 * Can be GC'd by the android system if the user switches to a different app
 * and RAM is scarce; it's a trade-off for convenience.
 *
 */
class FlobotApplication : Application() {
    var serverUrl = ""
    val auth = AuthState()
    fun loadUrl() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        serverUrl = prefs.getString("serverUrl", "http://ec2-18-219-63-23.us-east-2.compute.amazonaws.com/development")
    }
}