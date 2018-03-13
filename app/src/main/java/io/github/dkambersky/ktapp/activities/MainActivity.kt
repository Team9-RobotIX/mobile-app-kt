package io.github.dkambersky.ktapp.activities

import android.app.Activity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.View
import android.view.inputmethod.InputMethodManager
import io.github.dkambersky.ktapp.R
import khttp.post
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.experimental.launch


class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buttonScan.setOnClickListener { transition(ScanningActivity::class.java) }
        buttonLogin.setOnClickListener { toggleVisibility(login_form) }
        email_sign_in_button.setOnClickListener { toggleVisibility(login_form); trySigningIn() }
        buttonCreateOrder.setOnClickListener { transition(CreateOrderActivity::class.java) }


        /* Disable creating order and scanning w/o login */
        buttonCreateOrder.isEnabled = false
        buttonScan.isEnabled = false

        authStatusText.text = flobotApp.auth.loggedInFriendlyText()

    }

    private fun trySigningIn() {
        /* Hide keyboard - android APIs for this are a bloody mess */
        hideKeyboardFrom(name)
        hideKeyboardFrom(password)

        launch {
            val uName = name.text.toString()
            val response = post(flobotApp.serverUrl + "/login",
                    json = mapOf(
                            "username" to uName,
                            "password" to password.text.toString()
                    )
            )

            when (response.statusCode) {
                200 -> {
                    /* Extract bearer token */
                    val token = response.jsonObject.get("bearer") as String

                    /* Save login data */
                    flobotApp.auth.name = uName
                    flobotApp.auth.loggedIn = true
                    flobotApp.auth.token = token

                    /* Inform the user */
                    showSnackbar("Logged in successfully!", Snackbar.LENGTH_LONG)


                    /* Enable user-dependent actions */
                    this@MainActivity.runOnUiThread {
                        buttonCreateOrder.isEnabled = true
                        buttonScan.isEnabled = true
                    }
                }
                401 -> {
                    showSnackbar("Wrong username or password")
                }

            }
            this@MainActivity.runOnUiThread({ authStatusText.text = flobotApp.auth.loggedInFriendlyText() })

        }
    }

    private fun hideKeyboardFrom(view: View) {
        val imm = this.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }


}
