package io.github.dkambersky.ktapp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import khttp.post
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.experimental.launch

class MainActivity : AppCompatActivity() {
    val angleStep = 45.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buttonOnOff.setOnClickListener { onOffPressed() }
        buttonLeft.setOnClickListener { rightPressed() }
        buttonRight.setOnClickListener { leftPressed() }

    }

    var onOff = 0
    var angle = 0.0

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
        launch {
            post("http://robotix.james-odonnell.com/post",
                    params = mapOf(
                            "onOff" to onOff.toString(),
                            "turnAngle" to angle.toString()
                    )
            )
        }
    }
}
