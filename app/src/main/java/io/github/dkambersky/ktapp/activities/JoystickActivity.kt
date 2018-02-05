package io.github.dkambersky.ktapp.activities

import android.os.Bundle
import io.github.controlwear.virtual.joystick.android.JoystickView
import io.github.dkambersky.ktapp.R
import kotlinx.android.synthetic.main.activity_joystick.*


class JoystickActivity : BaseActivity() {
    var lastAngle = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_joystick)

        val joystick = findViewById<JoystickView>(R.id.joystickView)

        joystick.setOnMoveListener({ angle, strength -> processJoystickInput(angle, strength) }, 1000)

        joystickMovementSwitch.setOnClickListener {
            sendCommand(mapOf(
                    "onOff" to (if (joystickMovementSwitch.isChecked) 1 else 0).toString(),
                    "turnAngle" to lastAngle.toString()
            ))
        }

    }


    private fun processJoystickInput(angle: Int, strength: Int) {
        /* Map to our angle format */
        val turnAngle =
                when {
                    strength == 0 -> lastAngle
                    angle <= 90 -> 90 - angle
                    angle <= 269 -> -1 * (angle - 90)
                    else -> 180 - (angle - 270)
                }

        lastAngle = turnAngle

        println("Original: $angle, mapped: $turnAngle, str: $strength")

        sendCommand(mapOf(
                "onOff" to (if (strength != 0 || joystickMovementSwitch.isChecked) 1 else 0).toString(),
                "turnAngle" to turnAngle.toString()
        ))


    }
}
