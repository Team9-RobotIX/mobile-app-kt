package io.github.dkambersky.ktapp.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.text.Spanned
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import io.github.dkambersky.ktapp.FlobotApplication
import java.io.Serializable


/**
 * Base Activity class.
 * Implements shared functionality like UI tweaks
 *  and simple transitions not to duplicate that
 *  in every activity separately.
 *
 */
abstract class BaseActivity : AppCompatActivity() {
    lateinit var flobotApp: FlobotApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /* Get application */
        flobotApp = application as FlobotApplication

    }

    /* UI Manipulation */
    protected fun enterFullscreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        window.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()
        supportActionBar?.hide()
    }

    protected fun leaveFullscreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN, WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        window.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_IMMERSIVE
        actionBar?.show()
        supportActionBar?.show()
    }

    /* Activity Transitions */
    protected fun <T> transition(activity: Class<T>, vararg data: Pair<String, Serializable>): Boolean where T : Activity {
        val intent = Intent(this, activity)

        data.forEach { intent.putExtra(it.first, it.second) }

        startActivity(intent)
        return true
    }

    /* Common snackBar methods */
    protected fun showSnackbar(message: String, length: Int = Snackbar.LENGTH_LONG): Snackbar {
        val rootView = this.window.decorView.findViewById<View>(android.R.id.content)
        val bar = Snackbar.make(rootView, message, length)

        bar.show()
        return bar

    }

    protected fun showSnackbar(message: Spanned, length: Int = Snackbar.LENGTH_LONG): Snackbar {
        val rootView = this.window.decorView.findViewById<View>(android.R.id.content)
        val bar = Snackbar.make(rootView, message, length)

        bar.show()
        return bar

    }

    protected fun toggleVisibility(vararg views: View) {
        views.forEach { it.visibility = if (it.visibility == View.GONE) View.VISIBLE else View.GONE }
    }

    protected fun hideKeyboardFrom(view: View) {
        val imm = this.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }


}