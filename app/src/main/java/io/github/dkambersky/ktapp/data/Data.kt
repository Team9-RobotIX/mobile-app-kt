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

data class DeliveryTarget(val id: Int, val name: String = "Unnamed target", val description: String? = "", val color: String? = "") {
    override fun toString() = name
}


data class Delivery(
        val description: String? = "",
        val from: DeliveryTarget,
        val id: Int,
        val name: String,
        val robot: Int,
        val priority: Int,
        val receiver: String,
        val sender: String,
        val state: String,
        val to: DeliveryTarget
) {
    fun prettyPrint(userName: String): String {
        /* Normal behavior */
        println("$sender, $receiver, $userName")
        if (userName == sender) {
            if (sender == receiver)
                return "#$id - self-ordered - $name to $receiver, ${prettyStringState(state)}"

            return "#$id - outgoing - $name to $receiver, ${prettyStringState(state)}"
        } else
            return "#$id - incoming - $name from $sender, ${prettyStringState(state)}"

    }
}


fun prettyStringState(state: String): String {
    return when (state) {
        "IN_QUEUE" -> "In queue"
        "MOVING_TO_SOURCE" -> "Moving to sender"
        "AWAITING_AUTHENTICATION_SENDER" -> "Awaiting verification by sender"
        "AWAITING_PACKAGE_LOAD" -> "Awaiting package load"
        "PACKAGE_LOAD_COMPLETE" -> "Package loaded"
        "MOVING_TO_DESTINATION" -> "Moving to recipient"
        "AWAITING_AUTHENTICATION_RECEIVER" -> "Awaiting verification by recipient"
        "AWAITING_PACKAGE_RETRIEVAL" -> "Awaiting package retrieval"
        "PACKAGE_RETRIEVAL_COMPLETE" -> "Package retrieved"
        "COMPLETE" -> "Complete"
        "UNKNOWN" -> "Unknown"
        else -> state
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

