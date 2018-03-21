package io.github.dkambersky.ktapp.activities

import android.os.Bundle
import android.support.design.widget.Snackbar
import io.github.dkambersky.ktapp.FlobotApplication
import io.github.dkambersky.ktapp.R
import khttp.post
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.experimental.launch

class LoginActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        /* Load URL from prefs when app instantiated */
        (application as FlobotApplication).loadUrl()

        email_sign_in_button.setOnClickListener { trySigningIn() }
        prefs_button.setOnClickListener { transition(SettingsActivity::class.java) }
    }

    override fun onResume() {
        super.onResume()
        (application as FlobotApplication).loadUrl()
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
                    transition(MainActivity::class.java)
                }
                401 -> {
                    showSnackbar("Wrong username or password")
                }

            }


        }
    }

}
