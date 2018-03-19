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

data class Delivery(
        val description: String? = "",
        val from: DeliveryTarget,
        val id: Int,
        val name: String,
        val priority: Int,
        val receiver: String,
        val sender: String,
        val state: String,
        val to: DeliveryTarget
) {
    fun prettyPrint(userName: String): String {
        /* Recepient confirmation */
        if (state == "AWAITING_AUTHENTICATION_RECEIVER") {
            return "#$id - NEEDS CONFIRMATION - $name from $sender, $state"
        }


        /* Normal behavior */
        return if (userName == sender)
            "#$id - outgoing - $name to $receiver, $state"
        else
            "#$id - incoming - $name from $sender, $state"

    }
}

/*
*  {
    "description": "Shdhdh",
    "from": {
      "id": 1,
      "name": "Reception"
    },
    "id": 0,
    "name": "Elfjfh",
    "priority": 0,
    "receiver": "rms",
    "sender": "jsmith",
    "state": "AWAITING_AUTHENTICATION_RECEIVER",
    "to": {
      "id": 2,
      "name": "ER"
    }

*
* */

