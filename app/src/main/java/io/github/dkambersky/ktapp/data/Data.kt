package io.github.dkambersky.ktapp.data

/**
 * Defines useful data classes
 */
data class AuthState(var token: String? = null, var name: String? = null, var loggedIn: Boolean = false) {
    fun loggedInFriendlyText() =
            if (loggedIn)
                "Logged in as $name"
            else
                "Not logged in"
}

data class DeliveryTarget(val id: Int, val name: String = "Unnamed target") {
    override fun toString() = name
}