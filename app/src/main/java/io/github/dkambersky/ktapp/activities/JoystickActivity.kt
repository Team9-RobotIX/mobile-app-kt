package io.github.dkambersky.ktapp.activities

import android.os.Bundle
import io.github.controlwear.virtual.joystick.android.JoystickView
import io.github.dkambersky.ktapp.R


class JoystickActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_joystick)

        val joystick = findViewById<JoystickView>(R.id.joystickView)

        joystick.setOnMoveListener({ angle, strength -> processJoystickInput(angle, strength) }, 1000)

    }


    private fun processJoystickInput(angle: Int, strength: Int) {


        /* Map to our format */
        val turnAngle =
                when {
                    strength == 0 -> 0
                    angle <= 90 -> 90 - angle
                    angle <= 269 -> -1 * (angle - 90)
                    else -> 180 - (angle - 270)
                }

        println("Original: $angle, mapped: $turnAngle, str: $strength")

        sendCommand(mapOf(
                "onOff" to (if (strength == 0) 0 else 1).toString(),
                "turnAngle" to turnAngle.toString()
        ))


    }
}
