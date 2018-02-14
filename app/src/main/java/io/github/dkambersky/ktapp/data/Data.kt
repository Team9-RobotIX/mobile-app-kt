package io.github.dkambersky.ktapp.data

/**
 * Defines useful data classes
 */
data class LoginState(var sessionToken: String? = null, var name: String? = null, var loggedIn: Boolean = false)