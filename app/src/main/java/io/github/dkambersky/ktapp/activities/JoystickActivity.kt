package io.github.dkambersky.ktapp.activities

import android.os.Bundle
import io.github.controlwear.virtual.joystick.android.JoystickView
import io.github.dkambersky.ktapp.R
import kotlinx.android.synthetic.main.activity_joystick.*


class JoystickActivity : BaseActivity() {

    /* Range in degrees of what angle changes to ignore,
     *  to prevent the robot from constantly correcting
     *  by 1 degree or so
     */
    private val ANGLE_TOLERANCE = 5

    /* Value in 1..100 at which the robot stops (joystick strength) */
    private val STRENGTH_THRESHOLD = 35

    private var lastAngle = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_joystick)

        val joystick = findViewById<JoystickView>(R.id.joystickView)

        joystick.setOnMoveListener({ angle, strength -> processJoystickInput(angle, strength) }, 1000)

        joystickMovementSwitch.setOnClickListener {
            sendToServer(params = mapOf(
                    "onOff" to (if (joystickMovementSwitch.isChecked) 1 else 0).toString(),
                    "turnAngle" to lastAngle.toString()
            ))
        }

    }


    private fun processJoystickInput(angle: Int, strength: Int) {

        /* If input is at 0, repeat last input with correct onOff */
        if (angle == 0 && strength == 0) {
            sendToServer(
                    params = mapOf(
                            "onOff" to (if (joystickMovementSwitch.isChecked) 1 else 0).toString(),
                            "turnAngle" to lastAngle.toString()
                    )
            )
            return
        }

        /* Map to our angle format */
        val turnAngle =
                when {
                    strength <= STRENGTH_THRESHOLD -> lastAngle
                    angle <= 90 -> 90 - angle
                    angle <= 269 -> -1 * (angle - 90)
                    else -> 180 - (angle - 270)
                }

        /* Don't send tiny changes in direction */
        if (Math.abs(turnAngle - lastAngle) < ANGLE_TOLERANCE)
            return

        lastAngle = turnAngle

        val args = mapOf(
                "onOff" to (if (strength > STRENGTH_THRESHOLD || joystickMovementSwitch.isChecked) 1 else 0).toString(),
                "turnAngle" to turnAngle.toString()
        )

        sendToServer(params = args)

        println("Original: $angle, mapped: $turnAngle, str: $strength, args: $args")
    }
}
