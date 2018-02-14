package io.github.dkambersky.ktapp.activities

import android.os.Bundle
import io.github.dkambersky.ktapp.R
import khttp.post
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {
    private val angleStep = 45.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buttonOnOff.setOnClickListener { onOffPressed() }
        buttonLeft.setOnClickListener { rightPressed() }
        buttonRight.setOnClickListener { leftPressed() }
        buttonJoystick.setOnClickListener { transition(JoystickActivity::class.java) }
        buttonScan.setOnClickListener { transition(ScanningActivity::class.java) }
        buttonLogin.setOnClickListener { toggleVisibility(login_form) }
        email_sign_in_button.setOnClickListener { toggleVisibility(login_form); trySigningIn() }
    }

    private fun trySigningIn() {
        val token = post(flobotApp.serverUrl+"/login",
                mapOf(
                        "username" to email.text.toString(),
                        "password" to password.text.toString()
                        )
        ).text

        showSnackbar(token)
    }

    private var onOff = 0
    private var angle = 0.0

    private fun onOffPressed() {
        onOff = if (onOff == 0) 1 else 0
        sendCommand()
    }

    private fun rightPressed() {
        angle -= angleStep
        sendCommand()
    }

    private fun leftPressed() {
        angle += angleStep
        sendCommand()
    }

    private fun sendCommand() {
        sendToServer( params = mapOf(
                "onOff" to onOff.toString(),
                "turnAngle" to angle.toString()
        ))
    }
}
