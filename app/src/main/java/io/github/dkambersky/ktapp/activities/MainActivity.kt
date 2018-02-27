package io.github.dkambersky.ktapp.activities

import android.os.Bundle
import io.github.dkambersky.ktapp.R
import khttp.post
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buttonScan.setOnClickListener { transition(ScanningActivity::class.java) }
        buttonLogin.setOnClickListener { toggleVisibility(login_form) }
        email_sign_in_button.setOnClickListener { toggleVisibility(login_form); trySigningIn() }
        buttonCreateOrder.setOnClickListener { transition(CreateOrderActivity::class.java) }
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


}
