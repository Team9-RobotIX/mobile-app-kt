package io.github.dkambersky.ktapp.data

/**
 * Defines useful data classes
 */
data class LoginState(var sessionToken: String? = null, var name: String? = null, var loggedIn: Boolean = false)

data class DeliveryTarget(val id: Int, val name: String = "Unnamed target"){
    override fun toString(): String {
        return name
    }
}